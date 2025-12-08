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
data class ITunesArtistSearchResponse(
    val resultCount: Int,
    val results: List<ITunesArtist>
) : Parcelable

@Parcelize
data class ITunesAlbumSearchResponse(
    val resultCount: Int,
    val results: List<ITunesAlbum>
) : Parcelable

@Parcelize
data class ITunesTrack(
    val trackId: Long,
    val trackName: String,
    val artistName: String,
    val artistId: Long?,
    val collectionName: String?, // Album name
    val collectionId: Long?,

    @SerializedName("artworkUrl100")
    val artworkUrl100: String?,

    @SerializedName("artworkUrl60")
    val artworkUrl60: String?,

    val previewUrl: String?, // Preview URL (30-90 sec)

    @SerializedName("trackTimeMillis")
    val trackTimeMillis: Int?, // Duration in milliseconds

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
     * Get high quality artwork (600x600)
     */
    fun getHighResArtwork(): String? {
        return artworkUrl100?.replace("100x100", "600x600")
    }
}

@Parcelize
data class ITunesArtist(
    val artistId: Long,
    val artistName: String,
    val artistLinkUrl: String?,
    val primaryGenreName: String?,
    val primaryGenreId: Int?
) : Parcelable

@Parcelize
data class ITunesAlbum(
    val collectionId: Long,
    val collectionName: String,
    val artistName: String,
    val artistId: Long?,

    @SerializedName("artworkUrl100")
    val artworkUrl100: String?,

    @SerializedName("artworkUrl60")
    val artworkUrl60: String?,

    val collectionType: String?, // Album, EP, etc.
    val trackCount: Int?,
    val releaseDate: String?,
    val primaryGenreName: String?,

    @SerializedName("collectionPrice")
    val collectionPrice: Double?,

    val country: String?,
    val currency: String?
) : Parcelable {

    /**
     * Get high quality artwork (600x600)
     */
    fun getHighResArtwork(): String? {
        return artworkUrl100?.replace("100x100", "600x600")
    }
}

/**
 * Response for looking up artist's tracks/albums
 */
@Parcelize
data class ITunesLookupResponse(
    val resultCount: Int,
    val results: List<ITunesLookupResult>
) : Parcelable

@Parcelize
data class ITunesLookupResult(
    val wrapperType: String, // "artist", "track", "collection"

    // Artist fields
    val artistId: Long?,
    val artistName: String?,
    val artistLinkUrl: String?,

    // Track fields
    val trackId: Long?,
    val trackName: String?,
    val previewUrl: String?,
    @SerializedName("trackTimeMillis")
    val trackTimeMillis: Int?,

    // Album/Collection fields
    val collectionId: Long?,
    val collectionName: String?,
    @SerializedName("artworkUrl100")
    val artworkUrl100: String?,
    val trackCount: Int?,
    val releaseDate: String?,

    val primaryGenreName: String?
) : Parcelable {

    fun getHighResArtwork(): String? {
        return artworkUrl100?.replace("100x100", "600x600")
    }
}
