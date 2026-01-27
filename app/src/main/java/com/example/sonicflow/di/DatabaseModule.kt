package com.example.sonicflow.di

import android.app.Application
import androidx.room.Room
import com.example.sonicflow.data.local.AppDatabase
import com.example.sonicflow.data.local.DAO.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule{
    @Provides
    @Singleton
    fun provideDatabase(app: Application): AppDatabase {
        return Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            "app_database"
        ).build()
    }
    @Provides
    @Singleton
    fun provideUserDao(database: AppDatabase): UserDao{
        return database.userDao()
    }
}