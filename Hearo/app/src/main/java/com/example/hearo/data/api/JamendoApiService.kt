package com.example.hearo.data.api

import com.example.hearo.data.model.jamendo.JamendoSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface JamendoApiService {

    @GET("tracks/")
    suspend fun searchTracks(
        @Query("client_id") clientId: String,
        @Query("search") query: String,
        @Query("limit") limit: Int = 20,
        @Query("format") format: String = "json",
        @Query("audioformat") audioformat: String = "mp32"
    ): JamendoSearchResponse
}