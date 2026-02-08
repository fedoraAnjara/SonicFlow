package com.example.sonicflow.di

import android.app.Application
import android.content.Context
import android.media.MediaPlayer
import androidx.media3.common.util.UnstableApi
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.sonicflow.data.audio.WaveformExtractor
import com.example.sonicflow.data.local.AppDatabase
import com.example.sonicflow.data.local.DAO.PlaylistDao
import com.example.sonicflow.data.local.DAO.UserDao
import com.example.sonicflow.data.local.DAO.WaveformDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.example.sonicflow.data.repository.AudioRepositoryImpl
import com.example.sonicflow.domain.repository.AudioRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import com.example.sonicflow.domain.usecase.GetAudioTracksUseCase
import com.example.sonicflow.data.repository.MusicPlayerRepositoryImpl
import com.example.sonicflow.domain.repository.MusicPlayerRepository
import com.example.sonicflow.data.preferences.PlaybackPreferences

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
            CREATE TABLE IF NOT EXISTS waveforms (
                audioPath TEXT NOT NULL PRIMARY KEY,
                amplitudes TEXT NOT NULL,
                generatedAt INTEGER NOT NULL
            )
            """.trimIndent()
            )
        }
    }

    @Provides
    @Singleton
    fun providePlaybackPreferences(
        @ApplicationContext context: Context
    ): PlaybackPreferences {
        return PlaybackPreferences(context)
    }

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun providePlaylistDao(database: AppDatabase): PlaylistDao {
        return database.playlistDao()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    @Singleton
    fun provideAudioRepository(
        @ApplicationContext context: Context
    ): AudioRepository = AudioRepositoryImpl(context)

    @Provides
    @Singleton
    fun provideGetAudioTracksUseCase(
        repository: AudioRepository
    ): GetAudioTracksUseCase {
        return GetAudioTracksUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideWaveformDao(database: AppDatabase): WaveformDao {
        return database.waveformDao()
    }

    @Provides
    @Singleton
    fun provideMediaPlayer(): MediaPlayer {
        return MediaPlayer()
    }

    @UnstableApi
    @Provides
    @Singleton
    fun provideMusicPlayerRepository(
        mediaPlayer: MediaPlayer,
        @ApplicationContext context: Context,
        waveformExtractor: WaveformExtractor,
        playbackPreferences: PlaybackPreferences
    ): MusicPlayerRepository {
        return MusicPlayerRepositoryImpl(mediaPlayer, context, waveformExtractor,playbackPreferences)
    }
}