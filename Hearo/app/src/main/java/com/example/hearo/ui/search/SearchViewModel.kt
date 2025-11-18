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

    private var searchJob: Job? = null

    init {
        loadSearchHistory()
        _searchState.value = UiState.Idle
    }

    /**
     * Поиск треков с debounce (задержка 500мс)
     */
    fun searchTracks(query: String) {
        // Отменяем предыдущий поиск
        searchJob?.cancel()

        if (query.isBlank()) {
            _searchState.value = UiState.Idle
            return
        }

        searchJob = viewModelScope.launch {
            // Debounce - ждем 500мс
            delay(500)

            _searchState.value = UiState.Loading

            musicRepository.searchTracks(query)
                .onSuccess { tracks ->
                    _searchState.value = UiState.Success(tracks)
                    loadSearchHistory() // Обновляем историю
                }
                .onFailure { error ->
                    _searchState.value = UiState.Error(error.message ?: "Search failed")
                }
        }
    }

    /**
     * Добавить/удалить из избранного
     */
    fun toggleFavorite(track: Track) {
        viewModelScope.launch {
            // Проверяем, в избранном ли трек
            musicRepository.isTrackSaved(track.id)
                .onSuccess { isSaved ->
                    if (isSaved) {
                        musicRepository.removeSavedTrack(track.id)
                    } else {
                        musicRepository.saveTrack(track.id)
                    }
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
}


