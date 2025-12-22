package com.example.hearo.ui.library

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.hearo.data.repository.DownloadsRepository
import com.example.hearo.data.repository.MusicRepository
import com.example.hearo.data.repository.PlaylistRepository
import kotlinx.coroutines.launch

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val musicRepository = MusicRepository(application)
    private val downloadsRepository = DownloadsRepository(application)
    private val playlistRepository = PlaylistRepository(application)

    private val _likedSongsCount = MutableLiveData<Int>()
    val likedSongsCount: LiveData<Int> = _likedSongsCount

    private val _downloadsCount = MutableLiveData<Int>()
    val downloadsCount: LiveData<Int> = _downloadsCount

    private val _artistsCount = MutableLiveData<Int>()
    val artistsCount: LiveData<Int> = _artistsCount

    private val _playlistsCount = MutableLiveData<Int>()
    val playlistsCount: LiveData<Int> = _playlistsCount

    init {
        loadCounts()
    }

    fun loadCounts() {
        viewModelScope.launch {
            musicRepository.getLocalLikedTracks().collect { tracks ->
                _likedSongsCount.value = tracks.size
            }
        }

        viewModelScope.launch {
            downloadsRepository.getAllDownloads().collect { downloads ->
                _downloadsCount.value = downloads.size
            }
        }

        viewModelScope.launch {
            musicRepository.getFollowedArtists().collect { artists ->
                _artistsCount.value = artists.size
            }
        }

        viewModelScope.launch {
            playlistRepository.getAllPlaylistsWithTracks().collect { playlists ->
                _playlistsCount.value = playlists.size
            }
        }
    }
}