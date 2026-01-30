package com.example.sonicflow.presentation.player

import com.example.sonicflow.domain.model.AudioTrack

data class PlayerState(
    val currentTrack: AudioTrack? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L
)