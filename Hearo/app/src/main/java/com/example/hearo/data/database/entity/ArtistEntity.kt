package com.example.hearo.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "followed_artists")
data class ArtistEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val imageUrl: String?,
    val followersCount: Int,
    val genres: String?,
    val monthlyListeners: String?,
    val addedAt: Long = System.currentTimeMillis()
)
