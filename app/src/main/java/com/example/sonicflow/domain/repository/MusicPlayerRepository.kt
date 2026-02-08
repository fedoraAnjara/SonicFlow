package com.example.sonicflow.domain.repository

import com.example.sonicflow.domain.model.AudioTrack
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface MusicPlayerRepository{
    val currentTrack: StateFlow<AudioTrack?>
    val isPlaying: StateFlow<Boolean>
    val currentPosition: StateFlow<Long>
    val duration: StateFlow<Long>
    val waveformData: StateFlow<List<Float>>
    val volume: Flow<Float>
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
}