package com.example.sonicflow.data.local.DAO

import androidx.room.*
import com.example.sonicflow.data.local.Entity.PlaylistEntity
import com.example.sonicflow.data.local.Entity.PlaylistTrackCrossRef
import com.example.sonicflow.data.local.Entity.PlaylistWithTrackIds
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    // ==================== PLAYLIST OPERATIONS ====================

    @Insert
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Delete
    suspend fun deletePlaylist(playlist: PlaylistEntity)

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    suspend fun getPlaylistById(playlistId: Long): PlaylistEntity?

    @Query("SELECT * FROM playlists WHERE name = :name LIMIT 1")
    suspend fun getPlaylistByName(name: String): PlaylistEntity?

    @Query("SELECT COUNT(*) FROM playlists")
    suspend fun getPlaylistCount(): Int

    // ==================== TRACK-PLAYLIST OPERATIONS ====================

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTrackToPlaylist(crossRef: PlaylistTrackCrossRef)

    @Delete
    suspend fun removeTrackFromPlaylist(crossRef: PlaylistTrackCrossRef)

    @Query("DELETE FROM playlist_track_cross_ref WHERE playlistId = :playlistId")
    suspend fun removeAllTracksFromPlaylist(playlistId: Long)

    @Query("SELECT trackId FROM playlist_track_cross_ref WHERE playlistId = :playlistId ORDER BY addedAt DESC")
    fun getTrackIdsForPlaylist(playlistId: Long): Flow<List<Long>>

    @Query("SELECT trackId FROM playlist_track_cross_ref WHERE playlistId = :playlistId ORDER BY addedAt DESC")
    suspend fun getTrackIdsForPlaylistOnce(playlistId: Long): List<Long>

    @Transaction
    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    suspend fun getPlaylistWithTracks(playlistId: Long): PlaylistWithTrackIds?

    @Transaction
    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getAllPlaylistsWithTracks(): Flow<List<PlaylistWithTrackIds>>

    // ==================== TRACK COUNT & CHECKS ====================

    @Query("SELECT COUNT(*) FROM playlist_track_cross_ref WHERE playlistId = :playlistId")
    suspend fun getTrackCountInPlaylist(playlistId: Long): Int

    @Query("SELECT COUNT(*) FROM playlist_track_cross_ref WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun isTrackInPlaylist(playlistId: Long, trackId: Long): Int

    @Query("SELECT COUNT(*) > 0 FROM playlist_track_cross_ref WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun checkIfTrackInPlaylist(playlistId: Long, trackId: Long): Boolean

    // ==================== BULK OPERATIONS ====================

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTracksToPlaylist(crossRefs: List<PlaylistTrackCrossRef>)

    @Query("DELETE FROM playlist_track_cross_ref WHERE playlistId = :playlistId AND trackId IN (:trackIds)")
    suspend fun removeTracksFromPlaylist(playlistId: Long, trackIds: List<Long>)

    // ==================== UTILITY QUERIES ====================

    @Query("""
        SELECT p.* FROM playlists p
        WHERE (SELECT COUNT(*) FROM playlist_track_cross_ref WHERE playlistId = p.id) > 0
        ORDER BY p.createdAt DESC
    """)
    fun getNonEmptyPlaylists(): Flow<List<PlaylistEntity>>

    @Query("""
        SELECT p.* FROM playlists p
        WHERE (SELECT COUNT(*) FROM playlist_track_cross_ref WHERE playlistId = p.id) = 0
        ORDER BY p.createdAt DESC
    """)
    fun getEmptyPlaylists(): Flow<List<PlaylistEntity>>

    // ==================== SEARCH ====================

    @Query("SELECT * FROM playlists WHERE name LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchPlaylists(query: String): Flow<List<PlaylistEntity>>
}