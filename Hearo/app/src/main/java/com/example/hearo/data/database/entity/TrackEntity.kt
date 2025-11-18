package com.example.hearo.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "liked_tracks")
data class TrackEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val artistName: String,
    val albumName: String,
    val imageUrl: String?,
    val previewUrl: String?,
    val durationMs: Int,
    val spotifyUri: String,
    val addedAt: Long = System.currentTimeMillis()
)