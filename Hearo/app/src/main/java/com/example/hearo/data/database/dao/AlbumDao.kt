package com.example.hearo.data.database.dao

import androidx.room.*
import com.example.hearo.data.database.entity.AlbumEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumDao {

    /**
     * Get all saved albums
     */
    @Query("SELECT * FROM saved_albums ORDER BY addedAt DESC")
    fun getAllSavedAlbums(): Flow<List<AlbumEntity>>

    /**
     * Get album by ID
     */
    @Query("SELECT * FROM saved_albums WHERE id = :albumId")
    suspend fun getAlbumById(albumId: String): AlbumEntity?

    /**
     * Check if album is saved
     */
    @Query("SELECT EXISTS(SELECT 1 FROM saved_albums WHERE id = :albumId)")
    suspend fun isAlbumSaved(albumId: String): Boolean

    /**
     * Save album
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbum(album: AlbumEntity)

    /**
     * Remove album
     */
    @Delete
    suspend fun deleteAlbum(album: AlbumEntity)

    /**
     * Remove album by ID
     */
    @Query("DELETE FROM saved_albums WHERE id = :albumId")
    suspend fun deleteAlbumById(albumId: String)

    /**
     * Get saved albums count
     */
    @Query("SELECT COUNT(*) FROM saved_albums")
    suspend fun getSavedAlbumsCount(): Int

    /**
     * Clear all saved albums
     */
    @Query("DELETE FROM saved_albums")
    suspend fun clearAllAlbums()

    /**
     * Get albums by artist
     */
    @Query("SELECT * FROM saved_albums WHERE artistId = :artistId ORDER BY releaseDate DESC")
    fun getAlbumsByArtist(artistId: String): Flow<List<AlbumEntity>>

    /**
     * Search saved albums by name
     */
    @Query("SELECT * FROM saved_albums WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchAlbums(query: String): Flow<List<AlbumEntity>>
}