package com.example.sonicflow.data.local.DAO

import androidx.room.*
import com.example.sonicflow.data.local.Entity.WaveformEntity

@Dao
interface WaveformDao {

    @Query("SELECT * FROM waveforms WHERE audioPath = :audioPath LIMIT 1")
    suspend fun getWaveform(audioPath: String): WaveformEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaveform(waveform: WaveformEntity)

    @Query("DELETE FROM waveforms WHERE audioPath = :audioPath")
    suspend fun deleteWaveform(audioPath: String)

    @Query("DELETE FROM waveforms")
    suspend fun clearAll()
}