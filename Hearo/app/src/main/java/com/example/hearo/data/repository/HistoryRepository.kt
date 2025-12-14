package com.example.hearo.data.repository

import android.content.Context
import android.util.Log
import com.example.hearo.data.database.MusicDatabase
import com.example.hearo.data.database.entity.RecentlyPlayedEntity
import com.example.hearo.data.model.MusicSource
import com.example.hearo.data.model.UniversalTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class HistoryRepository(context: Context) {

    private val recentlyPlayedDao = MusicDatabase.getDatabase(context).recentlyPlayedDao()

    fun getRecentlyPlayed(limit: Int = 20): Flow<List<UniversalTrack>> {
        return recentlyPlayedDao.getRecentlyPlayed(limit).map { entities ->
            entities.map { it.toUniversalTrack() }
        }
    }

    suspend fun getRecentlyPlayedList(limit: Int = 20): List<UniversalTrack> {
        return withContext(Dispatchers.IO) {
            recentlyPlayedDao.getRecentlyPlayedList(limit).map { it.toUniversalTrack() }
        }
    }

    suspend fun addToHistory(track: UniversalTrack) {
        withContext(Dispatchers.IO) {
            try {
                val entity = RecentlyPlayedEntity(
                    trackId = track.id,
                    trackName = track.name,
                    artistName = track.artistName,
                    albumName = track.albumName,
                    imageUrl = track.imageUrl,
                    previewUrl = track.previewUrl,
                    durationMs = track.durationMs,
                    source = track.source.name
                )
                recentlyPlayedDao.insertTrack(entity)
                recentlyPlayedDao.trimToSize(50)
                Log.d("HistoryRepository", "Added to history: ${track.name}")
            } catch (e: Exception) {
                Log.e("HistoryRepository", "Failed to add to history", e)
            }
        }
    }

    suspend fun clearHistory() {
        withContext(Dispatchers.IO) {
            recentlyPlayedDao.clearAll()
        }
    }

    private fun RecentlyPlayedEntity.toUniversalTrack(): UniversalTrack {
        return UniversalTrack(
            id = trackId,
            name = trackName,
            artistName = artistName,
            albumName = albumName,
            imageUrl = imageUrl,
            previewUrl = previewUrl,
            downloadUrl = null,
            durationMs = durationMs,
            source = try { MusicSource.valueOf(source) } catch (e: Exception) { MusicSource.ITUNES },
            canDownloadFull = false
        )
    }
}