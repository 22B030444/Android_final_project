package com.example.hearo.data.model

import android.os.Parcelable
import com.example.hearo.data.model.itunes.ITunesAlbum
import kotlinx.parcelize.Parcelize


@Parcelize
data class UniversalAlbum(
    val id: String,
    val name: String,
    val artistName: String,
    val artistId: String?,
    val imageUrl: String?,
    val releaseDate: String?,
    val totalTracks: Int,
    val albumType: String?,
    val source: MusicSource,
    val tracks: List<UniversalTrack> = emptyList()
) : Parcelable


fun ITunesAlbum.toUniversalAlbum(): UniversalAlbum {
    return UniversalAlbum(
        id = collectionId.toString(),
        name = collectionName,
        artistName = artistName,
        artistId = artistId?.toString(),
        imageUrl = getHighResArtwork(),
        releaseDate = releaseDate?.take(10),
        totalTracks = trackCount ?: 0,
        albumType = collectionType,
        source = MusicSource.ITUNES
    )
}
