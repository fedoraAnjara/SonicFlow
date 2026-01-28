package com.example.sonicflow.domain.usecase

import com.example.sonicflow.domain.model.AudioTrack
import com.example.sonicflow.domain.repository.AudioRepository

class GetAudioTracksUseCase(private val audioRepository: AudioRepository) {
    suspend operator fun invoke(): List<AudioTrack> {
        return audioRepository.getAudioTracks()
    }
}