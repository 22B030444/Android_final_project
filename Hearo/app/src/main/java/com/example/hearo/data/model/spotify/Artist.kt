package com.example.hearo.data.model.spotify

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * Полная модель артиста для результатов поиска
 */
@Parcelize
data class ArtistFull(
    val id: String,
    val name: String,
    val images: List<SpotifyImage>?,

    @SerializedName("followers")
    val followers: Followers?,

    val genres: List<String>?,

    val popularity: Int?,

    val uri: String,

    @SerializedName("external_urls")
    val externalUrls: ExternalUrls?
) : Parcelable

@Parcelize
data class Followers(
    val total: Int
) : Parcelable

/**
 * Упрощенная модель артиста (используется в Track)
 */
@Parcelize
data class Artist(
    val id: String,
    val name: String,

    @SerializedName("external_urls")
    val externalUrls: ExternalUrls?
) : Parcelable

@Parcelize
data class ExternalUrls(
    val spotify: String?
) : Parcelable