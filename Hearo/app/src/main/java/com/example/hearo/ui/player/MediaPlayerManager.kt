package com.example.hearo.ui.player

import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MediaPlayerManager {

    private var mediaPlayer: MediaPlayer? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _currentPosition = MutableStateFlow(0)
    val currentPosition: StateFlow<Int> = _currentPosition

    private val _duration = MutableStateFlow(0)
    val duration: StateFlow<Int> = _duration

    private val _isPreparing = MutableStateFlow(false)
    val isPreparing: StateFlow<Boolean> = _isPreparing

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    /**
     * Воспроизвести трек по URL
     */
    fun play(url: String?) {
        if (url.isNullOrEmpty()) {
            Log.e("MediaPlayerManager", "URL is null or empty")
            _error.value = "No preview available"
            return
        }

        try {
            // Останавливаем предыдущий плеер
            stop()

            _isPreparing.value = true
            _error.value = null

            // Создаем новый MediaPlayer
            mediaPlayer = MediaPlayer().apply {
                setDataSource(url)

                // Подготовка асинхронная
                prepareAsync()

                setOnPreparedListener {
                    _isPreparing.value = false
                    _duration.value = duration

                    // Автоматически начинаем воспроизведение
                    start()
                    _isPlaying.value = true

                    Log.d("MediaPlayerManager", "Playing: $url, duration: ${duration}ms")
                }

                setOnCompletionListener {
                    _isPlaying.value = false
                    _currentPosition.value = 0
                    Log.d("MediaPlayerManager", "Playback completed")
                }

                setOnErrorListener { mp, what, extra ->
                    Log.e("MediaPlayerManager", "Error: what=$what, extra=$extra")
                    _isPreparing.value = false
                    _isPlaying.value = false
                    _error.value = "Failed to play track"
                    true
                }

                setOnBufferingUpdateListener { mp, percent ->
                    Log.d("MediaPlayerManager", "Buffering: $percent%")
                }
            }

        } catch (e: Exception) {
            Log.e("MediaPlayerManager", "Failed to play", e)
            _isPreparing.value = false
            _error.value = e.message
        }
    }

    /**
     * Пауза
     */
    fun pause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                _isPlaying.value = false
                Log.d("MediaPlayerManager", "Paused at ${it.currentPosition}ms")
            }
        }
    }

    /**
     * Возобновить
     */
    fun resume() {
        mediaPlayer?.let {
            if (!it.isPlaying) {
                it.start()
                _isPlaying.value = true
                Log.d("MediaPlayerManager", "Resumed from ${it.currentPosition}ms")
            }
        }
    }

    /**
     * Остановить
     */
    fun stop() {
        mediaPlayer?.let {
            try {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
                Log.d("MediaPlayerManager", "Stopped and released")
            } catch (e: Exception) {
                Log.e("MediaPlayerManager", "Error stopping", e)
            }
        }
        mediaPlayer = null
        _isPlaying.value = false
        _isPreparing.value = false
        _currentPosition.value = 0
        _duration.value = 0
    }

    /**
     * Перемотать на позицию
     */
    fun seekTo(position: Int) {
        mediaPlayer?.let {
            try {
                it.seekTo(position)
                _currentPosition.value = position
                Log.d("MediaPlayerManager", "Seeked to ${position}ms")
            } catch (e: Exception) {
                Log.e("MediaPlayerManager", "Error seeking", e)
            }
        }
    }

    /**
     * Получить текущую позицию
     */
    fun getCurrentPosition(): Int {
        return try {
            mediaPlayer?.currentPosition ?: 0
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Получить длительность
     */
    fun getDuration(): Int {
        return try {
            mediaPlayer?.duration ?: 0
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Проверка идет ли воспроизведение
     */
    fun isCurrentlyPlaying(): Boolean {
        return try {
            mediaPlayer?.isPlaying ?: false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Очистить ресурсы
     */
    fun release() {
        stop()
    }
}