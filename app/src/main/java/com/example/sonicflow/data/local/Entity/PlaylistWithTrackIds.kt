package com.example.sonicflow.data.local.Entity

import androidx.room.Embedded
import androidx.room.Relation

data class PlaylistWithTrackIds(
    @Embedded val playlist: PlaylistEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "playlistId"
    )
    val trackCrossRefs: List<PlaylistTrackCrossRef>
)