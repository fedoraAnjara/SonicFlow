package com.example.sonicflow.di

import android.content.Context
import com.example.sonicflow.data.audio.WaveformExtractor
import com.example.sonicflow.data.local.DAO.WaveformDao
import com.example.sonicflow.data.repository.WaveformRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AudioModule {

    @Provides
    @Singleton
    fun provideWaveformExtractor(
        @ApplicationContext context: Context
    ): WaveformExtractor {
        return WaveformExtractor(context)
    }

    @Suppress("unused")
    @Provides
    @Singleton
    fun provideWaveformRepository(
        waveformDao: WaveformDao,
        waveformExtractor: WaveformExtractor
    ): WaveformRepository {
        return WaveformRepository(waveformDao, waveformExtractor)
    }
}

