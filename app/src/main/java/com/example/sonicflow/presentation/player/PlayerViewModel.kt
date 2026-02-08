package com.example.sonicflow.presentation.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonicflow.data.audio.WaveformExtractor
import com.example.sonicflow.data.repository.WaveformRepository
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
    private val musicPlayerRepository: MusicPlayerRepository,
    private val waveformRepository: WaveformRepository,
    application: Application
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    private val waveformCache = mutableMapOf<String, List<Float>>()
    private val waveformExtractor = WaveformExtractor(application.applicationContext)

    init {
        observePlayerState()
    }

    private fun observePlayerState() {
        viewModelScope.launch {
            musicPlayerRepository.currentTrack.collect { track ->
                _state.value = _state.value.copy(currentTrack = track)
                // Charger la waveform automatiquement quand la track change
                track?.let { loadWaveform(it.data) }
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

        viewModelScope.launch {
            musicPlayerRepository.volume.collect { volume ->
                _state.value = _state.value.copy(volume = volume)
            }
        }
    }

    private fun loadWaveform(audioPath: String) {
        viewModelScope.launch {
            //récupération depuis Room ou génération si nécessaire
            val waveform = waveformRepository.getWaveform(audioPath)
            _state.value = _state.value.copy(waveformData = waveform)
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

    fun setPlaylist(tracks: List<AudioTrack>, startIndex: Int) {
        musicPlayerRepository.setPlaylist(tracks, startIndex)
    }

    fun seekTo(position: Long) {
        musicPlayerRepository.seekTo(position)
    }

    fun setVolume(volume: Float) {
        musicPlayerRepository.setVolume(volume)
    }

    fun playNext() {
        musicPlayerRepository.playNext()
    }

    fun playPrevious() {
        musicPlayerRepository.playPrevious()
    }

    override fun onCleared() {
        super.onCleared()
        // Nettoyer le cache si trop gros
        if (waveformCache.size > 20) {
            waveformCache.clear()
        }
    }
}