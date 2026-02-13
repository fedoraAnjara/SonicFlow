package com.example.sonicflow.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.sonicflow.data.local.DAO.UserDao
import com.example.sonicflow.data.local.DAO.WaveformDao
import com.example.sonicflow.data.local.DAO.PlaylistDao
import com.example.sonicflow.data.local.Entity.UserEntity
import com.example.sonicflow.data.local.Entity.WaveformEntity
import com.example.sonicflow.data.local.Entity.PlaylistEntity
import com.example.sonicflow.data.local.Entity.PlaylistTrackCrossRef

@Database(
    entities = [
        UserEntity::class,
        WaveformEntity::class,
        PlaylistEntity::class,
        PlaylistTrackCrossRef::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun waveformDao(): WaveformDao
    abstract fun playlistDao(): PlaylistDao
}