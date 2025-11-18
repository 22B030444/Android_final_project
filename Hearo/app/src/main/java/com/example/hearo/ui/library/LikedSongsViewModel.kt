package com.example.hearo.ui.library

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.hearo.data.model.spotify.Track
import com.example.hearo.data.preferences.AppPreferences
import com.example.hearo.data.repository.AuthRepository
import com.example.hearo.data.repository.MusicRepository
import kotlinx.coroutines.launch

class LikedSongsViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(AppPreferences(application))
    private val musicRepository = MusicRepository(application, authRepository)

    // Автоматически обновляется когда меняется база данных
    val likedTracks: LiveData<List<Track>> = musicRepository.getLocalLikedTracks().asLiveData()

    private val _likedTracksCount = MutableLiveData<Int>()
    val likedTracksCount: LiveData<Int> = _likedTracksCount

    init {
        loadTracksCount()
    }

    /**
     * Удалить трек из избранного
     */
    fun removeTrack(track: Track) {
        viewModelScope.launch {
            musicRepository.removeTrackFromLocal(track.id)
            loadTracksCount()
        }
    }

    /**
     * Переключить избранное
     */
    fun toggleLike(track: Track) {
        viewModelScope.launch {
            musicRepository.toggleLocalLike(track)
            loadTracksCount()
        }
    }

    /**
     * Получить количество треков
     */
    private fun loadTracksCount() {
        viewModelScope.launch {
            val count = musicRepository.getLikedTracksCount()
            _likedTracksCount.value = count
        }
    }
}


