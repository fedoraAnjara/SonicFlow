package com.example.sonicflow.domain.repository

import com.example.sonicflow.domain.model.AudioTrack
import com.example.sonicflow.domain.model.Playlist
import com.example.sonicflow.domain.model.PlaylistWithTracks
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    fun getAllPlaylists(): Flow<List<Playlist>>
    suspend fun createPlaylist(name: String): Long
    suspend fun deletePlaylist(playlistId: Long)
    suspend fun renamePlaylist(playlistId: Long, newName: String)
    suspend fun addTrackToPlaylist(playlistId: Long, trackId: Long)
    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long)
    fun getPlaylistWithTracks(playlistId: Long): Flow<PlaylistWithTracks?>
    fun getTrackIdsForPlaylist(playlistId: Long): Flow<List<Long>>
}