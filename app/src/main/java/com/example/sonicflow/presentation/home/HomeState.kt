package com.example.sonicflow.presentation.home

import com.example.sonicflow.domain.model.AudioTrack

data class HomeState(
    val audioTracks: List<AudioTrack> = emptyList(),
    val filteredTracks: List<AudioTrack> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val sortType: SortType = SortType.TITLE_ASC
)

enum class SortType{
    TITLE_ASC,
    TITLE_DESC,
    ARTIST_ASC,


}