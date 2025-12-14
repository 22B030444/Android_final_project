package com.example.hearo.utils

import android.content.Context
import android.os.Environment
import android.util.Log
import com.example.hearo.data.model.UniversalTrack
import com.example.hearo.data.repository.DownloadsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

data class DownloadProgress(
    val isDownloading: Boolean = false,
    val progress: Int = 0,
    val fileName: String = "",
    val isComplete: Boolean = false,
    val error: String? = null
)

class TrackDownloadManager(private val context: Context) {

    private val client = OkHttpClient()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val downloadsRepository = DownloadsRepository(context)

    private val _downloadProgress = MutableStateFlow(DownloadProgress())
    val downloadProgress: StateFlow<DownloadProgress> = _downloadProgress

    private var currentTrack: UniversalTrack? = null
    private var currentIsFull: Boolean = false

    fun downloadTrack(
        track: UniversalTrack,
        isFull: Boolean
    ) {
        val url = if (isFull && !track.downloadUrl.isNullOrEmpty()) {
            track.downloadUrl
        } else {
            track.previewUrl
        }

        if (url.isNullOrEmpty()) {
            _downloadProgress.value = DownloadProgress(error = "No download URL available")
            return
        }

        currentTrack = track
        currentIsFull = isFull

        val fileName = sanitizeFileName("${track.artistName} - ${track.name}")
        val extension = if (url.contains(".mp3")) ".mp3" else ".m4a"

        scope.launch {
            try {
                _downloadProgress.value = DownloadProgress(
                    isDownloading = true,
                    progress = 0,
                    fileName = fileName
                )

                val file = downloadFile(url, fileName + extension)

                if (file != null && file.exists()) {
                    // Сохраняем в базу данных
                    downloadsRepository.saveDownloadedTrack(
                        track = track,
                        localFilePath = file.absolutePath,
                        fileSize = file.length(),
                        isFull = isFull
                    )

                    _downloadProgress.value = DownloadProgress(
                        isDownloading = false,
                        progress = 100,
                        fileName = fileName,
                        isComplete = true
                    )
                    Log.d("TrackDownloadManager", "Download complete: ${file.absolutePath}")
                } else {
                    _downloadProgress.value = DownloadProgress(
                        isDownloading = false,
                        error = "Failed to save file"
                    )
                }
            } catch (e: Exception) {
                Log.e("TrackDownloadManager", "Download failed", e)
                _downloadProgress.value = DownloadProgress(
                    isDownloading = false,
                    error = e.message ?: "Download failed"
                )
            }
        }
    }

    // Для обратной совместимости
    fun downloadTrack(
        url: String,
        trackName: String,
        artistName: String,
        isFull: Boolean
    ) {
        val fileName = sanitizeFileName("$artistName - $trackName")
        val extension = if (url.contains(".mp3")) ".mp3" else ".m4a"

        scope.launch {
            try {
                _downloadProgress.value = DownloadProgress(
                    isDownloading = true,
                    progress = 0,
                    fileName = fileName
                )

                val file = downloadFile(url, fileName + extension)

                if (file != null && file.exists()) {
                    _downloadProgress.value = DownloadProgress(
                        isDownloading = false,
                        progress = 100,
                        fileName = fileName,
                        isComplete = true
                    )
                    Log.d("TrackDownloadManager", "Download complete: ${file.absolutePath}")
                } else {
                    _downloadProgress.value = DownloadProgress(
                        isDownloading = false,
                        error = "Failed to save file"
                    )
                }
            } catch (e: Exception) {
                Log.e("TrackDownloadManager", "Download failed", e)
                _downloadProgress.value = DownloadProgress(
                    isDownloading = false,
                    error = e.message ?: "Download failed"
                )
            }
        }
    }

    private suspend fun downloadFile(url: String, fileName: String): File? {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url(url)
                    .build()

                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    throw IOException("Failed to download: ${response.code}")
                }

                val body = response.body ?: throw IOException("Empty response body")
                val contentLength = body.contentLength()

                // Создаем директорию для загрузок
                val downloadDir = File(
                    context.getExternalFilesDir(Environment.DIRECTORY_MUSIC),
                    "Hearo"
                )
                if (!downloadDir.exists()) {
                    downloadDir.mkdirs()
                }

                val file = File(downloadDir, fileName)

                FileOutputStream(file).use { output ->
                    body.byteStream().use { input ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        var totalBytesRead: Long = 0

                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            totalBytesRead += bytesRead

                            if (contentLength > 0) {
                                val progress = ((totalBytesRead * 100) / contentLength).toInt()
                                _downloadProgress.value = _downloadProgress.value.copy(
                                    progress = progress
                                )
                            }
                        }
                    }
                }

                file
            } catch (e: Exception) {
                Log.e("TrackDownloadManager", "Error downloading file", e)
                null
            }
        }
    }

    private fun sanitizeFileName(name: String): String {
        return name.replace(Regex("[\\\\/:*?\"<>|]"), "_")
            .take(100)
    }

    fun resetProgress() {
        _downloadProgress.value = DownloadProgress()
    }

    fun release() {
        scope.cancel()
    }
}