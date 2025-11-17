package com.example.hearo.data.repository

import android.content.Context
import android.util.Log
import com.example.hearo.data.api.RetrofitClient
import com.example.hearo.data.api.SpotifyApiService
import com.example.hearo.data.model.auth.UserProfile
import com.example.hearo.data.model.spotify.Track
import com.example.hearo.data.preferences.AppPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MusicRepository(
    context: Context,
    private val authRepository: AuthRepository
) {

    private val spotifyApi: SpotifyApiService = RetrofitClient.getSpotifyApi(context)
    private val preferences = AppPreferences(context)

    /**
     * Получить профиль пользователя
     */
    suspend fun getUserProfile(): Result<UserProfile> {
        return withContext(Dispatchers.IO) {
            try {
                // Проверяем токен перед запросом
                if (!authRepository.ensureValidToken()) {
                    return@withContext Result.failure(Exception("Token refresh failed"))
                }

                val profile = spotifyApi.getUserProfile()
                Log.d("MusicRepository", "Profile loaded: ${profile.displayName}")
                Result.success(profile)

            } catch (e: Exception) {
                Log.e("MusicRepository", "Failed to load profile", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Поиск треков
     */
    suspend fun searchTracks(query: String, limit: Int = 20): Result<List<Track>> {
        return withContext(Dispatchers.IO) {
            try {
                if (!authRepository.ensureValidToken()) {
                    return@withContext Result.failure(Exception("Token refresh failed"))
                }

                val response = spotifyApi.search(
                    query = query,
                    type = "track",
                    limit = limit
                )

                val tracks = response.tracks?.items ?: emptyList()

                // Сохраняем в историю поиска
                preferences.saveSearchQuery(query)

                Log.d("MusicRepository", "Found ${tracks.size} tracks")
                Result.success(tracks)

            } catch (e: Exception) {
                Log.e("MusicRepository", "Search failed", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Получить Liked Songs
     */
    suspend fun getSavedTracks(limit: Int = 50): Result<List<Track>> {
        return withContext(Dispatchers.IO) {
            try {
                if (!authRepository.ensureValidToken()) {
                    return@withContext Result.failure(Exception("Token refresh failed"))
                }

                val response = spotifyApi.getSavedTracks(limit = limit)
                val tracks = response.items.map { it.track }

                Log.d("MusicRepository", "Loaded ${tracks.size} saved tracks")
                Result.success(tracks)

            } catch (e: Exception) {
                Log.e("MusicRepository", "Failed to load saved tracks", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Добавить трек в Liked Songs
     */
    suspend fun saveTrack(trackId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                if (!authRepository.ensureValidToken()) {
                    return@withContext Result.failure(Exception("Token refresh failed"))
                }

                spotifyApi.saveTracks(trackIds = trackId)
                Log.d("MusicRepository", "Track saved: $trackId")
                Result.success(Unit)

            } catch (e: Exception) {
                Log.e("MusicRepository", "Failed to save track", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Удалить трек из Liked Songs
     */
    suspend fun removeSavedTrack(trackId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                if (!authRepository.ensureValidToken()) {
                    return@withContext Result.failure(Exception("Token refresh failed"))
                }

                spotifyApi.removeSavedTracks(trackIds = trackId)
                Log.d("MusicRepository", "Track removed: $trackId")
                Result.success(Unit)

            } catch (e: Exception) {
                Log.e("MusicRepository", "Failed to remove track", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Проверить, сохранен ли трек
     */
    suspend fun isTrackSaved(trackId: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                if (!authRepository.ensureValidToken()) {
                    return@withContext Result.failure(Exception("Token refresh failed"))
                }

                val result = spotifyApi.checkSavedTracks(trackIds = trackId)
                Result.success(result.firstOrNull() ?: false)

            } catch (e: Exception) {
                Log.e("MusicRepository", "Failed to check track", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Получить историю поиска
     */
    fun getSearchHistory(): List<String> {
        return preferences.getSearchHistory()
    }

    /**
     * Очистить историю поиска
     */
    fun clearSearchHistory() {
        preferences.clearSearchHistory()
    }
}