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

    // Текущий трек
    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack

    // Очередь треков (для Previous/Next)
    private val _playlist = MutableStateFlow<List<Track>>(emptyList())
    private var currentTrackIndex = 0

    // Состояния
    private val _isLiked = MutableStateFlow(false)
    val isLiked: StateFlow<Boolean> = _isLiked

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode

    private val _currentPosition = MutableStateFlow(0)
    val currentPosition: StateFlow<Int> = _currentPosition

    private val _duration = MutableStateFlow(0)
    val duration: StateFlow<Int> = _duration

    private var progressUpdateJob: Job? = null

    /**
     * Установить и воспроизвести трек
     */
    fun setTrack(track: Track, playlist: List<Track> = listOf(track)) {
        _currentTrack.value = track
        _playlist.value = playlist
        currentTrackIndex = playlist.indexOf(track).takeIf { it >= 0 } ?: 0

        // Проверяем, в избранном ли трек
        checkIfLiked(track.id)

        // Начинаем воспроизведение
        playCurrentTrack()
    }

    /**
     * Воспроизвести текущий трек
     */
    private fun playCurrentTrack() {
        val track = _currentTrack.value ?: return

        track.previewUrl?.let { url ->
            mediaPlayer.play(url)

            // Устанавливаем длительность (preview всегда ~30 сек, но берем реальную)
            viewModelScope.launch {
                // Ждем немного пока mediaPlayer подготовится
                delay(500)
                val realDuration = mediaPlayer.getDuration()
                _duration.value = if (realDuration > 0) realDuration else 30000
            }

            startProgressUpdate()
        } ?: run {
            // Нет preview URL
            mediaPlayer.stop()
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
     * Следующий трек
     */
    fun playNext() {
        val playlist = _playlist.value
        if (playlist.isEmpty()) return

        when (_repeatMode.value) {
            RepeatMode.ONE -> {
                // Повторяем текущий трек
                playCurrentTrack()
            }
            else -> {
                // Переходим к следующему
                currentTrackIndex = if (_isShuffle.value) {
                    // Случайный трек
                    playlist.indices.random()
                } else {
                    // Следующий по порядку
                    (currentTrackIndex + 1) % playlist.size
                }

                _currentTrack.value = playlist[currentTrackIndex]
                checkIfLiked(playlist[currentTrackIndex].id)
                playCurrentTrack()
            }
        }
    }

    /**
     * Предыдущий трек
     */
    fun playPrevious() {
        val playlist = _playlist.value
        if (playlist.isEmpty()) return

        // Если прошло более 3 секунд - перематываем в начало
        if (mediaPlayer.getCurrentPosition() > 3000) {
            mediaPlayer.seekTo(0)
            _currentPosition.value = 0
            return
        }

        // Иначе переходим к предыдущему треку
        currentTrackIndex = if (currentTrackIndex > 0) {
            currentTrackIndex - 1
        } else {
            playlist.size - 1
        }

        _currentTrack.value = playlist[currentTrackIndex]
        checkIfLiked(playlist[currentTrackIndex].id)
        playCurrentTrack()
    }

    /**
     * Переключить Shuffle
     */
    fun toggleShuffle() {
        _isShuffle.value = !_isShuffle.value
    }

    /**
     * Переключить Repeat
     */
    fun toggleRepeat() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
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
                val currentPos = mediaPlayer.getCurrentPosition()
                _currentPosition.value = currentPos

                // Проверяем конец трека
                val duration = mediaPlayer.getDuration()
                if (duration > 0 && currentPos >= duration - 100) {
                    // Трек закончился
                    handleTrackCompletion()
                    break
                }

                delay(100) // Обновляем каждые 100мс
            }
        }
    }

    private fun stopProgressUpdate() {
        progressUpdateJob?.cancel()
        progressUpdateJob = null
    }

    /**
     * Обработка завершения трека
     */
    private fun handleTrackCompletion() {
        when (_repeatMode.value) {
            RepeatMode.ONE -> {
                // Повторяем текущий
                playCurrentTrack()
            }
            RepeatMode.ALL -> {
                // Переходим к следующему
                playNext()
            }
            RepeatMode.OFF -> {
                // Переходим к следующему если не последний
                val playlist = _playlist.value
                if (currentTrackIndex < playlist.size - 1) {
                    playNext()
                } else {
                    // Это был последний трек - останавливаемся
                    stopProgressUpdate()
                    _currentPosition.value = 0
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer.release()
        stopProgressUpdate()
    }
}

/**
 * Режимы повтора
 */
enum class RepeatMode {
    OFF,    // Не повторять
    ALL,    // Повторять весь плейлист
    ONE     // Повторять один трек
}


