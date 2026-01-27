package com.example.sonicflow.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.sonicflow.data.local.DAO.UserDao
import com.example.sonicflow.data.local.Entity.UserEntity

@Database(
    entities = [UserEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}