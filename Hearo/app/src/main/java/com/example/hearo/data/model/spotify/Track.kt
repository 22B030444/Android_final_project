package com.example.hearo.data.model.spotify

import com.google.gson.annotations.SerializedName

data class Track(
    val id: String,
    val name: String,
    val artists: List<Artist>,
    val album: Album,

    @SerializedName("duration_ms")
    val durationMs: Int,

    @SerializedName("preview_url")
    val previewUrl: String?,

    val uri: String, // spotify:track:xxx

    @SerializedName("external_urls")
    val externalUrls: ExternalUrls?
)

data class Artist(
    val id: String,
    val name: String,

    @SerializedName("external_urls")
    val externalUrls: ExternalUrls?
)

data class SpotifyImage(
    val url: String,
    val height: Int?,
    val width: Int?
)

data class ExternalUrls(
    val spotify: String?
)