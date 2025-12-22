package com.example.hearo.ui.artist

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.hearo.data.model.MusicSource
import com.example.hearo.data.model.UniversalArtist
import com.example.hearo.data.model.UniversalTrack
import com.example.hearo.data.model.UiState
import com.example.hearo.data.repository.MusicRepository
import kotlinx.coroutines.launch

class ArtistDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val musicRepository = MusicRepository(application)

    private val _artistState = MutableLiveData<UiState<UniversalArtist>>()
    val artistState: LiveData<UiState<UniversalArtist>> = _artistState

    private val _tracksState = MutableLiveData<UiState<List<UniversalTrack>>>()
    val tracksState: LiveData<UiState<List<UniversalTrack>>> = _tracksState

    private val _isFollowing = MutableLiveData<Boolean>()
    val isFollowing: LiveData<Boolean> = _isFollowing

    private val _likedTracksIds = MutableLiveData<Set<String>>()
    val likedTracksIds: LiveData<Set<String>> = _likedTracksIds

    private val _albumImageUrl = MutableLiveData<String?>()
    val albumImageUrl: LiveData<String?> = _albumImageUrl

    private var currentArtist: UniversalArtist? = null
    private var currentTracks: List<UniversalTrack> = emptyList()

    init {
        loadLikedTracksIds()
    }

    fun loadArtistDetails(artistId: String, artistName: String? = null) {
        viewModelScope.launch {
            _artistState.value = UiState.Loading
            _tracksState.value = UiState.Loading

            _isFollowing.value = musicRepository.isArtistFollowed(artistId)

            musicRepository.getArtistDetails(artistId)
                .onSuccess { (artist, tracks) ->
                    val trackImageUrl = tracks.firstOrNull()?.imageUrl

                    val artistImageUrl = if (artistName != null) {
                        musicRepository.getArtistImage(artistName)
                    } else {
                        artist?.name?.let { musicRepository.getArtistImage(it) }
                    }

                    _albumImageUrl.value = artistImageUrl ?: trackImageUrl

                    if (artist != null) {
                        val updatedArtist = UniversalArtist(
                            id = artist.id,
                            name = artist.name,
                            imageUrl = artistImageUrl ?: trackImageUrl,
                            followersCount = artist.followersCount,
                            monthlyListeners = artist.monthlyListeners,
                            genres = artist.genres,
                            source = artist.source
                        )
                        currentArtist = updatedArtist
                        _artistState.value = UiState.Success(updatedArtist)
                    } else if (artistName != null) {
                        val tempArtist = UniversalArtist(
                            id = artistId,
                            name = artistName,
                            imageUrl = artistImageUrl ?: trackImageUrl,
                            followersCount = 0,
                            monthlyListeners = null,
                            genres = emptyList(),
                            source = MusicSource.ITUNES
                        )
                        currentArtist = tempArtist
                        _artistState.value = UiState.Success(tempArtist)
                    }

                    currentTracks = tracks
                    if (tracks.isEmpty()) {
                        _tracksState.value = UiState.Error("No songs found")
                    } else {
                        _tracksState.value = UiState.Success(tracks)
                    }

                    Log.d("ArtistDetailVM", "Loaded artist: ${artist?.name}, ${tracks.size} tracks, image: $artistImageUrl")
                }
                .onFailure { error ->
                    Log.e("ArtistDetailVM", "Failed to load artist", error)
                    _artistState.value = UiState.Error(error.message ?: "Failed to load artist")
                    _tracksState.value = UiState.Error(error.message ?: "Failed to load tracks")
                }
        }
    }

    fun loadAlbumDetails(albumId: String, albumName: String? = null) {
        viewModelScope.launch {
            _artistState.value = UiState.Loading
            _tracksState.value = UiState.Loading

            musicRepository.getAlbumDetails(albumId)
                .onSuccess { (album, tracks) ->
                    _albumImageUrl.value = album?.imageUrl ?: tracks.firstOrNull()?.imageUrl

                    val albumAsArtist = UniversalArtist(
                        id = albumId,
                        name = album?.name ?: albumName ?: "Unknown Album",
                        imageUrl = album?.imageUrl ?: tracks.firstOrNull()?.imageUrl,
                        followersCount = 0,
                        monthlyListeners = "${album?.totalTracks ?: tracks.size} tracks",
                        genres = listOf(album?.artistName ?: ""),
                        source = MusicSource.ITUNES
                    )
                    currentArtist = albumAsArtist
                    _artistState.value = UiState.Success(albumAsArtist)

                    currentTracks = tracks
                    if (tracks.isEmpty()) {
                        _tracksState.value = UiState.Error("No songs found")
                    } else {
                        _tracksState.value = UiState.Success(tracks)
                    }

                    Log.d("ArtistDetailVM", "Loaded album: ${album?.name}, ${tracks.size} tracks")
                }
                .onFailure { error ->
                    Log.e("ArtistDetailVM", "Failed to load album", error)
                    _artistState.value = UiState.Error(error.message ?: "Failed to load album")
                    _tracksState.value = UiState.Error(error.message ?: "Failed to load tracks")
                }
        }
    }

    fun toggleFollow() {
        val artist = currentArtist ?: return
        viewModelScope.launch {
            musicRepository.toggleFollowArtist(artist)
                .onSuccess { isNowFollowing ->
                    _isFollowing.value = isNowFollowing
                }
        }
    }

    fun toggleLikeTrack(track: UniversalTrack) {
        viewModelScope.launch {
            musicRepository.toggleLocalLike(track)
            loadLikedTracksIds()
        }
    }

    fun getCurrentTracks(): List<UniversalTrack> = currentTracks

    private fun loadLikedTracksIds() {
        viewModelScope.launch {
            musicRepository.getLocalLikedTracks().collect { tracks ->
                _likedTracksIds.value = tracks.map { it.id }.toSet()
            }
        }
    }
}