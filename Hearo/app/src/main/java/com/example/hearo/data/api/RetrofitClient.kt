package com.example.hearo.data.api

import android.content.Context
import com.example.hearo.data.preferences.AppPreferences
import com.example.hearo.utils.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    @Volatile
    private var spotifyApi: SpotifyApiService? = null

    @Volatile
    private var authService: SpotifyAuthService? = null

    private fun getAuthOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private fun getApiOkHttpClient(context: Context): OkHttpClient {
        val preferences = AppPreferences(context)

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = AuthInterceptor(preferences)

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    fun getAuthService(): SpotifyAuthService {
        return authService ?: synchronized(this) {
            authService ?: Retrofit.Builder()
                .baseUrl("https://accounts.spotify.com/")
                .client(getAuthOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(SpotifyAuthService::class.java)
                .also { authService = it }
        }
    }

    fun getSpotifyApi(context: Context): SpotifyApiService {
        return spotifyApi ?: synchronized(this) {
            spotifyApi ?: Retrofit.Builder()
                .baseUrl(Constants.SPOTIFY_API_BASE_URL)
                .client(getApiOkHttpClient(context))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(SpotifyApiService::class.java)
                .also { spotifyApi = it }
        }
    }
}