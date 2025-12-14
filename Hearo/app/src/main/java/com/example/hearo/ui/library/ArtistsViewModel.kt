package com.example.hearo.ui.library

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.hearo.data.model.UniversalArtist
import com.example.hearo.data.repository.MusicRepository
import kotlinx.coroutines.launch

class ArtistsViewModel(application: Application) : AndroidViewModel(application) {

    private val musicRepository = MusicRepository(application)

    private val _artists = MutableLiveData<List<UniversalArtist>>()
    val artists: LiveData<List<UniversalArtist>> = _artists

    private var allArtists: List<UniversalArtist> = emptyList()

    init {
        loadFollowedArtists()
    }

    private fun loadFollowedArtists() {
        viewModelScope.launch {
            musicRepository.getFollowedArtists().collect { artistsList ->
                allArtists = artistsList
                _artists.value = artistsList
            }
        }
    }

    fun searchArtists(query: String) {
        if (query.isEmpty()) {
            _artists.value = allArtists
        } else {
            _artists.value = allArtists.filter {
                it.name.contains(query, ignoreCase = true)
            }
        }
    }

    fun unfollowArtist(artist: UniversalArtist) {
        viewModelScope.launch {
            musicRepository.toggleFollowArtist(artist)
        }
    }
}


