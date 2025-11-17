package com.example.hearo.data.model.auth

import com.google.gson.annotations.SerializedName

data class UserProfile(
    val id: String,

    @SerializedName("display_name")
    val displayName: String?,

    val email: String?,

    val images: List<SpotifyImage>?,

    val country: String?,

    val product: String? // premium, free, etc
)

data class SpotifyImage(
    val url: String,
    val height: Int?,
    val width: Int?
)