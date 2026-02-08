package com.example.sonicflow.presentation.player

import com.example.sonicflow.domain.model.AudioTrack

data class PlayerState(
    val currentTrack: AudioTrack? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val waveformData: List<Float> = emptyList(),
    val isWaveformLoading: Boolean = false,
    val volume: Float = 0.7f
)