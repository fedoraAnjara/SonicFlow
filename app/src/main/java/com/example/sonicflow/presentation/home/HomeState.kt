package com.example.sonicflow.presentation.home

import com.example.sonicflow.domain.model.AudioTrack

data class HomeState(
    val audioTracks: List<AudioTrack> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)