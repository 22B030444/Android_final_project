package com.example.hearo.data.api

import com.example.hearo.data.model.auth.UserProfile
import com.example.hearo.data.model.spotify.SearchResponse
import com.example.hearo.data.model.spotify.Track
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface SpotifyApiService {

    // ========================================
    // USER PROFILE
    // ========================================

    @GET("me")
    suspend fun getUserProfile(): UserProfile

    // ========================================
    // SEARCH
    // ========================================

    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("type") type: String, // "track", "album", "artist" или "track,album"
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): SearchResponse

    // ========================================
    // LIKED SONGS (User's Saved Tracks)
    // ========================================

    @GET("me/tracks")
    suspend fun getSavedTracks(
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): SavedTracksResponse

    @PUT("me/tracks")
    suspend fun saveTracks(
        @Query("ids") trackIds: String // comma-separated IDs
    )

    @DELETE("me/tracks")
    suspend fun removeSavedTracks(
        @Query("ids") trackIds: String
    )

    @GET("me/tracks/contains")
    suspend fun checkSavedTracks(
        @Query("ids") trackIds: String
    ): List<Boolean>

    // ========================================
    // PLAYLISTS
    // ========================================

    @GET("me/playlists")
    suspend fun getUserPlaylists(
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): PlaylistsResponse

    // TODO: Добавим больше методов позже
}

// Временные response классы
data class SavedTracksResponse(
    val items: List<SavedTrackItem>,
    val total: Int
)

data class SavedTrackItem(
    val track: Track,
    val added_at: String
)

data class PlaylistsResponse(
    val items: List<PlaylistSimplified>,
    val total: Int
)

data class PlaylistSimplified(
    val id: String,
    val name: String,
    val images: List<com.example.hearo.data.model.spotify.SpotifyImage>?
)
