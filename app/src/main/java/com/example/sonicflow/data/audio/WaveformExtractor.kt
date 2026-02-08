package com.example.sonicflow.data.audio

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs
import kotlin.math.sqrt

class WaveformExtractor(private val context: Context) {

    suspend fun extractWaveform(
        audioPath: String,
        targetSamples: Int = 100
    ): List<Float> = withContext(Dispatchers.IO) {

        val extractor = MediaExtractor()
        var codec: MediaCodec? = null

        try {
            extractor.setDataSource(audioPath)

            var audioTrackIndex = -1
            var format: MediaFormat? = null

            for (i in 0 until extractor.trackCount) {
                val trackFormat = extractor.getTrackFormat(i)
                val mime = trackFormat.getString(MediaFormat.KEY_MIME) ?: ""
                if (mime.startsWith("audio/")) {
                    audioTrackIndex = i
                    format = trackFormat
                    break
                }
            }

            if (audioTrackIndex == -1 || format == null) {
                return@withContext List(targetSamples) { 0.5f }
            }

            extractor.selectTrack(audioTrackIndex)

            // OPTIMISATION 1: Extraire seulement les 30 premières secondes
            val durationUs = format.getLong(MediaFormat.KEY_DURATION)
            val maxDurationUs = 30_000_000L // 30 secondes
            val targetDurationUs = minOf(durationUs, maxDurationUs)

            val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
            codec = MediaCodec.createDecoderByType(mime)
            codec.configure(format, null, null, 0)
            codec.start()

            val amplitudes = mutableListOf<Float>()
            var isInputDone = false
            var isOutputDone = false
            val bufferInfo = MediaCodec.BufferInfo()

            while (!isOutputDone) {
                if (!isInputDone) {
                    val inputBufferId = codec.dequeueInputBuffer(5_000) // Réduit à 5ms
                    if (inputBufferId >= 0) {
                        val inputBuffer = codec.getInputBuffer(inputBufferId)
                        inputBuffer?.clear()

                        val sampleSize = extractor.readSampleData(inputBuffer!!, 0)
                        val sampleTime = extractor.sampleTime

                        // OPTIMISATION 2: Arrêter après 30 secondes
                        if (sampleSize < 0 || sampleTime >= targetDurationUs) {
                            codec.queueInputBuffer(
                                inputBufferId, 0, 0, 0,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM
                            )
                            isInputDone = true
                        } else {
                            codec.queueInputBuffer(
                                inputBufferId, 0, sampleSize,
                                sampleTime, 0
                            )
                            extractor.advance()
                        }
                    }
                }

                val outputBufferId = codec.dequeueOutputBuffer(bufferInfo, 5_000)
                if (outputBufferId >= 0) {
                    val outputBuffer = codec.getOutputBuffer(outputBufferId)

                    if (bufferInfo.size > 0 && outputBuffer != null) {
                        outputBuffer.position(bufferInfo.offset)
                        outputBuffer.limit(bufferInfo.offset + bufferInfo.size)
                        outputBuffer.order(ByteOrder.LITTLE_ENDIAN)

                        val shortBuffer = outputBuffer.asShortBuffer()

                        // OPTIMISATION 3: Chunks plus grands = moins d'itérations
                        val chunkSize = 4096 // Doublé
                        var sumSquares = 0.0
                        var count = 0

                        while (shortBuffer.hasRemaining()) {
                            val sample = shortBuffer.get().toDouble()
                            sumSquares += sample * sample
                            count++

                            if (count >= chunkSize) {
                                val rms = sqrt(sumSquares / count).toFloat()
                                amplitudes.add(rms)
                                sumSquares = 0.0
                                count = 0
                            }
                        }

                        if (count > 0) {
                            val rms = sqrt(sumSquares / count).toFloat()
                            amplitudes.add(rms)
                        }
                    }

                    codec.releaseOutputBuffer(outputBufferId, false)

                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                        isOutputDone = true
                    }
                }
            }

            if (amplitudes.isEmpty()) {
                return@withContext List(targetSamples) { 0.5f }
            }

            val maxAmplitude = amplitudes.maxOrNull() ?: 1f
            val step = amplitudes.size.toFloat() / targetSamples

            val downsampled = List(targetSamples) { i ->
                val startIdx = (i * step).toInt()
                val endIdx = ((i + 1) * step).toInt().coerceAtMost(amplitudes.size)

                val segment = amplitudes.subList(startIdx, endIdx)
                val peak = segment.maxOrNull() ?: 0f

                val normalized = peak / maxAmplitude
                val enhanced = normalized * normalized * normalized
                enhanced.coerceIn(0.02f, 1f)
            }

            return@withContext downsampled

        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext List(targetSamples) { 0.5f }
        } finally {
            codec?.stop()
            codec?.release()
            extractor.release()
        }
    }
}