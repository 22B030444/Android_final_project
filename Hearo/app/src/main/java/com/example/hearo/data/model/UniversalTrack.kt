package com.example.hearo.data.model

import android.os.Parcelable
import com.example.hearo.data.model.itunes.ITunesTrack
import com.example.hearo.data.model.jamendo.JamendoTrack
import kotlinx.parcelize.Parcelize


@Parcelize
data class UniversalTrack(
    val id: String,
    val name: String,
    val artistName: String,
    val albumName: String,
    val imageUrl: String?,
    val previewUrl: String?,
    val downloadUrl: String?,
    val durationMs: Int,
    val source: MusicSource,
    val canDownloadFull: Boolean = false,
    val price: String? = null
) : Parcelable

enum class MusicSource {
    ITUNES,
    JAMENDO
}

fun ITunesTrack.toUniversalTrack(): UniversalTrack {
    return UniversalTrack(
        id = trackId.toString(),
        name = trackName,
        artistName = artistName,
        albumName = collectionName ?: "",
        imageUrl = getHighResArtwork(),
        previewUrl = previewUrl,
        downloadUrl = null,
        durationMs = trackTimeMillis ?: 30000,
        source = MusicSource.ITUNES,
        canDownloadFull = false,
        price = trackPrice?.let { "$$it" }
    )
}

fun JamendoTrack.toUniversalTrack(): UniversalTrack {
    return UniversalTrack(
        id = id,
        name = name,
        artistName = artistName,
        albumName = albumName,
        imageUrl = albumImage ?: image,
        previewUrl = audio,
        downloadUrl = if (audiodownloadAllowed) audiodownload else null,
        durationMs = duration * 1000,
        source = MusicSource.JAMENDO,
        canDownloadFull = audiodownloadAllowed,
        price = "Free"
    )
}