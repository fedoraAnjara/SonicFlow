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
import kotlinx.coroutines.flow.update
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

    // Gestion de la queue
    private val _queue = MutableStateFlow<List<AudioTrack>>(emptyList())
    val queue: StateFlow<List<AudioTrack>> = _queue.asStateFlow()

    // Queue originale pour le mode shuffle
    private var originalQueue: List<AudioTrack> = emptyList()

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

        viewModelScope.launch {
            musicPlayerRepository.waveformData.collect { waveform ->
                _state.update { it.copy(waveformData = waveform) }
            }
        }

        viewModelScope.launch {
            musicPlayerRepository.queue.collect { queue ->
                _state.update { it.copy(queue = queue) }
            }
        }

        viewModelScope.launch {
            musicPlayerRepository.repeatMode.collect { mode ->
                _state.update { it.copy(repeatMode = mode) }
            }
        }

        viewModelScope.launch {
            musicPlayerRepository.shuffleMode.collect { mode ->
                _state.update { it.copy(shuffleMode = mode) }
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

    /**
     * Définit la file d'attente de lecture
     */
    fun setQueue(tracks: List<AudioTrack>) {
        originalQueue = tracks
        _state.update { it.copy(queue = tracks) }
    }

    /**
     * Définit la file d'attente et commence la lecture à partir d'un index spécifique
     */
    fun setQueueAndPlay(tracks: List<AudioTrack>, startIndex: Int = 0) {
        originalQueue = tracks

        // Si shuffle est activé, mélanger la queue
        val finalQueue = if (_state.value.shuffleMode == ShuffleMode.ON) {
            val trackToPlay = tracks[startIndex]
            val otherTracks = tracks.filterIndexed { index, _ -> index != startIndex }.shuffled()
            listOf(trackToPlay) + otherTracks
        } else {
            tracks
        }

        musicPlayerRepository.setQueue(finalQueue)
        _state.update { it.copy(queue = finalQueue) }

        //Puis jouer la piste à l'index souhaité
        if (finalQueue.isNotEmpty()) {
            musicPlayerRepository.playTrack(finalQueue[0])
        }
    }

    /**
     * Joue le prochain morceau de la file d'attente
     */
    fun playNext() {
        // Déléguer directement au repository qui gère les modes
        musicPlayerRepository.playNext()
    }

    /**
     * Joue le morceau précédent de la file d'attente
     */
    fun playPrevious() {
        // Déléguer directement au repository qui gère les modes
        musicPlayerRepository.playPrevious()
    }

    /**
     * Change le mode de répétition
     */
    fun toggleRepeatMode() {
        val newMode = when (_state.value.repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        musicPlayerRepository.setRepeatMode(newMode)
    }

    /**
     * Active/désactive le mode shuffle
     */
    fun toggleShuffleMode() {
        val currentTrack = _state.value.currentTrack
        val currentQueue = _state.value.queue

        val newMode = if (_state.value.shuffleMode == ShuffleMode.OFF) {
            ShuffleMode.ON
        } else {
            ShuffleMode.OFF
        }

        // Informer le repository du changement
        musicPlayerRepository.setShuffleMode(newMode)

        // Appliquer le shuffle ou restaurer l'ordre original
        if (newMode == ShuffleMode.ON && currentQueue.isNotEmpty()) {
            // Garder la piste actuelle en premier
            val shuffledQueue = if (currentTrack != null) {
                val otherTracks = currentQueue.filter { it.id != currentTrack.id }.shuffled()
                listOf(currentTrack) + otherTracks
            } else {
                currentQueue.shuffled()
            }

            musicPlayerRepository.setQueue(shuffledQueue)
        } else if (newMode == ShuffleMode.OFF && originalQueue.isNotEmpty()) {
            // Restaurer l'ordre original
            musicPlayerRepository.setQueue(originalQueue)
        }
    }

    /**
     * Vide la file d'attente
     */
    fun clearQueue() {
        _state.update { it.copy(queue = emptyList()) }
        originalQueue = emptyList()
        musicPlayerRepository.setQueue(emptyList())
    }

    /**
     * Ajoute des pistes à la queue
     */
    fun addToQueue(tracks: List<AudioTrack>) {
        val newQueue = _state.value.queue + tracks
        _state.update { it.copy(queue = newQueue) }

        // Mettre à jour aussi la queue originale
        if (_state.value.shuffleMode == ShuffleMode.OFF) {
            originalQueue = newQueue
        } else {
            originalQueue = originalQueue + tracks
        }

        musicPlayerRepository.setQueue(newQueue)
    }

    /**
     * Retire une piste de la queue
     */
    fun removeFromQueue(track: AudioTrack) {
        val newQueue = _state.value.queue.filter { it.id != track.id }
        _state.update { it.copy(queue = newQueue) }

        // Mettre à jour aussi la queue originale
        originalQueue = originalQueue.filter { it.id != track.id }

        musicPlayerRepository.setQueue(newQueue)
    }

    override fun onCleared() {
        super.onCleared()
        // Nettoyer le cache si trop gros
        if (waveformCache.size > 20) {
            waveformCache.clear()
        }
    }
}