package com.example.hearo.ui.search

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.hearo.data.model.UniversalAlbum
import com.example.hearo.data.model.UniversalArtist
import com.example.hearo.data.model.UniversalTrack
import com.example.hearo.data.model.UiState
import com.example.hearo.data.repository.MusicRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class SearchFilter {
    ALL,
    ITUNES,
    JAMENDO
}

enum class SearchType {
    TRACKS,
    ALBUMS,
    ARTISTS
}

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val musicRepository = MusicRepository(application)

    // Tracks state
    private val _searchState = MutableLiveData<UiState<List<UniversalTrack>>>()
    val searchState: LiveData<UiState<List<UniversalTrack>>> = _searchState

    // Albums state
    private val _albumsState = MutableLiveData<UiState<List<UniversalAlbum>>>()
    val albumsState: LiveData<UiState<List<UniversalAlbum>>> = _albumsState

    // Artists state
    private val _artistsState = MutableLiveData<UiState<List<UniversalArtist>>>()
    val artistsState: LiveData<UiState<List<UniversalArtist>>> = _artistsState

    private val _currentFilter = MutableLiveData(SearchFilter.ALL)
    val currentFilter: LiveData<SearchFilter> = _currentFilter

    private val _currentSearchType = MutableLiveData(SearchType.TRACKS)
    val currentSearchType: LiveData<SearchType> = _currentSearchType

    private val _likedTracksIds = MutableLiveData<Set<String>>()
    val likedTracksIds: LiveData<Set<String>> = _likedTracksIds

    private var searchJob: Job? = null
    private var currentQuery: String = ""

    init {
        loadLikedTracksIds()
        _searchState.value = UiState.Idle
        _albumsState.value = UiState.Idle
        _artistsState.value = UiState.Idle
    }

    fun setFilter(filter: SearchFilter) {
        _currentFilter.value = filter
        if (currentQuery.isNotBlank()) {
            search(currentQuery)
        }
    }

    fun setSearchType(type: SearchType) {
        _currentSearchType.value = type
        if (currentQuery.isNotBlank()) {
            search(currentQuery)
        }
    }

    fun search(query: String) {
        searchJob?.cancel()
        currentQuery = query

        if (query.isBlank()) {
            _searchState.value = UiState.Idle
            _albumsState.value = UiState.Idle
            _artistsState.value = UiState.Idle
            return
        }

        searchJob = viewModelScope.launch {
            delay(500) // Debounce

            when (_currentSearchType.value) {
                SearchType.TRACKS -> searchTracks(query)
                SearchType.ALBUMS -> searchAlbums(query)
                SearchType.ARTISTS -> searchArtists(query)
                null -> searchTracks(query)
            }
        }
    }

    private suspend fun searchTracks(query: String) {
        _searchState.value = UiState.Loading

        when (_currentFilter.value) {
            SearchFilter.ALL -> searchAllTracks(query)
            SearchFilter.ITUNES -> searchITunesOnly(query)
            SearchFilter.JAMENDO -> searchJamendoOnly(query)
            null -> searchAllTracks(query)
        }
    }

    private suspend fun searchAllTracks(query: String) {
        musicRepository.searchBoth(query)
            .onSuccess { (itunesTracks, jamendoTracks) ->
                val allTracks = itunesTracks + jamendoTracks
                Log.d("SearchViewModel", "Total: ${allTracks.size} tracks")
                _searchState.value = UiState.Success(allTracks)
                loadLikedTracksIds()
            }
            .onFailure { error ->
                _searchState.value = UiState.Error(error.message ?: "Search failed")
            }
    }

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

    private suspend fun searchAlbums(query: String) {
        _albumsState.value = UiState.Loading

        musicRepository.searchAlbums(query)
            .onSuccess { albums ->
                Log.d("SearchViewModel", "Found ${albums.size} albums")
                _albumsState.value = UiState.Success(albums)
            }
            .onFailure { error ->
                _albumsState.value = UiState.Error(error.message ?: "Album search failed")
            }
    }

    private suspend fun searchArtists(query: String) {
        _artistsState.value = UiState.Loading

        musicRepository.searchArtists(query)
            .onSuccess { artists ->
                Log.d("SearchViewModel", "Found ${artists.size} artists")
                _artistsState.value = UiState.Success(artists)
            }
            .onFailure { error ->
                _artistsState.value = UiState.Error(error.message ?: "Artist search failed")
            }
    }

    fun toggleLocalLike(track: UniversalTrack) {
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
}
