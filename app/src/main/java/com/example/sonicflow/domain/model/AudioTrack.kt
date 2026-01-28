package com.example.sonicflow.domain.model

data class AudioTrack(
    val id:Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val data: String
)