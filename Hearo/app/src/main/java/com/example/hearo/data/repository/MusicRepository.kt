package com.example.hearo.data.repository

import android.content.Context
import android.util.Log
import com.example.hearo.data.api.ITunesRetrofitClient
import com.example.hearo.data.api.JamendoRetrofitClient
import com.example.hearo.data.database.MusicDatabase
import com.example.hearo.data.database.entity.AlbumEntity
import com.example.hearo.data.database.entity.ArtistEntity
import com.example.hearo.data.model.MusicSource
import com.example.hearo.data.model.UniversalAlbum
import com.example.hearo.data.model.UniversalArtist
import com.example.hearo.data.model.UniversalTrack
import com.example.hearo.data.model.toUniversalAlbum
import com.example.hearo.data.model.toUniversalArtist
import com.example.hearo.data.model.toUniversalTrack
import com.example.hearo.data.preferences.AppPreferences
import com.example.hearo.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class MusicRepository(context: Context) {

    private val itunesApi = ITunesRetrofitClient.api
    private val jamendoApi = JamendoRetrofitClient.api
    private val preferences = AppPreferences(context)
    private val database = MusicDatabase.getDatabase(context)
    private val trackDao = database.trackDao()
    private val artistDao = database.artistDao()
    private val albumDao = database.albumDao()


    suspend fun searchITunes(query: String, limit: Int = 25): Result<List<UniversalTrack>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = itunesApi.searchMusic(
                    term = query,
                    limit = limit
                )

                val tracks = response.results.map { it.toUniversalTrack() }
                preferences.saveSearchQuery(query)

                Log.d("MusicRepository", "Found ${tracks.size} iTunes tracks")
                Result.success(tracks)

            } catch (e: Exception) {
                Log.e("MusicRepository", "iTunes search failed", e)
                Result.failure(e)
            }
        }
    }

    suspend fun searchArtists(query: String, limit: Int = 25): Result<List<UniversalArtist>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = itunesApi.searchArtists(
                    term = query,
                    limit = limit
                )

                val artists = response.results.map { it.toUniversalArtist() }
                preferences.saveSearchQuery(query)

                Log.d("MusicRepository", "Found ${artists.size} artists")
                Result.success(artists)

            } catch (e: Exception) {
                Log.e("MusicRepository", "Artist search failed", e)
                Result.failure(e)
            }
        }
    }

    suspend fun searchAlbums(query: String, limit: Int = 25): Result<List<UniversalAlbum>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = itunesApi.searchAlbums(
                    term = query,
                    limit = limit
                )

                val albums = response.results.map { it.toUniversalAlbum() }
                preferences.saveSearchQuery(query)

                Log.d("MusicRepository", "Found ${albums.size} albums")
                Result.success(albums)

            } catch (e: Exception) {
                Log.e("MusicRepository", "Album search failed", e)
                Result.failure(e)
            }
        }
    }

    suspend fun getArtistDetails(artistId: String): Result<Pair<UniversalArtist?, List<UniversalTrack>>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = itunesApi.lookupArtist(
                    artistId = artistId.toLong(),
                    limit = 50
                )

                var artist: UniversalArtist? = null
                val tracks = mutableListOf<UniversalTrack>()

                response.results.forEach { result ->
                    when (result.wrapperType) {
                        "artist" -> {
                            artist = UniversalArtist(
                                id = result.artistId?.toString() ?: artistId,
                                name = result.artistName ?: "Unknown Artist",
                                imageUrl = null,
                                followersCount = 0,
                                monthlyListeners = null,
                                genres = listOfNotNull(result.primaryGenreName),
                                source = MusicSource.ITUNES
                            )
                        }
                        "track" -> {
                            tracks.add(
                                UniversalTrack(
                                    id = result.trackId?.toString() ?: "",
                                    name = result.trackName ?: "",
                                    artistName = result.artistName ?: "",
                                    albumName = result.collectionName ?: "",
                                    imageUrl = result.getHighResArtwork(),
                                    previewUrl = result.previewUrl,
                                    downloadUrl = null,
                                    durationMs = result.trackTimeMillis ?: 30000,
                                    source = MusicSource.ITUNES,
                                    canDownloadFull = false
                                )
                            )
                        }
                    }
                }

                Log.d("MusicRepository", "Artist details: ${artist?.name}, ${tracks.size} tracks")
                Result.success(Pair(artist, tracks))

            } catch (e: Exception) {
                Log.e("MusicRepository", "Get artist details failed", e)
                Result.failure(e)
            }
        }
    }

    suspend fun getAlbumDetails(albumId: String): Result<Pair<UniversalAlbum?, List<UniversalTrack>>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = itunesApi.lookupAlbum(
                    albumId = albumId.toLong()
                )

                var album: UniversalAlbum? = null
                val tracks = mutableListOf<UniversalTrack>()

                response.results.forEach { result ->
                    when (result.wrapperType) {
                        "collection" -> {
                            album = UniversalAlbum(
                                id = result.collectionId?.toString() ?: albumId,
                                name = result.collectionName ?: "Unknown Album",
                                artistName = result.artistName ?: "Unknown Artist",
                                artistId = result.artistId?.toString(),
                                imageUrl = result.getHighResArtwork(),
                                releaseDate = result.releaseDate?.take(10),
                                totalTracks = result.trackCount ?: 0,
                                albumType = null,
                                source = MusicSource.ITUNES
                            )
                        }
                        "track" -> {
                            tracks.add(
                                UniversalTrack(
                                    id = result.trackId?.toString() ?: "",
                                    name = result.trackName ?: "",
                                    artistName = result.artistName ?: "",
                                    albumName = result.collectionName ?: "",
                                    imageUrl = result.getHighResArtwork(),
                                    previewUrl = result.previewUrl,
                                    downloadUrl = null,
                                    durationMs = result.trackTimeMillis ?: 30000,
                                    source = MusicSource.ITUNES,
                                    canDownloadFull = false
                                )
                            )
                        }
                    }
                }

                Log.d("MusicRepository", "Album details: ${album?.name}, ${tracks.size} tracks")
                Result.success(Pair(album, tracks))

            } catch (e: Exception) {
                Log.e("MusicRepository", "Get album details failed", e)
                Result.failure(e)
            }
        }
    }


    suspend fun searchJamendo(query: String, limit: Int = 20): Result<List<UniversalTrack>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = jamendoApi.searchTracks(
                    clientId = Constants.JAMENDO_CLIENT_ID,
                    query = query,
                    limit = limit
                )

                val tracks = response.results.map { it.toUniversalTrack() }
                preferences.saveSearchQuery(query)

                Log.d("MusicRepository", "Found ${tracks.size} Jamendo tracks")
                Result.success(tracks)

            } catch (e: Exception) {
                Log.e("MusicRepository", "Jamendo search failed", e)
                Result.failure(e)
            }
        }
    }

    suspend fun searchBoth(query: String): Result<Pair<List<UniversalTrack>, List<UniversalTrack>>> {
        return withContext(Dispatchers.IO) {
            try {
                val itunesDeferred = async { searchITunes(query) }
                val jamendoDeferred = async { searchJamendo(query) }

                val itunesResult = itunesDeferred.await()
                val jamendoResult = jamendoDeferred.await()

                val itunesTracks = itunesResult.getOrNull() ?: emptyList()
                val jamendoTracks = jamendoResult.getOrNull() ?: emptyList()

                Log.d("MusicRepository", "Combined: ${itunesTracks.size} iTunes + ${jamendoTracks.size} Jamendo")
                Result.success(Pair(itunesTracks, jamendoTracks))

            } catch (e: Exception) {
                Log.e("MusicRepository", "Combined search failed", e)
                Result.failure(e)
            }
        }
    }

    fun getLocalLikedTracks(): Flow<List<UniversalTrack>> {
        return trackDao.getAllLikedTracks().map { entities ->
            entities.map { entity ->
                UniversalTrack(
                    id = entity.id,
                    name = entity.name,
                    artistName = entity.artistName,
                    albumName = entity.albumName,
                    imageUrl = entity.imageUrl,
                    previewUrl = entity.previewUrl,
                    downloadUrl = null,
                    durationMs = entity.durationMs,
                    source = MusicSource.ITUNES,
                    canDownloadFull = false
                )
            }
        }
    }

    suspend fun addTrackToLocal(track: UniversalTrack): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val entity = com.example.hearo.data.database.entity.TrackEntity(
                    id = track.id,
                    name = track.name,
                    artistName = track.artistName,
                    albumName = track.albumName,
                    imageUrl = track.imageUrl,
                    previewUrl = track.previewUrl,
                    durationMs = track.durationMs,
                    spotifyUri = "track:${track.id}"  // или можно переименовать поле
                )
                trackDao.insertTrack(entity)
                Log.d("MusicRepository", "Added to local: ${track.name}")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e("MusicRepository", "Failed to add track", e)
                Result.failure(e)
            }
        }
    }

    suspend fun removeTrackFromLocal(trackId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                trackDao.deleteTrackById(trackId)
                Log.d("MusicRepository", "Removed from local: $trackId")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e("MusicRepository", "Failed to remove track", e)
                Result.failure(e)
            }
        }
    }

    suspend fun isTrackLikedLocally(trackId: String): Boolean {
        return withContext(Dispatchers.IO) {
            trackDao.isTrackLiked(trackId)
        }
    }

    suspend fun getArtistImage(artistName: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val response = itunesApi.searchAlbums(
                    term = artistName,
                    limit = 5
                )

                val imageUrl = response.results
                    .firstOrNull { !it.artworkUrl100.isNullOrEmpty() }
                    ?.getHighResArtwork()

                Log.d("MusicRepository", "Found artist image: $imageUrl")
                imageUrl
            } catch (e: Exception) {
                Log.e("MusicRepository", "Failed to get artist image", e)
                null
            }
        }
    }
    suspend fun toggleLocalLike(track: UniversalTrack): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val isLiked = isTrackLikedLocally(track.id)
                if (isLiked) {
                    removeTrackFromLocal(track.id)
                    Result.success(false)
                } else {
                    addTrackToLocal(track)
                    Result.success(true)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getLikedTracksCount(): Int {
        return withContext(Dispatchers.IO) {
            trackDao.getLikedTracksCount()
        }
    }

    fun getFollowedArtists(): Flow<List<UniversalArtist>> {
        return artistDao.getAllFollowedArtists().map { entities ->
            entities.map { entity ->
                UniversalArtist(
                    id = entity.id,
                    name = entity.name,
                    imageUrl = entity.imageUrl,
                    followersCount = entity.followersCount,
                    monthlyListeners = entity.monthlyListeners,
                    genres = entity.genres?.split(",")?.map { it.trim() } ?: emptyList(),
                    source = MusicSource.ITUNES
                )
            }
        }
    }

    suspend fun isArtistFollowed(artistId: String): Boolean {
        return withContext(Dispatchers.IO) {
            artistDao.isArtistFollowed(artistId)
        }
    }

    suspend fun toggleFollowArtist(artist: UniversalArtist): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val isFollowed = isArtistFollowed(artist.id)
                if (isFollowed) {
                    artistDao.deleteArtistById(artist.id)
                    Result.success(false)
                } else {
                    artistDao.insertArtist(
                        ArtistEntity(
                            id = artist.id,
                            name = artist.name,
                            imageUrl = artist.imageUrl,
                            followersCount = artist.followersCount,
                            genres = artist.genres.joinToString(","),
                            monthlyListeners = artist.monthlyListeners
                        )
                    )
                    Result.success(true)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    fun getSavedAlbums(): Flow<List<AlbumEntity>> {
        return albumDao.getAllSavedAlbums()
    }

    suspend fun isAlbumSaved(albumId: String): Boolean {
        return withContext(Dispatchers.IO) {
            albumDao.isAlbumSaved(albumId)
        }
    }

    suspend fun toggleSaveAlbum(album: UniversalAlbum): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val isSaved = isAlbumSaved(album.id)
                if (isSaved) {
                    albumDao.deleteAlbumById(album.id)
                    Result.success(false)
                } else {
                    albumDao.insertAlbum(
                        AlbumEntity(
                            id = album.id,
                            name = album.name,
                            artistName = album.artistName,
                            artistId = album.artistId,
                            imageUrl = album.imageUrl,
                            releaseDate = album.releaseDate,
                            totalTracks = album.totalTracks,
                            albumType = album.albumType
                        )
                    )
                    Result.success(true)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    fun getSearchHistory(): List<String> {
        return preferences.getSearchHistory()
    }

    fun clearSearchHistory() {
        preferences.clearSearchHistory()
    }
}

