package com.example.hearo.data.api

import com.example.hearo.data.model.itunes.ITunesAlbumSearchResponse
import com.example.hearo.data.model.itunes.ITunesArtistSearchResponse
import com.example.hearo.data.model.itunes.ITunesLookupResponse
import com.example.hearo.data.model.itunes.ITunesSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ITunesApiService {

    /**
     * Search music in iTunes (tracks)
     */
    @GET("search")
    suspend fun searchMusic(
        @Query("term") term: String,
        @Query("entity") entity: String = "song",
        @Query("limit") limit: Int = 25,
        @Query("country") country: String = "us",
        @Query("media") media: String = "music"
    ): ITunesSearchResponse

    /**
     * Search albums
     */
    @GET("search")
    suspend fun searchAlbums(
        @Query("term") term: String,
        @Query("entity") entity: String = "album",
        @Query("limit") limit: Int = 25,
        @Query("country") country: String = "us",
        @Query("media") media: String = "music"
    ): ITunesAlbumSearchResponse

    /**
     * Search artists
     */
    @GET("search")
    suspend fun searchArtists(
        @Query("term") term: String,
        @Query("entity") entity: String = "musicArtist",
        @Query("limit") limit: Int = 25,
        @Query("country") country: String = "us"
    ): ITunesArtistSearchResponse

    /**
     * Lookup artist by ID to get their info and top songs
     */
    @GET("lookup")
    suspend fun lookupArtist(
        @Query("id") artistId: Long,
        @Query("entity") entity: String = "song",
        @Query("limit") limit: Int = 50
    ): ITunesLookupResponse

    /**
     * Lookup album by ID to get its tracks
     */
    @GET("lookup")
    suspend fun lookupAlbum(
        @Query("id") albumId: Long,
        @Query("entity") entity: String = "song"
    ): ITunesLookupResponse

    /**
     * Get artist's albums
     */
    @GET("lookup")
    suspend fun getArtistAlbums(
        @Query("id") artistId: Long,
        @Query("entity") entity: String = "album",
        @Query("limit") limit: Int = 50
    ): ITunesLookupResponse
}
