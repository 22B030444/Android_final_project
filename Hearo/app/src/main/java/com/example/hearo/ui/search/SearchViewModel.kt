package com.example.hearo.ui.search

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.hearo.data.model.MusicSource
import com.example.hearo.data.model.UniversalTrack
import com.example.hearo.data.model.UiState
import com.example.hearo.data.repository.MusicRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class SearchFilter {
    ALL,      // iTunes + Jamendo
    ITUNES,   // Только iTunes
    JAMENDO   // Только Jamendo
}

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val musicRepository = MusicRepository(application)

    private val _searchState = MutableLiveData<UiState<List<UniversalTrack>>>()
    val searchState: LiveData<UiState<List<UniversalTrack>>> = _searchState

    private val _currentFilter = MutableLiveData<SearchFilter>(SearchFilter.ALL)
    val currentFilter: LiveData<SearchFilter> = _currentFilter

    private val _likedTracksIds = MutableLiveData<Set<String>>()
    val likedTracksIds: LiveData<Set<String>> = _likedTracksIds

    private var searchJob: Job? = null

    init {
        loadLikedTracksIds()
        _searchState.value = UiState.Idle
    }

    /**
     * Установить фильтр источника
     */
    fun setFilter(filter: SearchFilter) {
        _currentFilter.value = filter
    }

    /**
     * Поиск с учетом фильтра
     */
    fun search(query: String) {
        searchJob?.cancel()

        if (query.isBlank()) {
            _searchState.value = UiState.Idle
            return
        }

        searchJob = viewModelScope.launch {
            delay(500) // Debounce

            _searchState.value = UiState.Loading

            when (_currentFilter.value) {
                SearchFilter.ALL -> searchAll(query)
                SearchFilter.ITUNES -> searchITunesOnly(query)
                SearchFilter.JAMENDO -> searchJamendoOnly(query)
                null -> searchAll(query)
            }
        }
    }

    /**
     * Поиск во всех источниках
     */
    private suspend fun searchAll(query: String) {
        musicRepository.searchBoth(query)
            .onSuccess { (itunesTracks, jamendoTracks) ->
                // Объединяем результаты
                val allTracks = itunesTracks + jamendoTracks

                Log.d("SearchViewModel", "Total: ${allTracks.size} tracks")
                _searchState.value = UiState.Success(allTracks)
                loadLikedTracksIds()
            }
            .onFailure { error ->
                _searchState.value = UiState.Error(error.message ?: "Search failed")
            }
    }

    /**
     * Поиск только в iTunes
     */
    private suspend fun searchITunesOnly(query: String) {
        musicRepository.searchITunes(query)
            .onSuccess { tracks ->
                _searchState.value = UiState.Success(tracks)
                loadLikedTracksIds()
            }
            .onFailure { error ->
                _searchState.value = UiState.Error(error.message ?: "iTunes search failed")
            }
    }

    /**
     * Поиск только в Jamendo
     */
    private suspend fun searchJamendoOnly(query: String) {
        musicRepository.searchJamendo(query)
            .onSuccess { tracks ->
                _searchState.value = UiState.Success(tracks)
                loadLikedTracksIds()
            }
            .onFailure { error ->
                _searchState.value = UiState.Error(error.message ?: "Jamendo search failed")
            }
    }

    /**
     * Переключить избранное
     */
    fun toggleLocalLike(track: UniversalTrack) {
        viewModelScope.launch {
            musicRepository.toggleLocalLike(track)
            loadLikedTracksIds()
        }
    }

    /**
     * Загрузить ID избранных треков
     */
    private fun loadLikedTracksIds() {
        viewModelScope.launch {
            musicRepository.getLocalLikedTracks().collect { tracks ->
                _likedTracksIds.value = tracks.map { it.id }.toSet()
            }
        }
    }
}