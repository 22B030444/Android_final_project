package com.example.hearo.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.hearo.data.repository.MusicRepository
import com.example.hearo.data.repository.PlaylistRepository
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val musicRepository = MusicRepository(application)
    private val playlistRepository = PlaylistRepository(application)

    private val _likedSongsCount = MutableLiveData<Int>()
    val likedSongsCount: LiveData<Int> = _likedSongsCount

    private val _playlistsCount = MutableLiveData<Int>()
    val playlistsCount: LiveData<Int> = _playlistsCount

    private val _artistsCount = MutableLiveData<Int>()
    val artistsCount: LiveData<Int> = _artistsCount

    init {
        loadCounts()
    }

    private fun loadCounts() {
        viewModelScope.launch {
            musicRepository.getLocalLikedTracks().collect { tracks ->
                _likedSongsCount.value = tracks.size
            }
        }

        viewModelScope.launch {
            playlistRepository.getAllPlaylistsWithTracks().collect { playlists ->
                _playlistsCount.value = playlists.size
            }
        }

        viewModelScope.launch {
            musicRepository.getFollowedArtists().collect { artists ->
                _artistsCount.value = artists.size
            }
        }
    }
}


