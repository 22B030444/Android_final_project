package com.example.hearo.ui.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hearo.data.model.spotify.Track
import com.example.hearo.data.preferences.AppPreferences
import com.example.hearo.data.repository.AuthRepository
import com.example.hearo.data.repository.MusicRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(AppPreferences(application))
    private val musicRepository = MusicRepository(application, authRepository)

    val mediaPlayer = MediaPlayerManager()

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack

    private val _isLiked = MutableStateFlow(false)
    val isLiked: StateFlow<Boolean> = _isLiked

    private val _currentPosition = MutableStateFlow(0)
    val currentPosition: StateFlow<Int> = _currentPosition

    private val _duration = MutableStateFlow(0)
    val duration: StateFlow<Int> = _duration

    private var progressUpdateJob: Job? = null

    /**
     * Установить и воспроизвести трек
     */
    fun setTrack(track: Track) {
        _currentTrack.value = track

        // Проверяем, в избранном ли трек
        checkIfLiked(track.id)

        // Начинаем воспроизведение
        track.previewUrl?.let { url ->
            mediaPlayer.play(url)
            _duration.value = 30000 // Превью всегда 30 секунд
            startProgressUpdate()
        }
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
     * Проверить, в избранном ли трек
     */
    private fun checkIfLiked(trackId: String) {
        viewModelScope.launch {
            val isLiked = musicRepository.isTrackLikedLocally(trackId)
            _isLiked.value = isLiked
        }
    }

    /**
     * Обновление прогресса воспроизведения
     */
    private fun startProgressUpdate() {
        stopProgressUpdate()
        progressUpdateJob = viewModelScope.launch {
            while (mediaPlayer.isPlaying.value) {
                _currentPosition.value = mediaPlayer.getCurrentPosition()
                delay(100) // Обновляем каждые 100мс
            }
        }
    }

    private fun stopProgressUpdate() {
        progressUpdateJob?.cancel()
        progressUpdateJob = null
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer.release()
        stopProgressUpdate()
    }
}