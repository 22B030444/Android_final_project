package com.example.hearo.ui.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.hearo.data.model.spotify.AlbumFull
import com.example.hearo.data.model.spotify.Track
import com.example.hearo.data.model.UiState
import com.example.hearo.data.preferences.AppPreferences
import com.example.hearo.data.repository.AuthRepository
import com.example.hearo.data.repository.MusicRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class SearchType {
    TRACKS,
    ALBUMS,
    ALL
}

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(AppPreferences(application))
    private val musicRepository = MusicRepository(application, authRepository)

    private val _searchTracksState = MutableLiveData<UiState<List<Track>>>()
    val searchTracksState: LiveData<UiState<List<Track>>> = _searchTracksState

    private val _searchAlbumsState = MutableLiveData<UiState<List<AlbumFull>>>()
    val searchAlbumsState: LiveData<UiState<List<AlbumFull>>> = _searchAlbumsState

    private val _currentSearchType = MutableLiveData<SearchType>(SearchType.TRACKS)
    val currentSearchType: LiveData<SearchType> = _currentSearchType

    private val _searchHistory = MutableLiveData<List<String>>()
    val searchHistory: LiveData<List<String>> = _searchHistory

    private val _likedTracksIds = MutableLiveData<Set<String>>()
    val likedTracksIds: LiveData<Set<String>> = _likedTracksIds

    private var searchJob: Job? = null

    init {
        loadSearchHistory()
        loadLikedTracksIds()
        _searchTracksState.value = UiState.Idle
        _searchAlbumsState.value = UiState.Idle
    }

    /**
     * Изменить тип поиска
     */
    fun setSearchType(type: SearchType) {
        _currentSearchType.value = type
    }

    /**
     * Универсальный поиск
     */
    fun search(query: String) {
        searchJob?.cancel()

        if (query.isBlank()) {
            _searchTracksState.value = UiState.Idle
            _searchAlbumsState.value = UiState.Idle
            return
        }

        when (_currentSearchType.value) {
            SearchType.TRACKS -> searchTracks(query)
            SearchType.ALBUMS -> searchAlbums(query)
            SearchType.ALL -> searchAll(query)
            null -> searchTracks(query)
        }
    }

    /**
     * Поиск только треков
     */
    private fun searchTracks(query: String) {
        searchJob = viewModelScope.launch {
            delay(500) // Debounce

            _searchTracksState.value = UiState.Loading

            musicRepository.searchTracks(query)
                .onSuccess { tracks ->
                    _searchTracksState.value = UiState.Success(tracks)
                    loadSearchHistory()
                    loadLikedTracksIds()
                }
                .onFailure { error ->
                    _searchTracksState.value = UiState.Error(error.message ?: "Search failed")
                }
        }
    }

    /**
     * Поиск только альбомов
     */
    private fun searchAlbums(query: String) {
        searchJob = viewModelScope.launch {
            delay(500) // Debounce

            _searchAlbumsState.value = UiState.Loading

            musicRepository.searchAlbums(query)
                .onSuccess { albums ->
                    _searchAlbumsState.value = UiState.Success(albums)
                    loadSearchHistory()
                }
                .onFailure { error ->
                    _searchAlbumsState.value = UiState.Error(error.message ?: "Search failed")
                }
        }
    }

    /**
     * Поиск треков и альбомов одновременно
     */
    private fun searchAll(query: String) {
        searchJob = viewModelScope.launch {
            delay(500) // Debounce

            _searchTracksState.value = UiState.Loading
            _searchAlbumsState.value = UiState.Loading

            musicRepository.searchAll(query)
                .onSuccess { (tracks, albums) ->
                    _searchTracksState.value = UiState.Success(tracks)
                    _searchAlbumsState.value = UiState.Success(albums)
                    loadSearchHistory()
                    loadLikedTracksIds()
                }
                .onFailure { error ->
                    _searchTracksState.value = UiState.Error(error.message ?: "Search failed")
                    _searchAlbumsState.value = UiState.Error(error.message ?: "Search failed")
                }
        }
    }

    fun toggleLocalLike(track: Track) {
        viewModelScope.launch {
            musicRepository.toggleLocalLike(track)
            loadLikedTracksIds()
        }
    }

    private fun loadSearchHistory() {
        _searchHistory.value = musicRepository.getSearchHistory()
    }

    fun clearSearchHistory() {
        musicRepository.clearSearchHistory()
        loadSearchHistory()
    }

    private fun loadLikedTracksIds() {
        viewModelScope.launch {
            musicRepository.getLocalLikedTracks().collect { tracks ->
                _likedTracksIds.value = tracks.map { it.id }.toSet()
            }
        }
    }
}