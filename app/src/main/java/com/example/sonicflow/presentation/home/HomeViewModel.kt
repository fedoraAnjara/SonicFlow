package com.example.sonicflow.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonicflow.domain.usecase.GetAudioTracksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.sonicflow.domain.model.AudioTrack

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAudioTracksUseCase: GetAudioTracksUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        loadAudioTracks()
    }

    fun loadAudioTracks() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            try {
                val tracks = getAudioTracksUseCase()
                _state.value = _state.value.copy(
                    audioTracks = tracks,
                    filteredTracks = tracks,  // Au début, filteredTracks = tous
                    isLoading = false
                )
                // Appliquer le tri actuel
                applySortAndFilter()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Erreur inconnue"
                )
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        applySortAndFilter()
    }

    fun onSortTypeChange(sortType: SortType) {
        _state.value = _state.value.copy(sortType = sortType)
        applySortAndFilter()
    }

    private fun applySortAndFilter() {
        val currentState = _state.value
        var filtered = currentState.audioTracks

        if (currentState.searchQuery.isNotEmpty()) {
            filtered = filtered.filter { track ->
                track.title.contains(currentState.searchQuery, ignoreCase = true) ||
                track.artist.contains(currentState.searchQuery, ignoreCase = true) ||
                track.album.contains(currentState.searchQuery, ignoreCase = true)
            }
        }
        filtered = when (currentState.sortType) {
            SortType.TITLE_ASC -> filtered.sortedBy { it.title.lowercase() }
            SortType.TITLE_DESC -> filtered.sortedByDescending { it.title.lowercase() }
            SortType.ARTIST_ASC -> filtered.sortedBy { it.artist.lowercase() }
        }

        _state.value = _state.value.copy(filteredTracks = filtered)
    }

}