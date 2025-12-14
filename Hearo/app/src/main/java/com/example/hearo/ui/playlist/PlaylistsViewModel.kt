package com.example.hearo.ui.playlist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.hearo.data.database.entity.PlaylistWithTracks
import com.example.hearo.data.repository.PlaylistRepository
import kotlinx.coroutines.launch

class PlaylistsViewModel(application: Application) : AndroidViewModel(application) {

    private val playlistRepository = PlaylistRepository(application)

    val playlists: LiveData<List<PlaylistWithTracks>> =
        playlistRepository.getAllPlaylistsWithTracks().asLiveData()

    fun createPlaylist(name: String, description: String? = null) {
        viewModelScope.launch {
            playlistRepository.createPlaylist(name, description)
        }
    }

    fun renamePlaylist(playlistId: Long, newName: String) {
        viewModelScope.launch {
            val playlist = playlistRepository.getPlaylistById(playlistId)
            playlist?.let {
                playlistRepository.updatePlaylist(it.copy(name = newName))
            }
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            playlistRepository.deletePlaylist(playlistId)
        }
    }
}


