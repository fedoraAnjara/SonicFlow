package com.example.sonicflow.domain.model

import android.provider.MediaStore

data class PlaylistWithTracks(
    val playlist: Playlist,
    val tracks: List<AudioTrack>
)