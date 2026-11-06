package com.example.ui

import android.app.Application
import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.FmAudioEngine
import com.example.audio.FmHardwareDetector
import com.example.audio.HardwareReport
import com.example.data.FmRadioRepository
import com.example.model.EqualizerPreset
import com.example.model.RadioRecording
import com.example.model.RadioState
import com.example.model.RadioStation
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

class RadioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FmRadioRepository(application)
    private val audioEngine = FmAudioEngine(application)
    private val hardwareDetector = FmHardwareDetector(application)

    private val vibrator: Vibrator? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    } catch (_: Throwable) {
        null
    }

    private val _radioState = MutableStateFlow(RadioState())
    val radioState: StateFlow<RadioState> = _radioState.asStateFlow()

    val stations: StateFlow<List<RadioStation>> = repository.stationsFlow
    val recordings: StateFlow<List<RadioRecording>> = repository.recordingsFlow
    val presets: StateFlow<List<Float>> = repository.presetsFlow

    private val _hardwareReport = MutableStateFlow(hardwareDetector.detect())
    val hardwareReport: StateFlow<HardwareReport> = _hardwareReport.asStateFlow()

    // Recording playback state
    private var recordingPlayer: MediaPlayer? = null
    private val _playingRecordingId = MutableStateFlow<String?>(null)
    val playingRecordingId: StateFlow<String?> = _playingRecordingId.asStateFlow()

    private val _recordingProgressSec = MutableStateFlow(0)
    val recordingProgressSec: StateFlow<Int> = _recordingProgressSec.asStateFlow()

    // Selected screen in bottom bar
    private val _currentScreen = MutableStateFlow(ScreenTab.TUNER)
    val currentScreen: StateFlow<ScreenTab> = _currentScreen.asStateFlow()

    // Filter query & category
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Tümü")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Filtered stations flow
    val filteredStations: StateFlow<List<RadioStation>> = combine(
        stations,
        _searchQuery,
        _selectedCategory
    ) { list, query, category ->
        list.filter { st ->
            val matchesQuery = query.isBlank() ||
                    st.name.contains(query, ignoreCase = true) ||
                    st.genre.contains(query, ignoreCase = true) ||
                    "%.1f".format(st.frequency).contains(query)

            val matchesCategory = when (category) {
                "Tümü" -> true
                "Favoriler" -> st.isFavorite
                "Pop" -> st.genre.contains("Pop", ignoreCase = true) || st.genre.contains("Hit", ignoreCase = true)
                "Haber" -> st.genre.contains("Haber", ignoreCase = true) || st.genre.contains("Güncel", ignoreCase = true)
                "Türkü & Arabesk" -> st.genre.contains("Arabesk", ignoreCase = true) || st.genre.contains("Halk", ignoreCase = true)
                "Slow" -> st.genre.contains("Slow", ignoreCase = true) || st.genre.contains("Romantik", ignoreCase = true)
                "Özel Yerel" -> st.isCustom
                else -> true
            }

            matchesQuery && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var scanJob: Job? = null
    private var sleepTimerJob: Job? = null
    private var recordingTimerJob: Job? = null
    private var recordingProgressJob: Job? = null

    init {
        // Restore preferences
        val lastFreq = repository.getLastFrequency()
        val vol = repository.getVolume()
        val squelch = repository.isSquelchEnabled()
        val boost = repository.isAntennaBoostEnabled()

        _radioState.value = _radioState.value.copy(
            currentFrequency = lastFreq,
            volume = vol,
            squelchEnabled = squelch,
            internalAntennaBoost = boost
        )

        audioEngine.setFrequency(lastFreq, findStationAt(lastFreq))
        audioEngine.setVolume(vol)
        audioEngine.setSquelch(squelch)
        audioEngine.setAntennaBoost(boost)

        audioEngine.onSpectrumUpdate = { levels ->
            _radioState.value = _radioState.value.copy(audioSpectrumLevels = levels)
        }

        updateActiveStationAndSignal(lastFreq)
    }

    fun setScreen(tab: ScreenTab) {
        _currentScreen.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(cat: String) {
        _selectedCategory.value = cat
    }

    fun refreshHardwareReport() {
        _hardwareReport.value = hardwareDetector.detect()
    }

    fun togglePower() {
        vibrateClick(25)
        val newPower = !_radioState.value.isPoweredOn
        _radioState.value = _radioState.value.copy(
            isPoweredOn = newPower,
            isPlaying = if (!newPower) false else _radioState.value.isPlaying
        )
        if (!newPower) {
            audioEngine.stop()
        } else if (_radioState.value.isPlaying) {
            audioEngine.start()
        }
    }

    fun togglePlayPause() {
        vibrateClick(30)
        if (!_radioState.value.isPoweredOn) {
            _radioState.value = _radioState.value.copy(isPoweredOn = true)
        }
        val newPlaying = !_radioState.value.isPlaying
        _radioState.value = _radioState.value.copy(isPlaying = newPlaying)

        if (newPlaying) {
            // Stop recorded preview if any
            stopRecordingPlayback()
            audioEngine.start()
        } else {
            audioEngine.stop()
        }
    }

    fun setFrequency(freq: Float) {
        val bounded = ((freq * 10f).roundToInt() / 10f).coerceIn(87.5f, 108.0f)
        if (abs(bounded - _radioState.value.currentFrequency) >= 0.05f) {
            vibrateClick(10)
        }
        _radioState.value = _radioState.value.copy(currentFrequency = bounded)
        repository.setLastFrequency(bounded)
        updateActiveStationAndSignal(bounded)
    }

    fun stepFrequency(delta: Float) {
        vibrateClick(15)
        val newFreq = (_radioState.value.currentFrequency + delta)
        setFrequency(newFreq)
    }

    fun seekStation(forward: Boolean) {
        vibrateClick(35)
        scanJob?.cancel()
        val all = stations.value
        if (all.isEmpty()) return

        val current = _radioState.value.currentFrequency
        val targetStation = if (forward) {
            all.firstOrNull { it.frequency > current + 0.15f } ?: all.firstOrNull()
        } else {
            all.lastOrNull { it.frequency < current - 0.15f } ?: all.lastOrNull()
        }

        if (targetStation != null) {
            scanJob = viewModelScope.launch {
                _radioState.value = _radioState.value.copy(isScanning = true, scanDirectionForward = forward)
                // Animate smooth seeker dial movement
                val step = if (forward) 0.1f else -0.1f
                var f = current
                val targetF = targetStation.frequency
                val maxSteps = 40
                var stepCount = 0

                while (abs(f - targetF) > 0.15f && stepCount < maxSteps) {
                    f += step
                    if (f > 108.0f) f = 87.5f
                    if (f < 87.5f) f = 108.0f
                    setFrequency(f)
                    delay(45)
                    stepCount++
                }
                setFrequency(targetF)
                _radioState.value = _radioState.value.copy(isScanning = false)
            }
        }
    }

    fun autoScanFullBand() {
        vibrateClick(40)
        scanJob?.cancel()
        scanJob = viewModelScope.launch {
            _radioState.value = _radioState.value.copy(isScanning = true)
            var f = 87.5f
            while (f <= 108.0f) {
                setFrequency(f)
                val st = findStationAt(f)
                if (st != null) {
                    delay(1200) // pause briefly on locked station
                } else {
                    delay(60)
                }
                f += 0.2f
            }
            _radioState.value = _radioState.value.copy(isScanning = false)
        }
    }

    fun stopScan() {
        scanJob?.cancel()
        _radioState.value = _radioState.value.copy(isScanning = false)
    }

    private fun updateActiveStationAndSignal(freq: Float) {
        val st = findStationAt(freq)
        val dist = if (st != null) abs(freq - st.frequency) else 99f

        val (bars, dbm) = when {
            dist <= 0.05f -> 5 to (st?.signalStrengthDbm ?: -40)
            dist <= 0.15f -> 4 to (st?.signalStrengthDbm ?: -55) - 10
            dist <= 0.25f -> 3 to (st?.signalStrengthDbm ?: -65) - 18
            dist <= 0.40f -> 1 to -88
            else -> 0 to -98
        }

        _radioState.value = _radioState.value.copy(
            activeStation = st,
            signalBars = bars,
            signalDbm = dbm
        )

        audioEngine.setFrequency(freq, st)
    }

    private fun findStationAt(freq: Float): RadioStation? {
        val tolerance = if (_radioState.value.internalAntennaBoost) 0.25f else 0.15f
        return stations.value.minByOrNull { abs(it.frequency - freq) }
            ?.takeIf { abs(it.frequency - freq) <= tolerance }
    }

    fun setVolume(vol: Float) {
        val clamped = vol.coerceIn(0f, 1f)
        _radioState.value = _radioState.value.copy(volume = clamped)
        repository.setVolume(clamped)
        audioEngine.setVolume(clamped)
    }

    fun toggleMute() {
        vibrateClick(20)
        val newMuted = !_radioState.value.isMuted
        _radioState.value = _radioState.value.copy(isMuted = newMuted)
        audioEngine.setMuted(newMuted)
    }

    fun toggleStereo() {
        vibrateClick(20)
        _radioState.value = _radioState.value.copy(isStereo = !_radioState.value.isStereo)
    }

    fun toggleSquelch() {
        vibrateClick(20)
        val newSquelch = !_radioState.value.squelchEnabled
        _radioState.value = _radioState.value.copy(squelchEnabled = newSquelch)
        repository.setSquelchEnabled(newSquelch)
        audioEngine.setSquelch(newSquelch)
    }

    fun toggleInternalAntennaBoost() {
        vibrateClick(30)
        val newBoost = !_radioState.value.internalAntennaBoost
        _radioState.value = _radioState.value.copy(internalAntennaBoost = newBoost)
        repository.setAntennaBoostEnabled(newBoost)
        audioEngine.setAntennaBoost(newBoost)
        updateActiveStationAndSignal(_radioState.value.currentFrequency)
    }

    fun setEqualizer(preset: EqualizerPreset) {
        vibrateClick(20)
        _radioState.value = _radioState.value.copy(activeEqualizer = preset)
    }

    fun toggleRecord() {
        vibrateClick(40)
        if (_radioState.value.isRecording) {
            recordingTimerJob?.cancel()
            val savedRec = audioEngine.stopRecording()
            if (savedRec != null) {
                repository.addRecording(savedRec)
            }
            _radioState.value = _radioState.value.copy(
                isRecording = false,
                recordingDurationSeconds = 0
            )
        } else {
            val freq = _radioState.value.currentFrequency
            val name = _radioState.value.activeStation?.name ?: "FM ${"%.1f".format(freq)}"
            val success = audioEngine.startRecording(freq, name)
            if (success) {
                _radioState.value = _radioState.value.copy(
                    isRecording = true,
                    recordingDurationSeconds = 0
                )
                recordingTimerJob?.cancel()
                recordingTimerJob = viewModelScope.launch {
                    while (true) {
                        delay(1000)
                        _radioState.value = _radioState.value.copy(
                            recordingDurationSeconds = _radioState.value.recordingDurationSeconds + 1
                        )
                    }
                }
            }
        }
    }

    fun setSleepTimer(minutes: Int?) {
        vibrateClick(20)
        sleepTimerJob?.cancel()
        _radioState.value = _radioState.value.copy(sleepTimerMinutesLeft = minutes)

        if (minutes != null && minutes > 0) {
            sleepTimerJob = viewModelScope.launch {
                var remaining = minutes
                while (remaining > 0) {
                    delay(60_000)
                    remaining--
                    _radioState.value = _radioState.value.copy(sleepTimerMinutesLeft = remaining)
                }
                // Stop radio playback when timer expires
                _radioState.value = _radioState.value.copy(
                    isPlaying = false,
                    sleepTimerMinutesLeft = null
                )
                audioEngine.stop()
            }
        }
    }

    fun savePreset(index: Int) {
        vibrateClick(50)
        val current = _radioState.value.currentFrequency
        repository.savePreset(index, current)
    }

    fun toggleFavorite(stationId: String) {
        vibrateClick(25)
        repository.toggleFavorite(stationId)
        updateActiveStationAndSignal(_radioState.value.currentFrequency)
    }

    fun addCustomStation(name: String, freq: Float, genre: String, location: String) {
        val st = repository.addCustomStation(name, freq, genre, location)
        setFrequency(st.frequency)
    }

    fun removeCustomStation(stationId: String) {
        repository.removeCustomStation(stationId)
    }

    // Playback of recorded WAV broadcasts
    fun playRecording(recording: RadioRecording) {
        vibrateClick(25)
        stopRecordingPlayback()

        // Pause live radio playback
        if (_radioState.value.isPlaying) {
            _radioState.value = _radioState.value.copy(isPlaying = false)
            audioEngine.stop()
        }

        try {
            val player = MediaPlayer().apply {
                setDataSource(recording.filePath)
                prepare()
                start()
                setOnCompletionListener {
                    stopRecordingPlayback()
                }
            }
            recordingPlayer = player
            _playingRecordingId.value = recording.id

            recordingProgressJob?.cancel()
            recordingProgressJob = viewModelScope.launch {
                while (player.isPlaying) {
                    _recordingProgressSec.value = player.currentPosition / 1000
                    delay(500)
                }
            }
        } catch (_: Throwable) {
            stopRecordingPlayback()
        }
    }

    fun stopRecordingPlayback() {
        recordingProgressJob?.cancel()
        try {
            recordingPlayer?.stop()
            recordingPlayer?.release()
        } catch (_: Throwable) {}
        recordingPlayer = null
        _playingRecordingId.value = null
        _recordingProgressSec.value = 0
    }

    fun deleteRecording(recordingId: String) {
        if (_playingRecordingId.value == recordingId) {
            stopRecordingPlayback()
        }
        repository.deleteRecording(recordingId)
    }

    private fun vibrateClick(durationMs: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createOneShot(
                        durationMs.coerceAtLeast(10),
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Throwable) {}
    }

    override fun onCleared() {
        super.onCleared()
        try { audioEngine.stop() } catch (_: Throwable) {}
        try { stopRecordingPlayback() } catch (_: Throwable) {}
        scanJob?.cancel()
        sleepTimerJob?.cancel()
        recordingTimerJob?.cancel()
    }
}

enum class ScreenTab {
    TUNER,
    STATIONS,
    RECORDINGS,
    HARDWARE
}
