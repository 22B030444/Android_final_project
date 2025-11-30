package com.example.hearo.data.model.spotify

data class SearchResponse(
    val tracks: TrackSearchResult?,
    val albums: AlbumSearchResult?,
    val artists: ArtistSearchResult?
)

data class TrackSearchResult(
    val items: List<Track>,
    val total: Int,
    val limit: Int,
    val offset: Int
)


data class AlbumSearchResult(
    val items: List<AlbumFull>,
    val total: Int,
    val limit: Int,
    val offset: Int
)

data class ArtistSearchResult(
    val items: List<ArtistFull>,
    val total: Int,
    val limit: Int,
    val offset: Int
)