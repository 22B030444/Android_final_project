package com.example.hearo.data.database.dao

import androidx.room.*
import com.example.hearo.data.database.entity.ArtistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtistDao {

    @Query("SELECT * FROM followed_artists ORDER BY addedAt DESC")
    fun getAllFollowedArtists(): Flow<List<ArtistEntity>>

    @Query("SELECT * FROM followed_artists WHERE id = :artistId")
    suspend fun getArtistById(artistId: String): ArtistEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM followed_artists WHERE id = :artistId)")
    suspend fun isArtistFollowed(artistId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtist(artist: ArtistEntity)

    @Delete
    suspend fun deleteArtist(artist: ArtistEntity)

    @Query("DELETE FROM followed_artists WHERE id = :artistId")
    suspend fun deleteArtistById(artistId: String)

    @Query("SELECT COUNT(*) FROM followed_artists")
    suspend fun getFollowedArtistsCount(): Int

    @Query("DELETE FROM followed_artists")
    suspend fun clearAllArtists()

    @Query("SELECT * FROM followed_artists WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchArtists(query: String): Flow<List<ArtistEntity>>
}
