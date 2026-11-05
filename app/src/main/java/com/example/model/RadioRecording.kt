package com.example.model

data class RadioRecording(
    val id: String,
    val title: String,
    val frequency: Float,
    val stationName: String,
    val filePath: String,
    val durationSeconds: Int,
    val timestamp: Long,
    val fileSizeBytes: Long
)
