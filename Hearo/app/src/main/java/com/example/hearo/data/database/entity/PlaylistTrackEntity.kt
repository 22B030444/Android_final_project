package com.example.hearo.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "playlist_tracks",
    primaryKeys = ["playlistId", "trackId"],
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("playlistId"), Index("trackId")]
)
data class PlaylistTrackEntity(
    val playlistId: Long,
    val trackId: String,
    val trackName: String,
    val artistName: String,
    val albumName: String,
    val imageUrl: String?,
    val previewUrl: String?,
    val durationMs: Int,
    val source: String,
    val addedAt: Long = System.currentTimeMillis()
)


