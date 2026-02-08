package com.example.sonicflow.data.local.Entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "waveforms")
data class WaveformEntity(
    @PrimaryKey
    val audioPath: String,
    val amplitudes: String,
    val generatedAt: Long = System.currentTimeMillis()
)