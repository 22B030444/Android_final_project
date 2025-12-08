package com.example.hearo.data.database.dao

import androidx.room.*
import com.example.hearo.data.database.entity.ArtistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtistDao {

    /**
     * Get all followed artists
     */
    @Query("SELECT * FROM followed_artists ORDER BY addedAt DESC")
    fun getAllFollowedArtists(): Flow<List<ArtistEntity>>

    /**
     * Get artist by ID
     */
    @Query("SELECT * FROM followed_artists WHERE id = :artistId")
    suspend fun getArtistById(artistId: String): ArtistEntity?

    /**
     * Check if artist is followed
     */
    @Query("SELECT EXISTS(SELECT 1 FROM followed_artists WHERE id = :artistId)")
    suspend fun isArtistFollowed(artistId: String): Boolean

    /**
     * Follow artist
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtist(artist: ArtistEntity)

    /**
     * Unfollow artist
     */
    @Delete
    suspend fun deleteArtist(artist: ArtistEntity)

    /**
     * Unfollow artist by ID
     */
    @Query("DELETE FROM followed_artists WHERE id = :artistId")
    suspend fun deleteArtistById(artistId: String)

    /**
     * Get followed artists count
     */
    @Query("SELECT COUNT(*) FROM followed_artists")
    suspend fun getFollowedArtistsCount(): Int

    /**
     * Clear all followed artists
     */
    @Query("DELETE FROM followed_artists")
    suspend fun clearAllArtists()

    /**
     * Search followed artists by name
     */
    @Query("SELECT * FROM followed_artists WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchArtists(query: String): Flow<List<ArtistEntity>>
}
