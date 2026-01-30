package com.example.sonicflow.presentation.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonicflow.domain.model.AudioTrack
import com.example.sonicflow.domain.repository.MusicPlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val musicPlayerRepository: MusicPlayerRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    init {
        observePlayerState()
    }

    private fun observePlayerState() {
        viewModelScope.launch {
            musicPlayerRepository.currentTrack.collect { track ->
                _state.value = _state.value.copy(currentTrack = track)
            }
        }

        viewModelScope.launch {
            musicPlayerRepository.isPlaying.collect { isPlaying ->
                _state.value = _state.value.copy(isPlaying = isPlaying)
            }
        }

        viewModelScope.launch {
            musicPlayerRepository.currentPosition.collect { position ->
                _state.value = _state.value.copy(currentPosition = position)
            }
        }

        viewModelScope.launch {
            musicPlayerRepository.duration.collect { duration ->
                _state.value = _state.value.copy(duration = duration)
            }
        }
    }

    fun playTrack(track: AudioTrack) {
        musicPlayerRepository.playTrack(track)
    }

    fun togglePlayPause() {
        if (_state.value.isPlaying) {
            musicPlayerRepository.pauseTrack()
        } else {
            musicPlayerRepository.resume()
        }
    }

    fun seekTo(position: Long) {
        musicPlayerRepository.seekTo(position)
    }

    fun playNext() {
        musicPlayerRepository.playNext()
    }

    fun playPrevious() {
        musicPlayerRepository.playPrevious()
    }
}