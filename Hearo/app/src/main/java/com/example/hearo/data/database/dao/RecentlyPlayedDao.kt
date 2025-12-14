package com.example.hearo.data.database.dao

import androidx.room.*
import com.example.hearo.data.database.entity.RecentlyPlayedEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentlyPlayedDao {

    @Query("SELECT * FROM recently_played ORDER BY playedAt DESC LIMIT :limit")
    fun getRecentlyPlayed(limit: Int = 20): Flow<List<RecentlyPlayedEntity>>

    @Query("SELECT * FROM recently_played ORDER BY playedAt DESC LIMIT :limit")
    suspend fun getRecentlyPlayedList(limit: Int = 20): List<RecentlyPlayedEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: RecentlyPlayedEntity)

    @Query("DELETE FROM recently_played WHERE trackId = :trackId")
    suspend fun deleteTrack(trackId: String)

    @Query("DELETE FROM recently_played")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM recently_played")
    suspend fun getCount(): Int

    // Удаляем старые записи, оставляя только последние N
    @Query("DELETE FROM recently_played WHERE trackId NOT IN (SELECT trackId FROM recently_played ORDER BY playedAt DESC LIMIT :keepCount)")
    suspend fun trimToSize(keepCount: Int = 50)
}