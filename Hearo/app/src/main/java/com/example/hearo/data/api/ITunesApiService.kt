package com.example.hearo.data.api

import com.example.hearo.data.model.itunes.ITunesSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ITunesApiService {

    /**
     * Поиск музыки в iTunes
     *
     * @param term Поисковый запрос
     * @param entity Тип контента (song, album, artist)
     * @param limit Количество результатов (макс 200)
     * @param country Код страны (us, gb, ru и т.д.)
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
     * Поиск альбомов
     */
    @GET("search")
    suspend fun searchAlbums(
        @Query("term") term: String,
        @Query("entity") entity: String = "album",
        @Query("limit") limit: Int = 25,
        @Query("country") country: String = "us",
        @Query("media") media: String = "music"
    ): ITunesSearchResponse

    /**
     * Поиск артистов
     */
    @GET("search")
    suspend fun searchArtists(
        @Query("term") term: String,
        @Query("entity") entity: String = "musicArtist",
        @Query("limit") limit: Int = 25,
        @Query("country") country: String = "us"
    ): ITunesSearchResponse
}