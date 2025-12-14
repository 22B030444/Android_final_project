package com.example.hearo.data.model

import android.os.Parcelable
import com.example.hearo.data.model.itunes.ITunesArtist
import kotlinx.parcelize.Parcelize


@Parcelize
data class UniversalArtist(
    val id: String,
    val name: String,
    val imageUrl: String?,
    val followersCount: Int,
    val monthlyListeners: String?,
    val genres: List<String>,
    val source: MusicSource
) : Parcelable {

    fun copyArtist(
        id: String = this.id,
        name: String = this.name,
        imageUrl: String? = this.imageUrl,
        followersCount: Int = this.followersCount,
        monthlyListeners: String? = this.monthlyListeners,
        genres: List<String> = this.genres,
        source: MusicSource = this.source
    ) = UniversalArtist(
        id = id,
        name = name,
        imageUrl = imageUrl,
        followersCount = followersCount,
        monthlyListeners = monthlyListeners,
        genres = genres,
        source = source
    )
}

fun ITunesArtist.toUniversalArtist(): UniversalArtist {
    return UniversalArtist(
        id = artistId.toString(),
        name = artistName,
        imageUrl = null, // Will be loaded separately
        followersCount = 0,
        monthlyListeners = null,
        genres = listOfNotNull(primaryGenreName),
        source = MusicSource.ITUNES
    )
}