package com.example.sonicflow.domain.repository

import com.example.sonicflow.domain.model.AudioTrack
import com.example.sonicflow.presentation.player.RepeatMode
import com.example.sonicflow.presentation.player.ShuffleMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface MusicPlayerRepository {
    val currentTrack: StateFlow<AudioTrack?>
    val isPlaying: StateFlow<Boolean>
    val currentPosition: StateFlow<Long>
    val duration: StateFlow<Long>
    val waveformData: StateFlow<List<Float>>
    val volume: Flow<Float>
    val queue: StateFlow<List<AudioTrack>>
    val repeatMode: StateFlow<RepeatMode>
    val shuffleMode: StateFlow<ShuffleMode>
    val trackEnded: StateFlow<Boolean>

    suspend fun generateWaveform(audioPath: String)
    suspend fun restorePlaybackState(allTracks: List<AudioTrack>)

    fun playTrack(track: AudioTrack)
    fun pauseTrack()
    fun resume()
    fun setVolume(volume: Float)
    fun seekTo(position: Long)
    fun playNext()
    fun playPrevious()
    fun setPlaylist(tracks: List<AudioTrack>, startIndex: Int)
    fun setQueue(tracks: List<AudioTrack>)
    fun addToQueue(tracks: List<AudioTrack>)
    fun removeFromQueue(track: AudioTrack)
    fun clearQueue()
    fun setRepeatMode(mode: RepeatMode)
    fun setShuffleMode(mode: ShuffleMode)
}