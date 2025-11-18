package com.example.hearo.ui.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.hearo.data.model.spotify.Track
import com.example.hearo.data.model.UiState
import com.example.hearo.data.preferences.AppPreferences
import com.example.hearo.data.repository.AuthRepository
import com.example.hearo.data.repository.MusicRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(AppPreferences(application))
    private val musicRepository = MusicRepository(application, authRepository)

    private val _searchState = MutableLiveData<UiState<List<Track>>>()
    val searchState: LiveData<UiState<List<Track>>> = _searchState

    private val _searchHistory = MutableLiveData<List<String>>()
    val searchHistory: LiveData<List<String>> = _searchHistory

    // Храним состояние лайков для каждого трека
    private val _likedTracksIds = MutableLiveData<Set<String>>()
    val likedTracksIds: LiveData<Set<String>> = _likedTracksIds

    private var searchJob: Job? = null

    init {
        loadSearchHistory()
        loadLikedTracksIds()
        _searchState.value = UiState.Idle
    }

    fun searchTracks(query: String) {
        searchJob?.cancel()

        if (query.isBlank()) {
            _searchState.value = UiState.Idle
            return
        }

        searchJob = viewModelScope.launch {
            delay(500) // Debounce

            _searchState.value = UiState.Loading

            musicRepository.searchTracks(query)
                .onSuccess { tracks ->
                    _searchState.value = UiState.Success(tracks)
                    loadSearchHistory()
                    loadLikedTracksIds() // Обновляем состояние лайков
                }
                .onFailure { error ->
                    _searchState.value = UiState.Error(error.message ?: "Search failed")
                }
        }
    }

    /**
     * Переключить локальное избранное
     */
    fun toggleLocalLike(track: Track) {
        viewModelScope.launch {
            musicRepository.toggleLocalLike(track)
                .onSuccess { isNowLiked ->
                    // Обновляем список лайкнутых ID
                    loadLikedTracksIds()
                }
                .onFailure { error ->
                    // Можно показать Toast через LiveData
                }
        }
    }

    private fun loadSearchHistory() {
        _searchHistory.value = musicRepository.getSearchHistory()
    }

    fun clearSearchHistory() {
        musicRepository.clearSearchHistory()
        loadSearchHistory()
    }

    /**
     * Загружаем ID всех лайкнутых треков для отображения сердечка
     */
    private fun loadLikedTracksIds() {
        viewModelScope.launch {
            musicRepository.getLocalLikedTracks().collect { tracks ->
                _likedTracksIds.value = tracks.map { it.id }.toSet()
            }
        }
    }
}


