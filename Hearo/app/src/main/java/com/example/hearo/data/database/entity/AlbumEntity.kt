package com.example.hearo.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_albums")
data class AlbumEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val artistName: String,
    val artistId: String?,
    val imageUrl: String?,
    val releaseDate: String?,
    val totalTracks: Int,
    val albumType: String?, // album, single, compilation
    val addedAt: Long = System.currentTimeMillis()
)
