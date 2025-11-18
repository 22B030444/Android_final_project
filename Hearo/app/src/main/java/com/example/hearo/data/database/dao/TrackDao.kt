package com.example.hearo.data.database.dao

import androidx.room.*
import com.example.hearo.data.database.entity.TrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {

    /**
     * Получить все избранные треки (с автообновлением через Flow)
     */
    @Query("SELECT * FROM liked_tracks ORDER BY addedAt DESC")
    fun getAllLikedTracks(): Flow<List<TrackEntity>>

    /**
     * Получить трек по ID
     */
    @Query("SELECT * FROM liked_tracks WHERE id = :trackId")
    suspend fun getTrackById(trackId: String): TrackEntity?

    /**
     * Проверить, есть ли трек в избранном
     */
    @Query("SELECT EXISTS(SELECT 1 FROM liked_tracks WHERE id = :trackId)")
    suspend fun isTrackLiked(trackId: String): Boolean

    /**
     * Добавить трек в избранное
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: TrackEntity)

    /**
     * Удалить трек из избранного
     */
    @Delete
    suspend fun deleteTrack(track: TrackEntity)

    /**
     * Удалить трек по ID
     */
    @Query("DELETE FROM liked_tracks WHERE id = :trackId")
    suspend fun deleteTrackById(trackId: String)

    /**
     * Получить количество избранных треков
     */
    @Query("SELECT COUNT(*) FROM liked_tracks")
    suspend fun getLikedTracksCount(): Int

    /**
     * Очистить все избранные треки
     */
    @Query("DELETE FROM liked_tracks")
    suspend fun clearAllTracks()
}