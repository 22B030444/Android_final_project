package com.example.hearo.data.model.itunes

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ITunesSearchResponse(
    val resultCount: Int,
    val results: List<ITunesTrack>
) : Parcelable

@Parcelize
data class ITunesTrack(
    val trackId: Long,
    val trackName: String,
    val artistName: String,
    val collectionName: String?, // Название альбома

    @SerializedName("artworkUrl100")
    val artworkUrl100: String?,

    @SerializedName("artworkUrl60")
    val artworkUrl60: String?,

    val previewUrl: String?, // Preview URL (30-90 сек)

    @SerializedName("trackTimeMillis")
    val trackTimeMillis: Int?, // Длительность в миллисекундах

    val primaryGenreName: String?,
    val releaseDate: String?,

    @SerializedName("trackPrice")
    val trackPrice: Double?,

    @SerializedName("collectionPrice")
    val collectionPrice: Double?,

    val country: String?,
    val currency: String?
) : Parcelable {

    /**
     * Получить обложку высокого качества (600x600)
     */
    fun getHighResArtwork(): String? {
        return artworkUrl100?.replace("100x100", "600x600")
    }
}