package com.example.sonicflow.domain.repository

import com.example.sonicflow.domain.model.AudioTrack
import kotlinx.coroutines.flow.StateFlow

interface MusicPlayerRepository{
    val currentTrack: StateFlow<AudioTrack?>
    val isPlaying: StateFlow<Boolean>
    val currentPosition: StateFlow<Long>
    val duration: StateFlow<Long>

    fun playTrack(track: AudioTrack)
    fun pauseTrack()
    fun resume()
    fun seekTo(position: Long)
    fun playNext()
    fun playPrevious()
    fun setPlaylist(tracks: List<AudioTrack>, startIndex: Int)
}