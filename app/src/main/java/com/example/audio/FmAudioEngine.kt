package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.model.RadioRecording
import com.example.model.RadioStation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.util.UUID
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

class FmAudioEngine(private val context: Context) {

    private val sampleRate = 44100
    private val bufferSize = AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_STEREO,
        AudioFormat.ENCODING_PCM_16BIT
    ) * 2

    private var audioTrack: AudioTrack? = null
    private var playbackJob: Job? = null
    private val audioScope = CoroutineScope(Dispatchers.Default)

    // Current synthesis parameters
    @Volatile private var currentFreq = 92.4f
    @Volatile private var targetVolume = 0.85f
    @Volatile private var isMuted = false
    @Volatile private var squelch = true
    @Volatile private var antennaBoost = true
    @Volatile private var isPlaying = false
    @Volatile private var nearestStation: RadioStation? = null

    // Recording state
    @Volatile private var isRecording = false
    private var recordingFile: File? = null
    private var recordingStream: FileOutputStream? = null
    private var recordedBytesCount = 0L
    private var recordingStartTime = 0L

    // Visualizer callback
    var onSpectrumUpdate: ((List<Float>) -> Unit)? = null

    fun setFrequency(freq: Float, station: RadioStation?) {
        this.currentFreq = freq
        this.nearestStation = station
    }

    fun setVolume(vol: Float) {
        this.targetVolume = vol.coerceIn(0f, 1f)
    }

    fun setMuted(muted: Boolean) {
        this.isMuted = muted
    }

    fun setSquelch(enabled: Boolean) {
        this.squelch = enabled
    }

    fun setAntennaBoost(enabled: Boolean) {
        this.antennaBoost = enabled
    }

    @Synchronized
    fun start() {
        if (isPlaying) return
        isPlaying = true

        try {
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.play()
        } catch (_: Throwable) {
            // AudioTrack init fallback
        }

        playbackJob = audioScope.launch {
            runAudioLoop()
        }
    }

    @Synchronized
    fun stop() {
        isPlaying = false
        playbackJob?.cancel()
        playbackJob = null

        if (isRecording) {
            stopRecording()
        }

        try {
            audioTrack?.pause()
            audioTrack?.flush()
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Throwable) {
        } finally {
            audioTrack = null
        }
    }

    // Start in-app recording
    fun startRecording(freq: Float, stationName: String): Boolean {
        if (isRecording) return false
        return try {
            val recDir = File(context.filesDir, "fm_recordings").apply { mkdirs() }
            val fileName = "REC_${System.currentTimeMillis()}_${(freq * 10).toInt()}.wav"
            val file = File(recDir, fileName)
            val fos = FileOutputStream(file)

            // Write placeholder 44-byte WAV header
            fos.write(ByteArray(44))

            recordingFile = file
            recordingStream = fos
            recordedBytesCount = 0L
            recordingStartTime = System.currentTimeMillis()
            isRecording = true
            true
        } catch (_: Throwable) {
            false
        }
    }

    // Stop recording and finalize WAV header
    fun stopRecording(): RadioRecording? {
        if (!isRecording) return null
        isRecording = false
        val file = recordingFile ?: return null
        val fos = recordingStream
        val totalBytes = recordedBytesCount
        val startTime = recordingStartTime

        try {
            fos?.flush()
            fos?.close()
        } catch (_: Throwable) {}
        recordingStream = null
        recordingFile = null

        // Write valid WAV header
        try {
            writeWavHeader(file, totalBytes)
        } catch (_: Throwable) {}

        val durationSec = ((System.currentTimeMillis() - startTime) / 1000).toInt().coerceAtLeast(1)
        val station = nearestStation
        return RadioRecording(
            id = UUID.randomUUID().toString(),
            title = "${station?.name ?: "FM ${"%.1f".format(currentFreq)}"} Kaydı",
            frequency = currentFreq,
            stationName = station?.name ?: "Bilinmeyen Frekans",
            filePath = file.absolutePath,
            durationSeconds = durationSec,
            timestamp = startTime,
            fileSizeBytes = file.length()
        )
    }

    private fun writeWavHeader(file: File, pcmDataLength: Long) {
        val totalDataLen = pcmDataLength + 36
        val channels = 2
        val byteRate = sampleRate * channels * 2
        val blockAlign = channels * 2

        RandomAccessFile(file, "rw").use { raf ->
            raf.seek(0)
            raf.writeBytes("RIFF")
            raf.writeInt(Integer.reverseBytes(totalDataLen.toInt()))
            raf.writeBytes("WAVE")
            raf.writeBytes("fmt ")
            raf.writeInt(Integer.reverseBytes(16)) // Subchunk1Size (16 for PCM)
            raf.writeShort(java.lang.Short.reverseBytes(1.toShort()).toInt()) // AudioFormat 1=PCM
            raf.writeShort(java.lang.Short.reverseBytes(channels.toShort()).toInt())
            raf.writeInt(Integer.reverseBytes(sampleRate))
            raf.writeInt(Integer.reverseBytes(byteRate))
            raf.writeShort(java.lang.Short.reverseBytes(blockAlign.toShort()).toInt())
            raf.writeShort(java.lang.Short.reverseBytes(16.toShort()).toInt()) // BitsPerSample
            raf.writeBytes("data")
            raf.writeInt(Integer.reverseBytes(pcmDataLength.toInt()))
        }
    }

    private suspend fun CoroutineScope.runAudioLoop() {
        val chunkSize = 1024 // frames (stereo = 2048 samples)
        val shortBuffer = ShortArray(chunkSize * 2)
        val byteBuffer = ByteArray(chunkSize * 4)

        var phaseMelody = 0.0
        var phaseBass = 0.0
        var phaseLead = 0.0
        var phaseBeep = 0.0
        var tickCounter = 0L

        val spectrumBands = FloatArray(16)

        while (isActive && isPlaying) {
            val st = nearestStation
            val freq = currentFreq
            val stationFreq = st?.frequency ?: -1f
            val dist = if (stationFreq > 0) abs(freq - stationFreq) else 99f

            // Carrier capture threshold:
            // With antenna boost, lock window is 0.22 MHz; without boost, 0.15 MHz.
            val lockThreshold = if (antennaBoost) 0.22f else 0.15f
            val isLocked = dist <= lockThreshold

            val clarity = if (isLocked) {
                ((1f - (dist / lockThreshold))).coerceIn(0.05f, 1f)
            } else 0f

            val vol = if (isMuted) 0f else targetVolume

            // Determine program musical profile
            val stationId = st?.id ?: ""
            val (bpm, scaleNotes, bassFreqs) = getMusicProfile(stationId)

            val samplesPerBeat = (sampleRate * 60.0 / bpm).toInt().coerceAtLeast(1000)

            for (i in 0 until chunkSize) {
                val beatIndex = ((tickCounter / samplesPerBeat) % scaleNotes.size).toInt()
                val subBeat = ((tickCounter / (samplesPerBeat / 2)) % scaleNotes.size).toInt()

                val melodyFreq = scaleNotes[beatIndex]
                val bassFreq = bassFreqs[(beatIndex / 2) % bassFreqs.size]
                val leadFreq = scaleNotes[(subBeat + 2) % scaleNotes.size]

                // Phase increments
                phaseMelody += 2.0 * PI * melodyFreq / sampleRate
                phaseBass += 2.0 * PI * bassFreq / sampleRate
                phaseLead += 2.0 * PI * leadFreq / sampleRate

                if (phaseMelody > 2.0 * PI) phaseMelody -= 2.0 * PI
                if (phaseBass > 2.0 * PI) phaseBass -= 2.0 * PI
                if (phaseLead > 2.0 * PI) phaseLead -= 2.0 * PI

                // Rhythmic envelope
                val beatPos = (tickCounter % samplesPerBeat).toFloat() / samplesPerBeat
                val envMelody = (1f - beatPos * 0.8f).coerceIn(0.1f, 1f)
                val envBass = if (beatPos < 0.6f) 1f - (beatPos / 0.6f) * 0.5f else 0.2f

                // Synthesize station music wave
                val waveMelody = sin(phaseMelody) * envMelody * 0.4
                val waveBass = (sin(phaseBass) + 0.3 * sin(phaseBass * 2.0)) * envBass * 0.4
                val waveLead = sin(phaseLead) * 0.18

                // For News / NTV, add periodic bulletin pings
                var newsPing = 0.0
                if (stationId == "ntv_radyo") {
                    val pingCycle = (sampleRate * 4) // every 4s
                    val pingPos = (tickCounter % pingCycle)
                    if (pingPos < sampleRate / 2) {
                        phaseBeep += 2.0 * PI * 880.0 / sampleRate
                        newsPing = sin(phaseBeep) * (1.0 - pingPos.toDouble() / (sampleRate / 2)) * 0.35
                    }
                }

                val stationSample = (waveMelody + waveBass + waveLead + newsPing) * clarity

                // Synthesize FM static noise
                var staticSample = 0.0
                if (!isLocked) {
                    if (!squelch) {
                        // FM white noise hiss
                        staticSample = (Random.nextDouble(-1.0, 1.0) * 0.22)
                    }
                } else {
                    // Slight static background when tuning is not 100% centered
                    val staticAmount = ((1f - clarity) * 0.35f).coerceIn(0f, 0.4f)
                    if (!squelch || staticAmount > 0.05f) {
                        staticSample = (Random.nextDouble(-1.0, 1.0) * staticAmount.toDouble())
                    }
                }

                // Mix final sound
                val combinedLeft = ((stationSample * 0.95 + staticSample) * vol).coerceIn(-1.0, 1.0)
                val combinedRight = ((stationSample * 1.0 + staticSample * 0.98) * vol).coerceIn(-1.0, 1.0)

                val shortL = (combinedLeft * 32767).toInt().toShort()
                val shortR = (combinedRight * 32767).toInt().toShort()

                val idx = i * 2
                shortBuffer[idx] = shortL
                shortBuffer[idx + 1] = shortR

                if (isRecording) {
                    val bIdx = i * 4
                    byteBuffer[bIdx] = (shortL.toInt() and 0xFF).toByte()
                    byteBuffer[bIdx + 1] = ((shortL.toInt() shr 8) and 0xFF).toByte()
                    byteBuffer[bIdx + 2] = (shortR.toInt() and 0xFF).toByte()
                    byteBuffer[bIdx + 3] = ((shortR.toInt() shr 8) and 0xFF).toByte()
                }

                tickCounter++
            }

            // Write to audio output
            audioTrack?.write(shortBuffer, 0, shortBuffer.size)

            // Write to recording stream if active
            if (isRecording) {
                try {
                    recordingStream?.write(byteBuffer, 0, byteBuffer.size)
                    recordedBytesCount += byteBuffer.size
                } catch (_: Throwable) {}
            }

            // Calculate spectrum visualizer levels
            var sumEnergy = 0f
            for (k in 0 until chunkSize step 32) {
                sumEnergy += abs(shortBuffer[k * 2].toFloat() / 32768f)
            }
            val baseEnergy = (sumEnergy / (chunkSize / 32)).coerceIn(0.05f, 1.0f)

            for (b in 0 until 16) {
                val decay = 0.72f
                val jitter = Random.nextFloat() * 0.35f
                val target = (baseEnergy * (1f - (b * 0.035f)) + jitter * baseEnergy).coerceIn(0.05f, 1f)
                spectrumBands[b] = (spectrumBands[b] * decay + target * (1f - decay)).coerceIn(0.05f, 1f)
            }

            onSpectrumUpdate?.invoke(spectrumBands.toList())
        }
    }

    private data class MusicProfile(val bpm: Double, val notes: List<Double>, val bass: List<Double>)

    private fun getMusicProfile(stationId: String): MusicProfile {
        return when (stationId) {
            "kral_fm", "trt_turku" -> {
                // Arabesk / Türkü modal Turkish scale (Hicaz style vibes, A - Bb - C# - D - E - F - G)
                MusicProfile(
                    bpm = 78.0,
                    notes = listOf(220.0, 233.08, 277.18, 293.66, 329.63, 349.23, 392.0, 440.0),
                    bass = listOf(110.0, 116.54, 138.59, 146.83)
                )
            }
            "power_fm", "fenomen" -> {
                // High energy Dance / EDM (128 BPM, Em Pentatonic)
                MusicProfile(
                    bpm = 126.0,
                    notes = listOf(329.63, 392.00, 440.00, 493.88, 587.33, 659.25),
                    bass = listOf(82.41, 98.00, 110.00, 73.42)
                )
            }
            "joy_fm" -> {
                // Warm romantic slow acoustic (70 BPM, Cmaj7 / Am9)
                MusicProfile(
                    bpm = 68.0,
                    notes = listOf(261.63, 329.63, 392.00, 493.88, 523.25, 440.00),
                    bass = listOf(130.81, 110.00, 98.00, 87.31)
                )
            }
            "rad_eksen" -> {
                // Rock (110 BPM, D Minor riff)
                MusicProfile(
                    bpm = 112.0,
                    notes = listOf(293.66, 349.23, 440.00, 523.25, 587.33),
                    bass = listOf(73.42, 87.31, 110.00, 98.00)
                )
            }
            "ntv_radyo" -> {
                // News & Talk broadcast atmosphere (95 BPM, structured pulses)
                MusicProfile(
                    bpm = 92.0,
                    notes = listOf(440.0, 554.37, 659.25, 880.0),
                    bass = listOf(110.0, 110.0, 146.83, 110.0)
                )
            }
            else -> {
                // Turkish Pop & General (Alem FM, Süper FM, TRT FM, Show Radyo) (105 BPM, G Major)
                MusicProfile(
                    bpm = 104.0,
                    notes = listOf(392.00, 440.00, 493.88, 523.25, 587.33, 659.25),
                    bass = listOf(98.00, 110.00, 123.47, 130.81)
                )
            }
        }
    }
}
