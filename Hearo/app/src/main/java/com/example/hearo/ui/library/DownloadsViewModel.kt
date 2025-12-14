package com.example.hearo.ui.library

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.hearo.data.repository.DownloadedTrack
import com.example.hearo.data.repository.DownloadsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class DownloadsViewModel(application: Application) : AndroidViewModel(application) {

    private val downloadsRepository = DownloadsRepository(application)

    private val searchQuery = MutableStateFlow("")

    val downloads: LiveData<List<DownloadedTrack>> = searchQuery.flatMapLatest { query ->
        if (query.isEmpty()) {
            downloadsRepository.getAllDownloads()
        } else {
            downloadsRepository.searchDownloads(query)
        }
    }.asLiveData()

    private val _storageInfo = MutableLiveData<String>()
    val storageInfo: LiveData<String> = _storageInfo

    init {
        loadStorageInfo()
    }

    fun searchDownloads(query: String) {
        searchQuery.value = query
    }

    fun deleteDownload(trackId: String) {
        viewModelScope.launch {
            downloadsRepository.deleteDownload(trackId)
            loadStorageInfo()
        }
    }

    fun clearAllDownloads() {
        viewModelScope.launch {
            downloadsRepository.clearAllDownloads()
            loadStorageInfo()
        }
    }

    private fun loadStorageInfo() {
        viewModelScope.launch {
            val count = downloadsRepository.getDownloadCount()
            val totalSize = downloadsRepository.getTotalDownloadsSize()
            _storageInfo.value = "$count tracks • ${formatSize(totalSize)}"
        }
    }

    private fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0))
        }
    }
}


