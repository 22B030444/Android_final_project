package com.example.hearo.data.model.spotify

data class SearchResponse(
    val tracks: TrackSearchResult?
)

data class TrackSearchResult(
    val items: List<Track>,
    val total: Int,
    val limit: Int,
    val offset: Int
)