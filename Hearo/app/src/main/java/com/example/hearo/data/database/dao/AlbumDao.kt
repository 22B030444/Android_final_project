package com.example.hearo.data.database.dao

import androidx.room.*
import com.example.hearo.data.database.entity.AlbumEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumDao {


    @Query("SELECT * FROM saved_albums ORDER BY addedAt DESC")
    fun getAllSavedAlbums(): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM saved_albums WHERE id = :albumId")
    suspend fun getAlbumById(albumId: String): AlbumEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM saved_albums WHERE id = :albumId)")
    suspend fun isAlbumSaved(albumId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbum(album: AlbumEntity)

    @Delete
    suspend fun deleteAlbum(album: AlbumEntity)

    @Query("DELETE FROM saved_albums WHERE id = :albumId")
    suspend fun deleteAlbumById(albumId: String)

    @Query("SELECT COUNT(*) FROM saved_albums")
    suspend fun getSavedAlbumsCount(): Int

    @Query("DELETE FROM saved_albums")
    suspend fun clearAllAlbums()

    @Query("SELECT * FROM saved_albums WHERE artistId = :artistId ORDER BY releaseDate DESC")
    fun getAlbumsByArtist(artistId: String): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM saved_albums WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchAlbums(query: String): Flow<List<AlbumEntity>>
}