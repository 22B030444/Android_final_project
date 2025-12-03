package com.example.hearo.utils

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TrackDownloadManager(private val context: Context) {

    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    private val _downloadProgress = MutableStateFlow<DownloadProgress>(DownloadProgress.Idle)
    val downloadProgress: StateFlow<DownloadProgress> = _downloadProgress

    private var currentDownloadId: Long = -1

    private val downloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) ?: return

            if (id == currentDownloadId) {
                val query = DownloadManager.Query().setFilterById(id)
                val cursor = downloadManager.query(query)

                if (cursor.moveToFirst()) {
                    val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    val status = cursor.getInt(statusIndex)

                    when (status) {
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            Log.d("TrackDownloadManager", "✅ Download completed!")
                            _downloadProgress.value = DownloadProgress.Success

                            val uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                            val localUri = cursor.getString(uriIndex)
                            Log.d("TrackDownloadManager", "File saved at: $localUri")

                            Toast.makeText(context, "Track downloaded successfully!", Toast.LENGTH_SHORT).show()
                        }

                        DownloadManager.STATUS_FAILED -> {
                            Log.e("TrackDownloadManager", "❌ Download failed!")
                            _downloadProgress.value = DownloadProgress.Failed("Download failed")
                            Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                cursor.close()
            }
        }
    }

    init {
        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(downloadReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(downloadReceiver, filter)
        }
    }

    fun downloadTrack(
        url: String,
        trackName: String,
        artistName: String,
        isFull: Boolean = false
    ) {
        if (url.isEmpty()) {
            _downloadProgress.value = DownloadProgress.Failed("No URL")
            Toast.makeText(context, "No download URL available", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val safeFileName = "${artistName} - ${trackName}"
                .replace(Regex("[^a-zA-Z0-9 ]"), "")
                .replace(" ", "_")
                .take(50)

            val fileName = if (isFull) {
                "$safeFileName-FULL.mp3"
            } else {
                "$safeFileName-preview.mp3"
            }

            Log.d("TrackDownloadManager", "Starting download...")
            Log.d("TrackDownloadManager", "URL: $url")
            Log.d("TrackDownloadManager", "Type: ${if (isFull) "FULL TRACK" else "PREVIEW"}")

            val request = DownloadManager.Request(Uri.parse(url)).apply {
                setTitle(if (isFull) "$artistName - $trackName (Full)" else "$artistName - $trackName (Preview)")
                setDescription(if (isFull) "Downloading full track..." else "Downloading preview...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

                setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_MUSIC,
                    "Hearo/$fileName"
                )

                setAllowedNetworkTypes(
                    DownloadManager.Request.NETWORK_WIFI or
                            DownloadManager.Request.NETWORK_MOBILE
                )

                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
            }

            currentDownloadId = downloadManager.enqueue(request)
            _downloadProgress.value = DownloadProgress.Downloading(0)

            val message = if (isFull) {
                "Downloading full track: $trackName"
            } else {
                "Downloading preview: $trackName"
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Log.e("TrackDownloadManager", "Error starting download", e)
            _downloadProgress.value = DownloadProgress.Failed(e.message ?: "Unknown error")
            Toast.makeText(context, "Failed to start download", Toast.LENGTH_SHORT).show()
        }
    }

    fun release() {
        try {
            context.unregisterReceiver(downloadReceiver)
        } catch (e: Exception) {
            Log.e("TrackDownloadManager", "Error unregistering receiver", e)
        }
    }
}

sealed class DownloadProgress {
    object Idle : DownloadProgress()
    data class Downloading(val progress: Int) : DownloadProgress()
    object Success : DownloadProgress()
    data class Failed(val message: String) : DownloadProgress()
}