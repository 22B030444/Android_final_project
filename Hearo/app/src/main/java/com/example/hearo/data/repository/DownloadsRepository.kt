package com.example.hearo.data.repository

import android.content.Context
import android.util.Log
import com.example.hearo.data.database.MusicDatabase
import com.example.hearo.data.database.entity.DownloadedTrackEntity
import com.example.hearo.data.model.MusicSource
import com.example.hearo.data.model.UniversalTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File

class DownloadsRepository(private val context: Context) {

    private val downloadedTrackDao = MusicDatabase.getDatabase(context).downloadedTrackDao()

    fun getAllDownloads(): Flow<List<DownloadedTrack>> {
        return downloadedTrackDao.getAllDownloads().map { entities ->
            entities.map { it.toDownloadedTrack() }
        }
    }

    fun searchDownloads(query: String): Flow<List<DownloadedTrack>> {
        return downloadedTrackDao.searchDownloads(query).map { entities ->
            entities.map { it.toDownloadedTrack() }
        }
    }

    suspend fun saveDownloadedTrack(
        track: UniversalTrack,
        localFilePath: String,
        fileSize: Long,
        isFull: Boolean
    ) {
        withContext(Dispatchers.IO) {
            try {
                val entity = DownloadedTrackEntity(
                    trackId = track.id,
                    trackName = track.name,
                    artistName = track.artistName,
                    albumName = track.albumName,
                    imageUrl = track.imageUrl,
                    localFilePath = localFilePath,
                    durationMs = track.durationMs,
                    source = track.source.name,
                    fileSize = fileSize,
                    isFull = isFull
                )
                downloadedTrackDao.insertDownload(entity)
                Log.d("DownloadsRepository", "Saved download: ${track.name}")
            } catch (e: Exception) {
                Log.e("DownloadsRepository", "Failed to save download", e)
            }
        }
    }

    suspend fun deleteDownload(trackId: String) {
        withContext(Dispatchers.IO) {
            try {
                val track = downloadedTrackDao.getDownloadedTrack(trackId)
                if (track != null) {
                    // Удаляем файл
                    val file = File(track.localFilePath)
                    if (file.exists()) {
                        file.delete()
                    }
                    // Удаляем из БД
                    downloadedTrackDao.deleteDownload(trackId)
                    Log.d("DownloadsRepository", "Deleted download: $trackId")
                }
            } catch (e: Exception) {
                Log.e("DownloadsRepository", "Failed to delete download", e)
            }
        }
    }

    suspend fun isTrackDownloaded(trackId: String): Boolean {
        return withContext(Dispatchers.IO) {
            downloadedTrackDao.isTrackDownloaded(trackId)
        }
    }

    suspend fun getDownloadedTrack(trackId: String): DownloadedTrack? {
        return withContext(Dispatchers.IO) {
            downloadedTrackDao.getDownloadedTrack(trackId)?.toDownloadedTrack()
        }
    }

    suspend fun getTotalDownloadsSize(): Long {
        return withContext(Dispatchers.IO) {
            downloadedTrackDao.getTotalSize() ?: 0L
        }
    }

    suspend fun getDownloadCount(): Int {
        return withContext(Dispatchers.IO) {
            downloadedTrackDao.getDownloadCount()
        }
    }

    suspend fun clearAllDownloads() {
        withContext(Dispatchers.IO) {
            val downloads = downloadedTrackDao.getAllDownloadsList()
            downloads.forEach { track ->
                val file = File(track.localFilePath)
                if (file.exists()) {
                    file.delete()
                }
            }
            downloadedTrackDao.clearAll()
        }
    }

    private fun DownloadedTrackEntity.toDownloadedTrack(): DownloadedTrack {
        return DownloadedTrack(
            track = UniversalTrack(
                id = trackId,
                name = trackName,
                artistName = artistName,
                albumName = albumName,
                imageUrl = imageUrl,
                previewUrl = localFilePath,  // Используем локальный путь для воспроизведения
                downloadUrl = null,
                durationMs = durationMs,
                source = try { MusicSource.valueOf(source) } catch (e: Exception) { MusicSource.ITUNES },
                canDownloadFull = false
            ),
            localFilePath = localFilePath,
            fileSize = fileSize,
            isFull = isFull,
            downloadedAt = downloadedAt
        )
    }
}

data class DownloadedTrack(
    val track: UniversalTrack,
    val localFilePath: String,
    val fileSize: Long,
    val isFull: Boolean,
    val downloadedAt: Long
)


