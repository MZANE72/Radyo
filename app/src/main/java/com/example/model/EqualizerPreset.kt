package com.example.model

data class EqualizerPreset(
    val name: String,
    val bassBoost: Int,   // 0 .. 100
    val treble: Int,      // 0 .. 100
    val bands: List<Int>  // 5 bands (-10 to +10 dB): 60Hz, 230Hz, 910Hz, 3.6kHz, 14kHz
) {
    companion object {
        val PRESETS = listOf(
            EqualizerPreset("Düz (Flat)", bassBoost = 20, treble = 20, bands = listOf(0, 0, 0, 0, 0)),
            EqualizerPreset("Pop (Net Vokal)", bassBoost = 35, treble = 45, bands = listOf(2, 4, 3, 5, 4)),
            EqualizerPreset("Rock (Güçlü Bas)", bassBoost = 65, treble = 55, bands = listOf(6, 4, -1, 3, 5)),
            EqualizerPreset("Konuşma / Haber", bassBoost = 10, treble = 60, bands = listOf(-3, 1, 6, 4, 2)),
            EqualizerPreset("Elektronik / Dans", bassBoost = 80, treble = 50, bands = listOf(7, 5, 0, 4, 6)),
            EqualizerPreset("Akustik / Slow", bassBoost = 40, treble = 35, bands = listOf(3, 2, 2, 3, 3))
        )
    }
}
