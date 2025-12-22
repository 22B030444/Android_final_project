package com.example.hearo.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hearo.R
import com.example.hearo.data.repository.DownloadedTrack
import com.example.hearo.databinding.ItemDownloadedTrackBinding

class DownloadedTrackAdapter(
    private val onTrackClick: (DownloadedTrack) -> Unit,
    private val onDeleteClick: (DownloadedTrack) -> Unit,
    private val onLongClick: (DownloadedTrack) -> Unit
) : ListAdapter<DownloadedTrack, DownloadedTrackAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDownloadedTrackBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemDownloadedTrackBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(downloadedTrack: DownloadedTrack) {
            val track = downloadedTrack.track

            binding.trackName.text = track.name
            binding.artistName.text = track.artistName
            binding.trackInfo.text = "${formatDuration(track.durationMs)} • ${formatSize(downloadedTrack.fileSize)}"

            if (downloadedTrack.isFull) {
                binding.typeBadge.text = "Full"
                binding.typeBadge.setBackgroundResource(R.drawable.badge_full)
            } else {
                binding.typeBadge.text = "Preview"
                binding.typeBadge.setBackgroundResource(R.drawable.badge_preview)
            }

            Glide.with(binding.root.context)
                .load(track.imageUrl)
                .placeholder(R.color.surface_dark)
                .error(R.color.surface_dark)
                .into(binding.albumImage)

            binding.root.setOnClickListener {
                onTrackClick(downloadedTrack)
            }

            binding.root.setOnLongClickListener {
                onLongClick(downloadedTrack)
                true
            }

            binding.deleteButton.setOnClickListener {
                onDeleteClick(downloadedTrack)
            }
        }

        private fun formatDuration(durationMs: Int): String {
            val minutes = (durationMs / 1000) / 60
            val seconds = (durationMs / 1000) % 60
            return String.format("%d:%02d", minutes, seconds)
        }

        private fun formatSize(bytes: Long): String {
            return when {
                bytes < 1024 -> "$bytes B"
                bytes < 1024 * 1024 -> "${bytes / 1024} KB"
                else -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<DownloadedTrack>() {
        override fun areItemsTheSame(oldItem: DownloadedTrack, newItem: DownloadedTrack): Boolean {
            return oldItem.track.id == newItem.track.id
        }

        override fun areContentsTheSame(oldItem: DownloadedTrack, newItem: DownloadedTrack): Boolean {
            return oldItem == newItem
        }
    }
}


