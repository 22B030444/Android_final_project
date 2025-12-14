package com.example.hearo.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloaded_tracks")
data class DownloadedTrackEntity(
    @PrimaryKey
    val trackId: String,
    val trackName: String,
    val artistName: String,
    val albumName: String,
    val imageUrl: String?,
    val localFilePath: String,
    val durationMs: Int,
    val source: String,
    val fileSize: Long,
    val isFull: Boolean,
    val downloadedAt: Long = System.currentTimeMillis()
)


