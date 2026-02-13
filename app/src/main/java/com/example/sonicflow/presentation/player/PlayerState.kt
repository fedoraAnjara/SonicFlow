package com.example.sonicflow.presentation.player

import com.example.sonicflow.domain.model.AudioTrack

data class PlayerState(
    val currentTrack: AudioTrack? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val volume: Float = 1.0f,
    val waveformData: List<Float> = emptyList(),
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val shuffleMode: ShuffleMode = ShuffleMode.OFF,
    val queue: List<AudioTrack> = emptyList()
)
enum class RepeatMode {
    OFF,
    ALL,
    ONE
}

enum class ShuffleMode {
    OFF,
    ON
}