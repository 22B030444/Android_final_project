package com.example.hearo.data.database.dao

import androidx.room.*
import com.example.hearo.data.database.entity.PlaylistEntity
import com.example.hearo.data.database.entity.PlaylistTrackEntity
import com.example.hearo.data.database.entity.PlaylistWithTracks
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {


    @Query("SELECT * FROM playlists ORDER BY updatedAt DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    suspend fun getPlaylistById(playlistId: Long): PlaylistEntity?

    @Transaction
    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    suspend fun getPlaylistWithTracks(playlistId: Long): PlaylistWithTracks?

    @Transaction
    @Query("SELECT * FROM playlists ORDER BY updatedAt DESC")
    fun getAllPlaylistsWithTracks(): Flow<List<PlaylistWithTracks>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    @Delete
    suspend fun deletePlaylist(playlist: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylistById(playlistId: Long)

    @Query("SELECT COUNT(*) FROM playlists")
    suspend fun getPlaylistsCount(): Int


    @Query("SELECT * FROM playlist_tracks WHERE playlistId = :playlistId ORDER BY addedAt DESC")
    fun getPlaylistTracks(playlistId: Long): Flow<List<PlaylistTrackEntity>>

    @Query("SELECT COUNT(*) FROM playlist_tracks WHERE playlistId = :playlistId")
    suspend fun getPlaylistTracksCount(playlistId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrackToPlaylist(track: PlaylistTrackEntity)

    @Query("DELETE FROM playlist_tracks WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: String)

    @Query("DELETE FROM playlist_tracks WHERE playlistId = :playlistId")
    suspend fun clearPlaylistTracks(playlistId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM playlist_tracks WHERE playlistId = :playlistId AND trackId = :trackId)")
    suspend fun isTrackInPlaylist(playlistId: Long, trackId: String): Boolean

    @Query("UPDATE playlists SET updatedAt = :timestamp WHERE id = :playlistId")
    suspend fun updatePlaylistTimestamp(playlistId: Long, timestamp: Long = System.currentTimeMillis())
}


