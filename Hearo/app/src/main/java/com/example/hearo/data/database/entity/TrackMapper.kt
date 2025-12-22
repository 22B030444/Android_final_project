package com.example.hearo.data.database.entity

import com.example.hearo.data.model.spotify.Track

fun Track.toEntity(): TrackEntity {
    return TrackEntity(
        id = this.id,
        name = this.name,
        artistName = this.artists.joinToString(", ") { it.name },
        albumName = this.album.name,
        imageUrl = this.album.images.firstOrNull()?.url,
        previewUrl = this.previewUrl,
        durationMs = this.durationMs,
        spotifyUri = this.uri
    )
}

fun TrackEntity.toTrack(): Track {
    return Track(
        id = this.id,
        name = this.name,
        artists = listOf(
            com.example.hearo.data.model.spotify.Artist(
                id = "",
                name = this.artistName,
                externalUrls = null
            )
        ),
        album = com.example.hearo.data.model.spotify.Album(
            id = "",
            name = this.albumName,
            images = if (this.imageUrl != null) {
                listOf(
                    com.example.hearo.data.model.spotify.SpotifyImage(
                        url = this.imageUrl,
                        height = null,
                        width = null
                    )
                )
            } else {
                emptyList()
            },
            releaseDate = null
        ),
        durationMs = this.durationMs,
        previewUrl = this.previewUrl,
        uri = this.spotifyUri,
        externalUrls = null
    )
}