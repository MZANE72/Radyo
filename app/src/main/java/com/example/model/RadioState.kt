package com.example.model

data class RadioState(
    val isPoweredOn: Boolean = true,
    val isPlaying: Boolean = false,
    val currentFrequency: Float = 92.4f,          // Default TRT FM
    val activeStation: RadioStation? = null,
    val isRecording: Boolean = false,
    val recordingDurationSeconds: Int = 0,
    val volume: Float = 0.85f,                    // 0.0 to 1.0
    val isMuted: Boolean = false,
    val isStereo: Boolean = true,
    val squelchEnabled: Boolean = true,           // Suppress static hiss when not tuned to station
    val internalAntennaBoost: Boolean = true,     // DSP software antenna booster for headphone-free listening
    val sleepTimerMinutesLeft: Int? = null,
    val signalBars: Int = 4,                      // 0 to 5 bars
    val signalDbm: Int = -58,                     // dBm
    val isScanning: Boolean = false,
    val scanDirectionForward: Boolean = true,
    val activeEqualizer: EqualizerPreset = EqualizerPreset.PRESETS[0],
    val audioSpectrumLevels: List<Float> = List(16) { 0.1f }
)
