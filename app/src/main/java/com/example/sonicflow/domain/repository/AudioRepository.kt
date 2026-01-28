package  com.example.sonicflow.domain.repository

import com.example.sonicflow.domain.model.AudioTrack

interface AudioRepository{
    suspend fun getAudioTracks(): List<AudioTrack>
}