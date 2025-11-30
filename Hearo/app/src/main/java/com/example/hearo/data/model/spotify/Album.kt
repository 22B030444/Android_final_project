package com.example.hearo.data.model.spotify

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * Полная модель альбома для результатов поиска
 */
data class AlbumFull(
    val id: String,
    val name: String,
    val artists: List<Artist>,
    val images: List<SpotifyImage>,

    @SerializedName("release_date")
    val releaseDate: String?,

    @SerializedName("total_tracks")
    val totalTracks: Int?,

    @SerializedName("album_type")
    val albumType: String?, // album, single, compilation

    val uri: String,

    @SerializedName("external_urls")
    val externalUrls: ExternalUrls?
)

/**
 * Упрощенная модель альбома (используется в Track)
 */
@Parcelize
data class Album(
    val id: String,
    val name: String,
    val images: List<SpotifyImage>,

    @SerializedName("release_date")
    val releaseDate: String?
) : Parcelable