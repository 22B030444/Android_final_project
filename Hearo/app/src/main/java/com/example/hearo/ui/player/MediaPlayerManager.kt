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

    /**
     * Воспроизвести трек по URL
     */
    fun play(url: String?) {
        if (url.isNullOrEmpty()) {
            Log.e("MediaPlayerManager", "URL is null or empty")
            return
        }

        try {
            // Останавливаем предыдущий плеер
            stop()

            // Создаем новый MediaPlayer
            mediaPlayer = MediaPlayer().apply {
                setDataSource(url)
                prepareAsync()

                setOnPreparedListener {
                    start()
                    _isPlaying.value = true
                    _duration.value = duration
                    Log.d("MediaPlayerManager", "Playing: $url, duration: $duration")
                }

                setOnCompletionListener {
                    _isPlaying.value = false
                    _currentPosition.value = 0
                    Log.d("MediaPlayerManager", "Playback completed")
                }

                setOnErrorListener { _, what, extra ->
                    Log.e("MediaPlayerManager", "Error: what=$what, extra=$extra")
                    _isPlaying.value = false
                    true
                }
            }

        } catch (e: Exception) {
            Log.e("MediaPlayerManager", "Failed to play", e)
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
                Log.d("MediaPlayerManager", "Paused")
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
                Log.d("MediaPlayerManager", "Resumed")
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
            } catch (e: Exception) {
                Log.e("MediaPlayerManager", "Error stopping", e)
            }
        }
        mediaPlayer = null
        _isPlaying.value = false
        _currentPosition.value = 0
        _duration.value = 0
    }

    /**
     * Перемотать на позицию
     */
    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
        _currentPosition.value = position
    }

    /**
     * Получить текущую позицию
     */
    fun getCurrentPosition(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }

    /**
     * Получить длительность
     */
    fun getDuration(): Int {
        return mediaPlayer?.duration ?: 0
    }

    /**
     * Очистить ресурсы
     */
    fun release() {
        stop()
    }
}