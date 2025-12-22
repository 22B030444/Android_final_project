package com.example.hearo.data.database.dao

import androidx.room.*
import com.example.hearo.data.database.entity.TrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {

    @Query("SELECT * FROM liked_tracks ORDER BY addedAt DESC")
    fun getAllLikedTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM liked_tracks WHERE id = :trackId")
    suspend fun getTrackById(trackId: String): TrackEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM liked_tracks WHERE id = :trackId)")
    suspend fun isTrackLiked(trackId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: TrackEntity)

    @Delete
    suspend fun deleteTrack(track: TrackEntity)

    @Query("DELETE FROM liked_tracks WHERE id = :trackId")
    suspend fun deleteTrackById(trackId: String)

    @Query("SELECT COUNT(*) FROM liked_tracks")
    suspend fun getLikedTracksCount(): Int

    @Query("DELETE FROM liked_tracks")
    suspend fun clearAllTracks()
}