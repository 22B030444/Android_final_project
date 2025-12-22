package com.example.hearo.ui.player

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hearo.data.model.UniversalTrack
import com.example.hearo.data.repository.DownloadsRepository
import com.example.hearo.data.repository.MusicRepository
import com.example.hearo.service.MusicPlayerService
import com.example.hearo.utils.DownloadProgress
import com.example.hearo.utils.TrackDownloadManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class RepeatMode {
    OFF,
    ALL,
    ONE
}

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val musicRepository = MusicRepository(application)
    private val downloadsRepository = DownloadsRepository(application)

    private val downloadManager = TrackDownloadManager(application)
    val downloadProgress: StateFlow<DownloadProgress> = downloadManager.downloadProgress

    val currentTrack: StateFlow<UniversalTrack?> = MusicPlayerService.currentTrack
    val isPlaying: StateFlow<Boolean> = MusicPlayerService.isPlaying
    val isPreparing: StateFlow<Boolean> = MusicPlayerService.isPreparing
    val currentPosition: StateFlow<Int> = MusicPlayerService.currentPosition
    val duration: StateFlow<Int> = MusicPlayerService.duration
    val error: StateFlow<String?> = MusicPlayerService.error

    private val _isLiked = MutableStateFlow(false)
    val isLiked: StateFlow<Boolean> = _isLiked

    private val _isDownloaded = MutableStateFlow(false)
    val isDownloaded: StateFlow<Boolean> = _isDownloaded

    private val _showMessage = MutableStateFlow<String?>(null)
    val showMessage: StateFlow<String?> = _showMessage

    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode

    init {

        _isShuffleEnabled.value = MusicPlayerService.isShuffleEnabled()
        _repeatMode.value = when (MusicPlayerService.getRepeatMode()) {
            1 -> RepeatMode.ALL
            2 -> RepeatMode.ONE
            else -> RepeatMode.OFF
        }
    }

    fun setTrackList(tracks: List<UniversalTrack>, startIndex: Int = 0) {
        MusicPlayerService.setTrackList(tracks, startIndex)
    }

    fun setTrack(track: UniversalTrack) {
        Log.d("PlayerViewModel", "=== SET TRACK: ${track.name} ===")

        checkIfLiked(track.id)
        checkIfDownloaded(track.id)

        if (track.previewUrl.isNullOrEmpty()) {
            _showMessage.value = "No preview available for this track"
            return
        }

        MusicPlayerService.play(track)
    }

    fun togglePlayPause() {
        MusicPlayerService.togglePlayPause()
    }

    fun playPrevious() {
        MusicPlayerService.playPrevious()
        MusicPlayerService.currentTrack.value?.let {
            checkIfLiked(it.id)
            checkIfDownloaded(it.id)
        }
    }

    fun playNext() {
        MusicPlayerService.playNext()
        MusicPlayerService.currentTrack.value?.let {
            checkIfLiked(it.id)
            checkIfDownloaded(it.id)
        }
    }

    fun toggleShuffle() {
        val enabled = MusicPlayerService.toggleShuffle()
        _isShuffleEnabled.value = enabled
        _showMessage.value = if (enabled) "Shuffle on" else "Shuffle off"
    }

    fun toggleRepeat() {
        val mode = MusicPlayerService.toggleRepeat()
        _repeatMode.value = when (mode) {
            1 -> {
                _showMessage.value = "Repeat all"
                RepeatMode.ALL
            }
            2 -> {
                _showMessage.value = "Repeat one"
                RepeatMode.ONE
            }
            else -> {
                _showMessage.value = "Repeat off"
                RepeatMode.OFF
            }
        }
    }

    fun seekTo(position: Int) {
        MusicPlayerService.seekTo(position)
    }

    fun toggleLike() {
        val track = MusicPlayerService.currentTrack.value ?: return
        viewModelScope.launch {
            musicRepository.toggleLocalLike(track)
            checkIfLiked(track.id)
        }
    }

    fun downloadTrack() {
        val track = MusicPlayerService.currentTrack.value ?: return

        viewModelScope.launch {
            if (downloadsRepository.isTrackDownloaded(track.id)) {
                _showMessage.value = "Track already downloaded"
                return@launch
            }

            if (track.canDownloadFull && !track.downloadUrl.isNullOrEmpty()) {
                downloadManager.downloadTrack(track, isFull = true)
                _showMessage.value = "Downloading full track..."
            } else if (!track.previewUrl.isNullOrEmpty()) {
                downloadManager.downloadTrack(track, isFull = false)
                _showMessage.value = "Downloading preview..."
            } else {
                _showMessage.value = "No download available"
            }
        }
    }

    private fun checkIfLiked(trackId: String) {
        viewModelScope.launch {
            _isLiked.value = musicRepository.isTrackLikedLocally(trackId)
        }
    }

    private fun checkIfDownloaded(trackId: String) {
        viewModelScope.launch {
            _isDownloaded.value = downloadsRepository.isTrackDownloaded(trackId)
        }
    }

    fun clearMessage() {
        _showMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        downloadManager.release()
    }
}