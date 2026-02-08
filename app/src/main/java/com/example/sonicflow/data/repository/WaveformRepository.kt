package com.example.sonicflow.data.repository

import com.example.sonicflow.data.audio.WaveformExtractor
import com.example.sonicflow.data.local.DAO.WaveformDao
import com.example.sonicflow.data.local.Entity.WaveformEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WaveformRepository @Inject constructor(
    private val waveformDao: WaveformDao,
    private val waveformExtractor: WaveformExtractor
) {

    suspend fun getWaveform(audioPath: String): List<Float> = withContext(Dispatchers.IO) {
        // 1. Vérifier si déjà en cache Room
        val cached = waveformDao.getWaveform(audioPath)
        if (cached != null) {
            return@withContext parseAmplitudes(cached.amplitudes)
        }

        // 2. Sinon, générer avec MediaExtractor
        val waveform = waveformExtractor.extractWaveform(audioPath, targetSamples = 100)

        // 3. Sauvegarder dans Room
        val entity = WaveformEntity(
            audioPath = audioPath,
            amplitudes = waveform.toJsonString()
        )
        waveformDao.insertWaveform(entity)

        return@withContext waveform
    }

    private fun List<Float>.toJsonString(): String {
        val jsonArray = JSONArray()
        forEach { jsonArray.put(it) }
        return jsonArray.toString()
    }

    private fun parseAmplitudes(json: String): List<Float> {
        val jsonArray = JSONArray(json)
        val list = mutableListOf<Float>()
        for (i in 0 until jsonArray.length()) {
            list.add(jsonArray.getDouble(i).toFloat())
        }
        return list
    }

    suspend fun clearCache() {
        waveformDao.clearAll()
    }
}