package com.example.hearo.data.model

import android.os.Parcelable
import com.example.hearo.data.model.itunes.ITunesArtist
import kotlinx.parcelize.Parcelize

/**
 * Universal artist model for working with different sources
 */
@Parcelize
data class UniversalArtist(
    val id: String,
    val name: String,
    val imageUrl: String?,
    val followersCount: Int,
    val monthlyListeners: String?,
    val genres: List<String>,
    val source: MusicSource
) : Parcelable

/**
 * Converters
 */
fun ITunesArtist.toUniversalArtist(): UniversalArtist {
    return UniversalArtist(
        id = artistId.toString(),
        name = artistName,
        imageUrl = null, // iTunes doesn't provide artist images in search
        followersCount = 0,
        monthlyListeners = null,
        genres = listOfNotNull(primaryGenreName),
        source = MusicSource.ITUNES
    )
}
