package com.example.hearo.ui.player

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hearo.data.model.UniversalTrack
import com.example.hearo.data.repository.MusicRepository
import com.example.hearo.utils.DownloadProgress
import com.example.hearo.utils.TrackDownloadManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val musicRepository = MusicRepository(application)

    val mediaPlayer = MediaPlayerManager()

    // ⭐ Download Manager
    private val downloadManager = TrackDownloadManager(application)
    val downloadProgress: StateFlow<DownloadProgress> = downloadManager.downloadProgress

    private val _currentTrack = MutableStateFlow<UniversalTrack?>(null)
    val currentTrack: StateFlow<UniversalTrack?> = _currentTrack

    private val _isLiked = MutableStateFlow(false)
    val isLiked: StateFlow<Boolean> = _isLiked

    private val _currentPosition = MutableStateFlow(0)
    val currentPosition: StateFlow<Int> = _currentPosition

    private val _duration = MutableStateFlow(0)
    val duration: StateFlow<Int> = _duration

    private val _showMessage = MutableStateFlow<String?>(null)
    val showMessage: StateFlow<String?> = _showMessage

    private var progressUpdateJob: Job? = null

    /**
     * Установить и воспроизвести трек
     */
    fun setTrack(track: UniversalTrack) {
        Log.d("PlayerViewModel", "=== SET TRACK ===")
        Log.d("PlayerViewModel", "Track: ${track.name}")
        Log.d("PlayerViewModel", "Source: ${track.source}")
        Log.d("PlayerViewModel", "Preview URL: ${track.previewUrl}")
        Log.d("PlayerViewModel", "Can download full: ${track.canDownloadFull}")

        _currentTrack.value = track
        checkIfLiked(track.id)

        // Проверяем наличие preview URL
        if (track.previewUrl.isNullOrEmpty()) {
            Log.e("PlayerViewModel", "❌ No preview URL!")
            _showMessage.value = "No preview available for this track"
            return
        }

        // Начинаем воспроизведение
        Log.d("PlayerViewModel", "Starting playback...")
        mediaPlayer.play(track.previewUrl)

        // Устанавливаем длительность
        viewModelScope.launch {
            delay(500)
            val realDuration = mediaPlayer.getDuration()
            _duration.value = if (realDuration > 0) realDuration else track.durationMs
            Log.d("PlayerViewModel", "Duration set: ${_duration.value}ms")
        }

        startProgressUpdate()
    }

    /**
     * Play/Pause
     */
    fun togglePlayPause() {
        if (mediaPlayer.isPlaying.value) {
            mediaPlayer.pause()
            stopProgressUpdate()
        } else {
            mediaPlayer.resume()
            startProgressUpdate()
        }
    }

    /**
     * Previous = перемотка в начало
     */
    fun playPrevious() {
        mediaPlayer.seekTo(0)
        _currentPosition.value = 0
    }

    /**
     * Next = сообщение
     */
    fun playNext() {
        _showMessage.value = "Playing preview mode"
    }

    /**
     * Перемотка
     */
    fun seekTo(position: Int) {
        mediaPlayer.seekTo(position)
        _currentPosition.value = position
    }

    /**
     * Переключить избранное
     */
    fun toggleLike() {
        val track = _currentTrack.value ?: return
        viewModelScope.launch {
            musicRepository.toggleLocalLike(track)
            checkIfLiked(track.id)
        }
    }

    /**
     * Скачать трек
     */
    fun downloadTrack() {
        val track = _currentTrack.value ?: return

        // Если доступен полный трек (Jamendo)
        if (track.canDownloadFull && !track.downloadUrl.isNullOrEmpty()) {
            Log.d("PlayerViewModel", "✅ Downloading FULL track from Jamendo")
            downloadManager.downloadTrack(
                url = track.downloadUrl,
                trackName = track.name,
                artistName = track.artistName,
                isFull = true
            )
            _showMessage.value = "Downloading full track..."
        }
        // Иначе скачиваем preview
        else if (!track.previewUrl.isNullOrEmpty()) {
            Log.d("PlayerViewModel", "⚠️ Downloading 30-90s preview")
            downloadManager.downloadTrack(
                url = track.previewUrl,
                trackName = track.name,
                artistName = track.artistName,
                isFull = false
            )
            _showMessage.value = "Downloading preview..."
        } else {
            _showMessage.value = "No download available"
        }
    }

    private fun checkIfLiked(trackId: String) {
        viewModelScope.launch {
            _isLiked.value = musicRepository.isTrackLikedLocally(trackId)
        }
    }

    fun clearMessage() {
        _showMessage.value = null
    }

    /**
     * Обновление прогресса
     */
    private fun startProgressUpdate() {
        stopProgressUpdate()
        progressUpdateJob = viewModelScope.launch {
            while (mediaPlayer.isPlaying.value) {
                val currentPos = mediaPlayer.getCurrentPosition()
                _currentPosition.value = currentPos

                val duration = mediaPlayer.getDuration()
                if (duration > 0 && currentPos >= duration - 100) {
                    stopProgressUpdate()
                    _currentPosition.value = 0
                    break
                }

                delay(100)
            }
        }
    }

    private fun stopProgressUpdate() {
        progressUpdateJob?.cancel()
        progressUpdateJob = null
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("PlayerViewModel", "ViewModel cleared")
        mediaPlayer.release()
        downloadManager.release()
        stopProgressUpdate()
    }
}