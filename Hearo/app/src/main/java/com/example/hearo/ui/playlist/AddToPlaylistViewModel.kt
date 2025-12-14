package com.example.hearo.ui.playlist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.hearo.data.database.entity.PlaylistWithTracks
import com.example.hearo.data.model.UniversalTrack
import com.example.hearo.data.repository.PlaylistRepository
import kotlinx.coroutines.launch

class AddToPlaylistViewModel(application: Application) : AndroidViewModel(application) {

    private val playlistRepository = PlaylistRepository(application)

    val playlists: LiveData<List<PlaylistWithTracks>> =
        playlistRepository.getAllPlaylistsWithTracks().asLiveData()

    fun addTrackToPlaylist(playlistId: Long, track: UniversalTrack) {
        viewModelScope.launch {
            playlistRepository.addTrackToPlaylist(playlistId, track)
        }
    }

    fun createPlaylistAndAddTrack(name: String, track: UniversalTrack?) {
        viewModelScope.launch {
            playlistRepository.createPlaylist(name).onSuccess { playlistId ->
                track?.let {
                    playlistRepository.addTrackToPlaylist(playlistId, it)
                }
            }
        }
    }
}


