package com.example.hearo.data.repository

import android.content.Context
import android.util.Log
import com.example.hearo.data.database.MusicDatabase
import com.example.hearo.data.database.entity.PlaylistEntity
import com.example.hearo.data.database.entity.PlaylistTrackEntity
import com.example.hearo.data.database.entity.PlaylistWithTracks
import com.example.hearo.data.model.UniversalTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class PlaylistRepository(context: Context) {

    private val playlistDao = MusicDatabase.getDatabase(context).playlistDao()


    fun getAllPlaylists(): Flow<List<PlaylistEntity>> {
        return playlistDao.getAllPlaylists()
    }

    fun getAllPlaylistsWithTracks(): Flow<List<PlaylistWithTracks>> {
        return playlistDao.getAllPlaylistsWithTracks()
    }

    suspend fun getPlaylistById(playlistId: Long): PlaylistEntity? {
        return withContext(Dispatchers.IO) {
            playlistDao.getPlaylistById(playlistId)
        }
    }

    suspend fun getPlaylistWithTracks(playlistId: Long): PlaylistWithTracks? {
        return withContext(Dispatchers.IO) {
            playlistDao.getPlaylistWithTracks(playlistId)
        }
    }

    suspend fun createPlaylist(name: String, description: String? = null): Result<Long> {
        return withContext(Dispatchers.IO) {
            try {
                val playlist = PlaylistEntity(
                    name = name,
                    description = description,
                    imageUrl = null
                )
                val id = playlistDao.insertPlaylist(playlist)
                Log.d("PlaylistRepository", "Created playlist: $name with id: $id")
                Result.success(id)
            } catch (e: Exception) {
                Log.e("PlaylistRepository", "Failed to create playlist", e)
                Result.failure(e)
            }
        }
    }

    suspend fun updatePlaylist(playlist: PlaylistEntity): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                playlistDao.updatePlaylist(playlist.copy(updatedAt = System.currentTimeMillis()))
                Log.d("PlaylistRepository", "Updated playlist: ${playlist.name}")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e("PlaylistRepository", "Failed to update playlist", e)
                Result.failure(e)
            }
        }
    }

    suspend fun deletePlaylist(playlistId: Long): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                playlistDao.deletePlaylistById(playlistId)
                Log.d("PlaylistRepository", "Deleted playlist: $playlistId")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e("PlaylistRepository", "Failed to delete playlist", e)
                Result.failure(e)
            }
        }
    }

    suspend fun getPlaylistsCount(): Int {
        return withContext(Dispatchers.IO) {
            playlistDao.getPlaylistsCount()
        }
    }

    // ========== TRACK OPERATIONS ==========

    fun getPlaylistTracks(playlistId: Long): Flow<List<PlaylistTrackEntity>> {
        return playlistDao.getPlaylistTracks(playlistId)
    }

    suspend fun addTrackToPlaylist(playlistId: Long, track: UniversalTrack): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val playlistTrack = PlaylistTrackEntity(
                    playlistId = playlistId,
                    trackId = track.id,
                    trackName = track.name,
                    artistName = track.artistName,
                    albumName = track.albumName,
                    imageUrl = track.imageUrl,
                    previewUrl = track.previewUrl,
                    durationMs = track.durationMs,
                    source = track.source.name
                )
                playlistDao.insertTrackToPlaylist(playlistTrack)
                playlistDao.updatePlaylistTimestamp(playlistId)


                val playlist = playlistDao.getPlaylistById(playlistId)
                if (playlist != null && playlist.imageUrl == null && track.imageUrl != null) {
                    playlistDao.updatePlaylist(playlist.copy(imageUrl = track.imageUrl)) }



                Log.d("PlaylistRepository", "Added track ${track.name} to playlist $playlistId")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e("PlaylistRepository", "Failed to add track to playlist", e)
                Result.failure(e)
            }
        }
    }

    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                playlistDao.removeTrackFromPlaylist(playlistId, trackId)
                playlistDao.updatePlaylistTimestamp(playlistId)
                Log.d("PlaylistRepository", "Removed track $trackId from playlist $playlistId")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e("PlaylistRepository", "Failed to remove track from playlist", e)
                Result.failure(e)
            }
        }
    }

    suspend fun isTrackInPlaylist(playlistId: Long, trackId: String): Boolean {
        return withContext(Dispatchers.IO) {
            playlistDao.isTrackInPlaylist(playlistId, trackId)
        }
    }

    suspend fun getPlaylistTracksCount(playlistId: Long): Int {
        return withContext(Dispatchers.IO) {
            playlistDao.getPlaylistTracksCount(playlistId)
        }
    }
}


