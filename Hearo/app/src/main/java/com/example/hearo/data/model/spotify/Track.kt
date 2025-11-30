package com.example.hearo.data.model.spotify

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
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
) : Parcelable


@Parcelize
data class SpotifyImage(
    val url: String,
    val height: Int?,
    val width: Int?
) : Parcelable

