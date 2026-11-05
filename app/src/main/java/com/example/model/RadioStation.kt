package com.example.model

data class RadioStation(
    val id: String,
    val frequency: Float,          // in MHz e.g. 100.0f
    val name: String,               // e.g. "Power FM"
    val genre: String,              // e.g. "Hit / Dance"
    val location: String = "Ulusal",
    val description: String = "",
    val currentProgram: String = "Canlı Yayın",
    val isFavorite: Boolean = false,
    val isCustom: Boolean = false,
    val signalStrengthDbm: Int = -55 // -30 dBm (strongest) to -95 dBm (faint)
)
