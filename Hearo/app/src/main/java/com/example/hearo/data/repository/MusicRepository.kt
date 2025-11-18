package com.example.hearo.data.repository

import android.content.Context
import android.util.Log
import com.example.hearo.data.api.RetrofitClient
import com.example.hearo.data.api.SpotifyApiService
import com.example.hearo.data.database.MusicDatabase
import com.example.hearo.data.database.entity.toEntity
import com.example.hearo.data.database.entity.toTrack
import com.example.hearo.data.model.auth.UserProfile
import com.example.hearo.data.model.spotify.Track
import com.example.hearo.data.preferences.AppPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class MusicRepository(
    context: Context,
    private val authRepository: AuthRepository
) {

    private val spotifyApi: SpotifyApiService = RetrofitClient.getSpotifyApi(context)
    private val preferences = AppPreferences(context)
    private val database = MusicDatabase.getDatabase(context)
    private val trackDao = database.trackDao()

    // ========================================
    // SPOTIFY API METHODS
    // ========================================

    suspend fun getUserProfile(): Result<UserProfile> {
        return withContext(Dispatchers.IO) {
            try {
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
                preferences.saveSearchQuery(query)

                Log.d("MusicRepository", "Found ${tracks.size} tracks")
                Result.success(tracks)

            } catch (e: Exception) {
                Log.e("MusicRepository", "Search failed", e)
                Result.failure(e)
            }
        }
    }

    suspend fun getSavedTracksFromSpotify(limit: Int = 50): Result<List<Track>> {
        return withContext(Dispatchers.IO) {
            try {
                if (!authRepository.ensureValidToken()) {
                    return@withContext Result.failure(Exception("Token refresh failed"))
                }

                val response = spotifyApi.getSavedTracks(limit = limit)
                val tracks = response.items.map { it.track }

                Log.d("MusicRepository", "Loaded ${tracks.size} saved tracks from Spotify")
                Result.success(tracks)

            } catch (e: Exception) {
                Log.e("MusicRepository", "Failed to load saved tracks", e)
                Result.failure(e)
            }
        }
    }

    // ========================================
    // LOCAL DATABASE METHODS (ROOM - 7 баллов)
    // ========================================

    /**
     * Получить все локально сохраненные треки (Flow для автообновления UI)
     */
    fun getLocalLikedTracks(): Flow<List<Track>> {
        return trackDao.getAllLikedTracks().map { entities ->
            entities.map { it.toTrack() }
        }
    }

    /**
     * Добавить трек в локальное избранное
     */
    suspend fun addTrackToLocal(track: Track): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                trackDao.insertTrack(track.toEntity())
                Log.d("MusicRepository", "Track added to local: ${track.name}")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e("MusicRepository", "Failed to add track to local", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Удалить трек из локального избранного
     */
    suspend fun removeTrackFromLocal(trackId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                trackDao.deleteTrackById(trackId)
                Log.d("MusicRepository", "Track removed from local: $trackId")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e("MusicRepository", "Failed to remove track from local", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Проверить, есть ли трек в локальном избранном
     */
    suspend fun isTrackLikedLocally(trackId: String): Boolean {
        return withContext(Dispatchers.IO) {
            trackDao.isTrackLiked(trackId)
        }
    }

    /**
     * Переключить избранное (добавить/удалить)
     */
    suspend fun toggleLocalLike(track: Track): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val isLiked = trackDao.isTrackLiked(track.id)

                if (isLiked) {
                    trackDao.deleteTrackById(track.id)
                    Log.d("MusicRepository", "Track unliked: ${track.name}")
                    Result.success(false) // теперь НЕ в избранном
                } else {
                    trackDao.insertTrack(track.toEntity())
                    Log.d("MusicRepository", "Track liked: ${track.name}")
                    Result.success(true) // теперь В избранном
                }
            } catch (e: Exception) {
                Log.e("MusicRepository", "Failed to toggle like", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Получить количество избранных треков
     */
    suspend fun getLikedTracksCount(): Int {
        return withContext(Dispatchers.IO) {
            trackDao.getLikedTracksCount()
        }
    }

    // ========================================
    // SEARCH HISTORY (SharedPreferences - 4 балла)
    // ========================================

    fun getSearchHistory(): List<String> {
        return preferences.getSearchHistory()
    }

    fun clearSearchHistory() {
        preferences.clearSearchHistory()
    }
}


