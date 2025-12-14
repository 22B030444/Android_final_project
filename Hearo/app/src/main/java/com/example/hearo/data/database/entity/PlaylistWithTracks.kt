package com.example.hearo.data.database.entity

import androidx.room.Embedded
import androidx.room.Relation

data class PlaylistWithTracks(
    @Embedded
    val playlist: PlaylistEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "playlistId"
    )
    val tracks: List<PlaylistTrackEntity>
)


