package com.example.hearo.ui.playlist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.hearo.data.database.entity.PlaylistTrackEntity
import com.example.hearo.data.database.entity.PlaylistWithTracks
import com.example.hearo.data.repository.PlaylistRepository
import kotlinx.coroutines.launch

class PlaylistDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val playlistRepository = PlaylistRepository(application)

    private val _playlistId = MutableLiveData<Long>()

    private val _playlist = MutableLiveData<PlaylistWithTracks?>()
    val playlist: LiveData<PlaylistWithTracks?> = _playlist

    val tracks: LiveData<List<PlaylistTrackEntity>> = _playlistId.switchMap { id ->
        playlistRepository.getPlaylistTracks(id).asLiveData()
    }

    fun loadPlaylist(playlistId: Long) {
        _playlistId.value = playlistId
        viewModelScope.launch {
            _playlist.value = playlistRepository.getPlaylistWithTracks(playlistId)
        }
    }

    fun removeTrackFromPlaylist(trackId: String) {
        val playlistId = _playlistId.value ?: return
        viewModelScope.launch {
            playlistRepository.removeTrackFromPlaylist(playlistId, trackId)
            _playlist.value = playlistRepository.getPlaylistWithTracks(playlistId)
        }
    }
}


