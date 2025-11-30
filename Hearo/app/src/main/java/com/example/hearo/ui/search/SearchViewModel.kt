package com.example.hearo.ui.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.hearo.data.model.spotify.AlbumFull
import com.example.hearo.data.model.spotify.ArtistFull
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
    ARTISTS
}

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(AppPreferences(application))
    private val musicRepository = MusicRepository(application, authRepository)

    private val _searchTracksState = MutableLiveData<UiState<List<Track>>>()
    val searchTracksState: LiveData<UiState<List<Track>>> = _searchTracksState

    private val _searchAlbumsState = MutableLiveData<UiState<List<AlbumFull>>>()
    val searchAlbumsState: LiveData<UiState<List<AlbumFull>>> = _searchAlbumsState

    private val _searchArtistsState = MutableLiveData<UiState<List<ArtistFull>>>()
    val searchArtistsState: LiveData<UiState<List<ArtistFull>>> = _searchArtistsState

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
        _searchArtistsState.value = UiState.Idle
    }

    fun setSearchType(type: SearchType) {
        _currentSearchType.value = type
    }

    fun search(query: String) {
        searchJob?.cancel()

        if (query.isBlank()) {
            _searchTracksState.value = UiState.Idle
            _searchAlbumsState.value = UiState.Idle
            _searchArtistsState.value = UiState.Idle
            return
        }

        when (_currentSearchType.value) {
            SearchType.TRACKS -> searchTracks(query)
            SearchType.ALBUMS -> searchAlbums(query)
            SearchType.ARTISTS -> searchArtists(query)
            null -> searchTracks(query)
        }
    }

    private fun searchTracks(query: String) {
        searchJob = viewModelScope.launch {
            delay(500)

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

    private fun searchAlbums(query: String) {
        searchJob = viewModelScope.launch {
            delay(500)

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

    private fun searchArtists(query: String) {
        searchJob = viewModelScope.launch {
            delay(500)

            _searchArtistsState.value = UiState.Loading

            musicRepository.searchArtists(query)
                .onSuccess { artists ->
                    _searchArtistsState.value = UiState.Success(artists)
                    loadSearchHistory()
                }
                .onFailure { error ->
                    _searchArtistsState.value = UiState.Error(error.message ?: "Search failed")
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