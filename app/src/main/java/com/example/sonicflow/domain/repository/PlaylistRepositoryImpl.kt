package com.example.sonicflow.data.repository

import com.example.sonicflow.data.local.DAO.PlaylistDao
import com.example.sonicflow.data.local.Entity.PlaylistEntity
import com.example.sonicflow.data.local.Entity.PlaylistTrackCrossRef
import com.example.sonicflow.domain.model.AudioTrack
import com.example.sonicflow.domain.model.Playlist
import com.example.sonicflow.domain.model.PlaylistWithTracks
import com.example.sonicflow.domain.repository.AudioRepository
import com.example.sonicflow.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PlaylistRepositoryImpl @Inject constructor(
    private val playlistDao: PlaylistDao,
    private val audioRepository: AudioRepository
) : PlaylistRepository {

    override fun getAllPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylists().map { entities ->
            entities.map { entity ->
                // Récupérer le nombre de pistes pour chaque playlist
                val trackIds = playlistDao.getTrackIdsForPlaylistOnce (entity.id)
                Playlist(
                    id = entity.id,
                    name = entity.name,
                    createdAt = entity.createdAt,
                    trackCount = trackIds.size
                )
            }
        }
    }

    override suspend fun createPlaylist(name: String): Long {
        val playlist = PlaylistEntity(name = name)
        return playlistDao.insertPlaylist(playlist)
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        val playlist = playlistDao.getPlaylistById(playlistId)
        playlist?.let {
            playlistDao.deletePlaylist(it)
        }
    }

    override suspend fun renamePlaylist(playlistId: Long, newName: String) {
        val playlist = playlistDao.getPlaylistById(playlistId)
        playlist?.let {
            playlistDao.updatePlaylist(it.copy(name = newName))
        }
    }

    override suspend fun addTrackToPlaylist(playlistId: Long, trackId: Long) {
        val crossRef = PlaylistTrackCrossRef(playlistId, trackId)
        playlistDao.addTrackToPlaylist(crossRef)
    }

    override suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) {
        val crossRef = PlaylistTrackCrossRef(playlistId, trackId)
        playlistDao.removeTrackFromPlaylist(crossRef)
    }

    override fun getPlaylistWithTracks(playlistId: Long): Flow<PlaylistWithTracks?> {
        return flow {
            val playlist = playlistDao.getPlaylistById(playlistId)
            if (playlist != null) {
                playlistDao.getTrackIdsForPlaylist(playlistId).collect { trackIds ->
                    val allAudioTracks = audioRepository.getAudioTracks()
                    val tracks = allAudioTracks.filter { audioTrack ->
                        audioTrack.id in trackIds
                    }
                    emit(
                        PlaylistWithTracks(
                            playlist = Playlist(
                                id = playlist.id,
                                name = playlist.name,
                                createdAt = playlist.createdAt,
                                trackCount = tracks.size
                            ),
                            tracks = tracks
                        )
                    )
                }
            } else {
                emit(null)
            }
        }
    }

    override fun getTrackIdsForPlaylist(playlistId: Long): Flow<List<Long>> {
        return playlistDao.getTrackIdsForPlaylist(playlistId)
    }
}