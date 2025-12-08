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

enum class RepeatMode {
    OFF,      // Не повторять
    ALL,      // Повторять весь список
    ONE       // Повторять один трек
}

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val musicRepository = MusicRepository(application)
    val mediaPlayer = MediaPlayerManager()

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

    // ⭐ Режимы воспроизведения
    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode

    private var progressUpdateJob: Job? = null

    private var trackList: List<UniversalTrack> = emptyList()
    private var currentTrackIndex: Int = 0
    private var shuffledIndices: List<Int> = emptyList()
    private var currentShufflePosition: Int = 0

    /**
     * Установить список треков с начальным индексом
     */
    fun setTrackList(tracks: List<UniversalTrack>, startIndex: Int = 0) {
        trackList = tracks
        currentTrackIndex = startIndex.coerceIn(0, tracks.size - 1)

        // Создаем перемешанный список индексов
        shuffledIndices = trackList.indices.shuffled()
        currentShufflePosition = shuffledIndices.indexOf(currentTrackIndex)

        Log.d("PlayerViewModel", "Track list set: ${tracks.size} tracks, starting at index $currentTrackIndex")
    }

    /**
     * Установить и воспроизвести трек
     */
    fun setTrack(track: UniversalTrack) {
        Log.d("PlayerViewModel", "=== SET TRACK: ${track.name} ===")
        _currentTrack.value = track
        checkIfLiked(track.id)

        if (track.previewUrl.isNullOrEmpty()) {
            _showMessage.value = "No preview available for this track"
            return
        }

        mediaPlayer.play(track.previewUrl)
        updateDuration(track)
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
     * ⭐ Предыдущий трек
     */
    fun playPrevious() {
        if (trackList.isEmpty()) {
            _showMessage.value = "No previous track available"
            return
        }

        // Если трек воспроизводился больше 3 секунд, перематываем в начало
        if (mediaPlayer.getCurrentPosition() > 3000) {
            mediaPlayer.seekTo(0)
            _currentPosition.value = 0
            return
        }

        // Иначе переходим к предыдущему треку
        if (_isShuffleEnabled.value) {
            // В режиме shuffle
            currentShufflePosition = if (currentShufflePosition > 0) {
                currentShufflePosition - 1
            } else {
                shuffledIndices.size - 1
            }
            currentTrackIndex = shuffledIndices[currentShufflePosition]
        } else {
            // В обычном режиме
            currentTrackIndex = if (currentTrackIndex > 0) {
                currentTrackIndex - 1
            } else {
                trackList.size - 1
            }
        }

        val previousTrack = trackList[currentTrackIndex]
        Log.d("PlayerViewModel", "Playing previous: ${previousTrack.name} (index: $currentTrackIndex)")
        setTrack(previousTrack)
    }

    /**
     * ⭐ Следующий трек
     */
    fun playNext() {
        if (trackList.isEmpty()) {
            _showMessage.value = "No next track available"
            return
        }

        if (_isShuffleEnabled.value) {
            // В режиме shuffle
            currentShufflePosition = (currentShufflePosition + 1) % shuffledIndices.size
            currentTrackIndex = shuffledIndices[currentShufflePosition]
        } else {
            // В обычном режиме
            currentTrackIndex = (currentTrackIndex + 1) % trackList.size
        }

        val nextTrack = trackList[currentTrackIndex]
        Log.d("PlayerViewModel", "Playing next: ${nextTrack.name} (index: $currentTrackIndex)")
        setTrack(nextTrack)
    }

    /**
     * ⭐ Переключить Shuffle
     */
    fun toggleShuffle() {
        _isShuffleEnabled.value = !_isShuffleEnabled.value

        if (_isShuffleEnabled.value) {
            // Включили shuffle - создаем новый перемешанный список
            shuffledIndices = trackList.indices.shuffled()
            currentShufflePosition = shuffledIndices.indexOf(currentTrackIndex)
            _showMessage.value = "Shuffle on"
            Log.d("PlayerViewModel", "Shuffle enabled")
        } else {
            _showMessage.value = "Shuffle off"
            Log.d("PlayerViewModel", "Shuffle disabled")
        }
    }

    /**
     * ⭐ Переключить режим повтора
     */
    fun toggleRepeat() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.OFF -> {
                _showMessage.value = "Repeat all"
                RepeatMode.ALL
            }
            RepeatMode.ALL -> {
                _showMessage.value = "Repeat one"
                RepeatMode.ONE
            }
            RepeatMode.ONE -> {
                _showMessage.value = "Repeat off"
                RepeatMode.OFF
            }
        }
        Log.d("PlayerViewModel", "Repeat mode: ${_repeatMode.value}")
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

        if (track.canDownloadFull && !track.downloadUrl.isNullOrEmpty()) {
            downloadManager.downloadTrack(
                url = track.downloadUrl,
                trackName = track.name,
                artistName = track.artistName,
                isFull = true
            )
            _showMessage.value = "Downloading full track..."
        } else if (!track.previewUrl.isNullOrEmpty()) {
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

    private fun updateDuration(track: UniversalTrack) {
        viewModelScope.launch {
            delay(500)
            val realDuration = mediaPlayer.getDuration()
            _duration.value = if (realDuration > 0) realDuration else track.durationMs
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
     * ⭐ Обработка окончания трека с учетом режимов повтора
     */
    private fun onTrackCompleted() {
        when (_repeatMode.value) {
            RepeatMode.ONE -> {
                // Повторяем текущий трек
                val track = _currentTrack.value
                if (track != null) {
                    setTrack(track)
                }
            }
            RepeatMode.ALL -> {
                // Переходим к следующему треку
                playNext()
            }
            RepeatMode.OFF -> {
                // Переходим к следующему, если не последний
                if (_isShuffleEnabled.value) {
                    if (currentShufflePosition < shuffledIndices.size - 1) {
                        playNext()
                    }
                } else {
                    if (currentTrackIndex < trackList.size - 1) {
                        playNext()
                    }
                }
            }
        }
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
                    // ⭐ Вызываем обработку окончания трека
                    onTrackCompleted()
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
        mediaPlayer.release()
        downloadManager.release()
        stopProgressUpdate()
    }
}

