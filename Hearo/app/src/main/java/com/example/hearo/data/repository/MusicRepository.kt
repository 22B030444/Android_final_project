package com.example.hearo.data.repository

import android.content.Context
import android.util.Log
import com.example.hearo.data.api.ITunesRetrofitClient
import com.example.hearo.data.api.JamendoRetrofitClient
import com.example.hearo.data.database.MusicDatabase
import com.example.hearo.data.database.entity.toEntity
import com.example.hearo.data.database.entity.toTrack
import com.example.hearo.data.model.UniversalTrack
import com.example.hearo.data.model.toUniversalTrack
import com.example.hearo.data.preferences.AppPreferences
import com.example.hearo.utils.Constants
import kotlinx.coroutines.Dispatchers

import com.example.hearo.data.model.MusicSource
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class MusicRepository(context: Context) {

    private val itunesApi = ITunesRetrofitClient.api
    private val jamendoApi = JamendoRetrofitClient.api
    private val preferences = AppPreferences(context)
    private val trackDao = MusicDatabase.getDatabase(context).trackDao()

    // ========================================
    // ITUNES SEARCH
    // ========================================

    /**
     * Поиск треков в iTunes
     */
    suspend fun searchITunes(query: String, limit: Int = 25): Result<List<UniversalTrack>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = itunesApi.searchMusic(
                    term = query,
                    limit = limit
                )

                val tracks = response.results.map { it.toUniversalTrack() }
                preferences.saveSearchQuery(query)

                Log.d("MusicRepository", "Found ${tracks.size} iTunes tracks")
                Result.success(tracks)

            } catch (e: Exception) {
                Log.e("MusicRepository", "iTunes search failed", e)
                Result.failure(e)
            }
        }
    }

    // ========================================
    // JAMENDO SEARCH
    // ========================================

    /**
     * Поиск треков в Jamendo
     */
    suspend fun searchJamendo(query: String, limit: Int = 20): Result<List<UniversalTrack>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = jamendoApi.searchTracks(
                    clientId = Constants.JAMENDO_CLIENT_ID,
                    query = query,
                    limit = limit
                )

                val tracks = response.results.map { it.toUniversalTrack() }
                preferences.saveSearchQuery(query)

                Log.d("MusicRepository", "Found ${tracks.size} Jamendo tracks")
                Result.success(tracks)

            } catch (e: Exception) {
                Log.e("MusicRepository", "Jamendo search failed", e)
                Result.failure(e)
            }
        }
    }

    // ========================================
    // COMBINED SEARCH
    // ========================================

    /**
     * Поиск в обоих источниках одновременно
     */
    suspend fun searchBoth(query: String): Result<Pair<List<UniversalTrack>, List<UniversalTrack>>> {
        return withContext(Dispatchers.IO) {
            try {
                val itunesDeferred = async { searchITunes(query) }
                val jamendoDeferred = async { searchJamendo(query) }

                val itunesResult = itunesDeferred.await()
                val jamendoResult = jamendoDeferred.await()

                val itunesTracks = itunesResult.getOrNull() ?: emptyList()
                val jamendoTracks = jamendoResult.getOrNull() ?: emptyList()

                Log.d("MusicRepository", "Combined: ${itunesTracks.size} iTunes + ${jamendoTracks.size} Jamendo")
                Result.success(Pair(itunesTracks, jamendoTracks))

            } catch (e: Exception) {
                Log.e("MusicRepository", "Combined search failed", e)
                Result.failure(e)
            }
        }
    }

    // ========================================
    // LOCAL DATABASE (Room)
    // ========================================

    fun getLocalLikedTracks(): Flow<List<UniversalTrack>> {
        return trackDao.getAllLikedTracks().map { entities ->
            entities.map { it.toTrack().toUniversalTrack() }
        }
    }

    suspend fun addTrackToLocal(track: UniversalTrack): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                // Конвертируем в старую модель для Room
                val oldTrack = track.toOldTrackModel()
                trackDao.insertTrack(oldTrack.toEntity())
                Log.d("MusicRepository", "Added to local: ${track.name}")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e("MusicRepository", "Failed to add track", e)
                Result.failure(e)
            }
        }
    }

    suspend fun removeTrackFromLocal(trackId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                trackDao.deleteTrackById(trackId)
                Log.d("MusicRepository", "Removed from local: $trackId")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e("MusicRepository", "Failed to remove track", e)
                Result.failure(e)
            }
        }
    }

    suspend fun isTrackLikedLocally(trackId: String): Boolean {
        return withContext(Dispatchers.IO) {
            trackDao.isTrackLiked(trackId)
        }
    }

    suspend fun toggleLocalLike(track: UniversalTrack): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val isLiked = isTrackLikedLocally(track.id)
                if (isLiked) {
                    removeTrackFromLocal(track.id)
                    Result.success(false)
                } else {
                    addTrackToLocal(track)
                    Result.success(true)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getLikedTracksCount(): Int {
        return withContext(Dispatchers.IO) {
            trackDao.getLikedTracksCount()
        }
    }

    // ========================================
    // SEARCH HISTORY
    // ========================================

    fun getSearchHistory(): List<String> {
        return preferences.getSearchHistory()
    }

    fun clearSearchHistory() {
        preferences.clearSearchHistory()
    }
}

/**
 * Конвертер для обратной совместимости с Room
 */
private fun UniversalTrack.toOldTrackModel(): com.example.hearo.data.model.spotify.Track {
    return com.example.hearo.data.model.spotify.Track(
        id = id,
        name = name,
        artists = listOf(
            com.example.hearo.data.model.spotify.Artist(
                id = "0",
                name = artistName,
                externalUrls = null
            )
        ),
        album = com.example.hearo.data.model.spotify.Album(
            id = "0",
            name = albumName,
            images = listOfNotNull(
                imageUrl?.let {
                    com.example.hearo.data.model.spotify.SpotifyImage(it, 600, 600)
                }
            ),
            releaseDate = null
        ),
        durationMs = durationMs,
        previewUrl = previewUrl,
        uri = "track:$id",
        externalUrls = null
    )
}

private fun com.example.hearo.data.model.spotify.Track.toUniversalTrack(): UniversalTrack {
    return UniversalTrack(
        id = id,
        name = name,
        artistName = artists.firstOrNull()?.name ?: "Unknown",
        albumName = album.name,
        imageUrl = album.images.firstOrNull()?.url,
        previewUrl = previewUrl,
        downloadUrl = null,
        durationMs = durationMs,
        source = MusicSource.ITUNES,
        canDownloadFull = false
    )
}
