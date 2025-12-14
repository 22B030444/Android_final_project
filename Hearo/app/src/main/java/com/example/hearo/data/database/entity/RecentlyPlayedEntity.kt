package com.example.hearo.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recently_played")
data class RecentlyPlayedEntity(
    @PrimaryKey
    val trackId: String,
    val trackName: String,
    val artistName: String,
    val albumName: String,
    val imageUrl: String?,
    val previewUrl: String?,
    val durationMs: Int,
    val source: String,
    val playedAt: Long = System.currentTimeMillis()
)