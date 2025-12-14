package com.example.hearo.data.database.dao

import androidx.room.*
import com.example.hearo.data.database.entity.DownloadedTrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadedTrackDao {

    @Query("SELECT * FROM downloaded_tracks ORDER BY downloadedAt DESC")
    fun getAllDownloads(): Flow<List<DownloadedTrackEntity>>

    @Query("SELECT * FROM downloaded_tracks ORDER BY downloadedAt DESC")
    suspend fun getAllDownloadsList(): List<DownloadedTrackEntity>

    @Query("SELECT * FROM downloaded_tracks WHERE trackId = :trackId")
    suspend fun getDownloadedTrack(trackId: String): DownloadedTrackEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM downloaded_tracks WHERE trackId = :trackId)")
    suspend fun isTrackDownloaded(trackId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(track: DownloadedTrackEntity)

    @Query("DELETE FROM downloaded_tracks WHERE trackId = :trackId")
    suspend fun deleteDownload(trackId: String)

    @Query("DELETE FROM downloaded_tracks")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM downloaded_tracks")
    suspend fun getDownloadCount(): Int

    @Query("SELECT SUM(fileSize) FROM downloaded_tracks")
    suspend fun getTotalSize(): Long?

    @Query("SELECT * FROM downloaded_tracks WHERE trackName LIKE '%' || :query || '%' OR artistName LIKE '%' || :query || '%'")
    fun searchDownloads(query: String): Flow<List<DownloadedTrackEntity>>
}


