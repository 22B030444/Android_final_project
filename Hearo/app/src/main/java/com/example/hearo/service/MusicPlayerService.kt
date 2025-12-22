package com.example.hearo.service

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.example.hearo.data.model.UniversalTrack
import com.example.hearo.data.repository.HistoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

object MusicPlayerService {

    private var mediaPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var historyRepository: HistoryRepository? = null
    private var progressJob: Job? = null

    private val _currentTrack = MutableStateFlow<UniversalTrack?>(null)
    val currentTrack: StateFlow<UniversalTrack?> = _currentTrack

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _isPreparing = MutableStateFlow(false)
    val isPreparing: StateFlow<Boolean> = _isPreparing

    private val _currentPosition = MutableStateFlow(0)
    val currentPosition: StateFlow<Int> = _currentPosition

    private val _duration = MutableStateFlow(0)
    val duration: StateFlow<Int> = _duration

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var trackList: List<UniversalTrack> = emptyList()
    private var currentTrackIndex: Int = 0
    private var isShuffleEnabled = false
    private var shuffledIndices: List<Int> = emptyList()
    private var currentShufflePosition = 0

    private var repeatMode = 0

    fun init(context: Context) {
        if (historyRepository == null) {
            historyRepository = HistoryRepository(context.applicationContext)
        }
    }

    fun setTrackList(tracks: List<UniversalTrack>, startIndex: Int = 0) {
        trackList = tracks
        currentTrackIndex = startIndex.coerceIn(0, tracks.size - 1)
        shuffledIndices = trackList.indices.shuffled()
        currentShufflePosition = shuffledIndices.indexOf(currentTrackIndex)
        Log.d("MusicPlayerService", "Track list set: ${tracks.size} tracks, starting at $currentTrackIndex")
    }

    fun play(track: UniversalTrack) {
        Log.d("MusicPlayerService", "Playing: ${track.name}")

        _currentTrack.value = track
        _error.value = null
        scope.launch(Dispatchers.IO) {
            historyRepository?.addToHistory(track)
        }

        if (track.previewUrl.isNullOrEmpty()) {
            _error.value = "No preview available"
            Log.w("MusicPlayerService", "No preview URL")
            return
        }

        try {
            stopProgressUpdate()
            mediaPlayer?.release()
            _isPreparing.value = true

            mediaPlayer = MediaPlayer().apply {
                setDataSource(track.previewUrl)

                setOnPreparedListener { mp ->
                    _isPreparing.value = false
                    mp.start()
                    _isPlaying.value = true
                    _duration.value = mp.duration
                    startProgressUpdate()
                    Log.d("MusicPlayerService", "Started playing, duration: ${mp.duration}ms")
                }

                setOnCompletionListener {
                    Log.d("MusicPlayerService", "Track completed")
                    _isPlaying.value = false
                    _currentPosition.value = 0
                    onTrackCompleted()
                }

                setOnErrorListener { _, what, extra ->
                    _isPreparing.value = false
                    _isPlaying.value = false
                    _error.value = "Playback error: $what"
                    Log.e("MusicPlayerService", "Error: what=$what, extra=$extra")
                    true
                }

                prepareAsync()
            }
        } catch (e: Exception) {
            _isPreparing.value = false
            _error.value = e.message
            Log.e("MusicPlayerService", "Error playing track", e)
        }
    }

    fun togglePlayPause() {
        mediaPlayer?.let { mp ->
            if (mp.isPlaying) {
                mp.pause()
                _isPlaying.value = false
                stopProgressUpdate()
                Log.d("MusicPlayerService", "Paused")
            } else {
                mp.start()
                _isPlaying.value = true
                startProgressUpdate()
                Log.d("MusicPlayerService", "Resumed")
            }
        }
    }

    fun pause() {
        mediaPlayer?.let { mp ->
            if (mp.isPlaying) {
                mp.pause()
                _isPlaying.value = false
                stopProgressUpdate()
            }
        }
    }

    fun resume() {
        mediaPlayer?.let { mp ->
            if (!mp.isPlaying) {
                mp.start()
                _isPlaying.value = true
                startProgressUpdate()
            }
        }
    }

    fun playNext() {
        if (trackList.isEmpty()) return

        if (isShuffleEnabled) {
            currentShufflePosition = (currentShufflePosition + 1) % shuffledIndices.size
            currentTrackIndex = shuffledIndices[currentShufflePosition]
        } else {
            currentTrackIndex = (currentTrackIndex + 1) % trackList.size
        }

        Log.d("MusicPlayerService", "Playing next: index=$currentTrackIndex")
        play(trackList[currentTrackIndex])
    }

    fun playPrevious() {
        if (trackList.isEmpty()) return
        mediaPlayer?.let {
            if (it.currentPosition > 3000) {
                it.seekTo(0)
                _currentPosition.value = 0
                return
            }
        }

        if (isShuffleEnabled) {
            currentShufflePosition = if (currentShufflePosition > 0) {
                currentShufflePosition - 1
            } else {
                shuffledIndices.size - 1
            }
            currentTrackIndex = shuffledIndices[currentShufflePosition]
        } else {
            currentTrackIndex = if (currentTrackIndex > 0) currentTrackIndex - 1 else trackList.size - 1
        }

        Log.d("MusicPlayerService", "Playing previous: index=$currentTrackIndex")
        play(trackList[currentTrackIndex])
    }

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
        _currentPosition.value = position
    }

    fun toggleShuffle(): Boolean {
        isShuffleEnabled = !isShuffleEnabled
        if (isShuffleEnabled) {
            shuffledIndices = trackList.indices.shuffled()
            currentShufflePosition = shuffledIndices.indexOf(currentTrackIndex)
        }
        Log.d("MusicPlayerService", "Shuffle: $isShuffleEnabled")
        return isShuffleEnabled
    }

    fun isShuffleEnabled(): Boolean = isShuffleEnabled

    fun toggleRepeat(): Int {
        repeatMode = (repeatMode + 1) % 3
        Log.d("MusicPlayerService", "Repeat mode: $repeatMode")
        return repeatMode
    }

    fun getRepeatMode(): Int = repeatMode

    fun getCurrentPosition(): Int = mediaPlayer?.currentPosition ?: 0

    fun getDuration(): Int = mediaPlayer?.duration ?: 0

    fun getTrackList(): List<UniversalTrack> = trackList

    fun getCurrentIndex(): Int = currentTrackIndex

    private fun onTrackCompleted() {
        when (repeatMode) {
            2 -> {
                _currentTrack.value?.let { play(it) }
            }
            1 -> {
                playNext()
            }
            0 -> {
                if (isShuffleEnabled) {
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

    private fun startProgressUpdate() {
        stopProgressUpdate()
        progressJob = scope.launch {
            while (_isPlaying.value) {
                _currentPosition.value = mediaPlayer?.currentPosition ?: 0
                delay(100)
            }
        }
    }

    private fun stopProgressUpdate() {
        progressJob?.cancel()
        progressJob = null
    }

    fun release() {
        stopProgressUpdate()
        mediaPlayer?.release()
        mediaPlayer = null
        _isPlaying.value = false
        _currentTrack.value = null
        _currentPosition.value = 0
        _duration.value = 0
    }
}