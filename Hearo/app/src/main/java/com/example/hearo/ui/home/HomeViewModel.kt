package com.example.hearo.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.hearo.data.model.UniversalTrack
import com.example.hearo.data.repository.HistoryRepository
import com.example.hearo.data.repository.MusicRepository
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val historyRepository = HistoryRepository(application)
    private val musicRepository = MusicRepository(application)

    val recentlyPlayed: LiveData<List<UniversalTrack>> =
        historyRepository.getRecentlyPlayed(10).asLiveData()

    private val _trendingTracks = MutableLiveData<List<UniversalTrack>>()
    val trendingTracks: LiveData<List<UniversalTrack>> = _trendingTracks

    private val _recommendations = MutableLiveData<List<UniversalTrack>>()
    val recommendations: LiveData<List<UniversalTrack>> = _recommendations

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _likedTracksIds = MutableLiveData<Set<String>>()
    val likedTracksIds: LiveData<Set<String>> = _likedTracksIds

    init {
        loadLikedTracksIds()
        loadTrending()
        loadRecommendations()
    }

    fun refresh() {
        loadTrending()
        loadRecommendations()
    }

    private fun loadTrending() {
        viewModelScope.launch {
            _isLoading.value = true
            musicRepository.searchITunes("top hits 2024", limit = 10)
                .onSuccess { tracks ->
                    _trendingTracks.value = tracks
                }
            _isLoading.value = false
        }
    }

    private fun loadRecommendations() {
        viewModelScope.launch {
            val recent = historyRepository.getRecentlyPlayedList(5)
            if (recent.isNotEmpty()) {
                val randomArtist = recent.random().artistName
                musicRepository.searchITunes(randomArtist, limit = 10)
                    .onSuccess { tracks ->
                        val recentIds = recent.map { it.id }.toSet()
                        _recommendations.value = tracks.filter { it.id !in recentIds }
                    }
            } else {
                musicRepository.searchITunes("popular music", limit = 10)
                    .onSuccess { tracks ->
                        _recommendations.value = tracks
                    }
            }
        }
    }

    fun toggleLike(track: UniversalTrack) {
        viewModelScope.launch {
            musicRepository.toggleLocalLike(track)
            loadLikedTracksIds()
        }
    }

    private fun loadLikedTracksIds() {
        viewModelScope.launch {
            musicRepository.getLocalLikedTracks().collect { tracks ->
                _likedTracksIds.value = tracks.map { it.id }.toSet()
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            historyRepository.clearHistory()
        }
    }
}