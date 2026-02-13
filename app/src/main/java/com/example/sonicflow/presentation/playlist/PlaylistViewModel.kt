package com.example.sonicflow.presentation.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonicflow.domain.model.AudioTrack
import com.example.sonicflow.domain.model.Playlist
import com.example.sonicflow.domain.model.PlaylistWithTracks
import com.example.sonicflow.domain.repository.AudioRepository
import com.example.sonicflow.domain.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val audioRepository: AudioRepository
) : ViewModel() {

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    private val _allTracks = MutableStateFlow<List<AudioTrack>>(emptyList())
    val allTracks: StateFlow<List<AudioTrack>> = _allTracks.asStateFlow()

    init {
        loadPlaylists()
        loadAllTracks()
    }

    private fun loadPlaylists() {
        viewModelScope.launch {
            playlistRepository.getAllPlaylists().collect { playlists ->
                _playlists.value = playlists
            }
        }
    }

    private fun loadAllTracks() {
        viewModelScope.launch {
            _allTracks.value = audioRepository.getAudioTracks()
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            playlistRepository.createPlaylist(name)
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            playlistRepository.deletePlaylist(playlistId)
        }
    }

    fun renamePlaylist(playlistId: Long, newName: String) {
        viewModelScope.launch {
            playlistRepository.renamePlaylist(playlistId, newName)
        }
    }

    fun addTrackToPlaylist(playlistId: Long, trackId: Long) {
        viewModelScope.launch {
            playlistRepository.addTrackToPlaylist(playlistId, trackId)
        }
    }

    fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) {
        viewModelScope.launch {
            playlistRepository.removeTrackFromPlaylist(playlistId, trackId)
        }
    }

    fun getPlaylistWithTracks(playlistId: Long): Flow<PlaylistWithTracks?> {
        return playlistRepository.getPlaylistWithTracks(playlistId)
    }
}