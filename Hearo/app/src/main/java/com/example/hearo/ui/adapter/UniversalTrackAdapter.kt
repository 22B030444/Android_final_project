package com.example.hearo.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hearo.R
import com.example.hearo.data.model.MusicSource
import com.example.hearo.data.model.UniversalTrack
import com.example.hearo.databinding.ItemUniversalTrackBinding

class UniversalTrackAdapter(
    private val onTrackClick: (UniversalTrack) -> Unit,
    private val onFavoriteClick: (UniversalTrack) -> Unit
) : ListAdapter<UniversalTrack, UniversalTrackAdapter.TrackViewHolder>(TrackDiffCallback()) {

    private var likedTrackIds: Set<String> = emptySet()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val binding = ItemUniversalTrackBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TrackViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun updateLikedTracks(likedIds: Set<String>) {
        likedTrackIds = likedIds
        notifyDataSetChanged()
    }

    inner class TrackViewHolder(
        private val binding: ItemUniversalTrackBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(track: UniversalTrack) {
            // Название трека
            binding.trackName.text = track.name

            // Исполнитель
            binding.artistName.text = track.artistName

            // Альбом
            binding.albumName.text = track.albumName

            // Длительность
            binding.duration.text = formatDuration(track.durationMs)

            // Источник (бейдж)
            when (track.source) {
                MusicSource.ITUNES -> {
                    binding.sourceBadge.text = "iTunes"
                    binding.sourceBadge.setBackgroundColor(
                        ContextCompat.getColor(binding.root.context, R.color.itunes_badge)
                    )
                }
                MusicSource.JAMENDO -> {
                    binding.sourceBadge.text = "Free"
                    binding.sourceBadge.setBackgroundColor(
                        ContextCompat.getColor(binding.root.context, R.color.jamendo_badge)
                    )
                }
            }

            // Показываем можно ли скачать полный трек
            if (track.canDownloadFull) {
                binding.downloadIcon.visibility = android.view.View.VISIBLE
            } else {
                binding.downloadIcon.visibility = android.view.View.GONE
            }

            // Обложка
            Glide.with(binding.root.context)
                .load(track.imageUrl)
                .placeholder(R.color.surface_dark)
                .error(R.color.surface_dark)
                .into(binding.albumImage)

            // Избранное
            val isLiked = likedTrackIds.contains(track.id)
            if (isLiked) {
                binding.favoriteButton.setImageResource(R.drawable.ic_favorite)
            } else {
                binding.favoriteButton.setImageResource(R.drawable.ic_favorite_border)
            }

            // Проверяем наличие preview
            if (track.previewUrl.isNullOrEmpty()) {
                binding.trackName.alpha = 0.5f
                binding.artistName.alpha = 0.5f
            } else {
                binding.trackName.alpha = 1.0f
                binding.artistName.alpha = 1.0f
            }

            // Клики
            binding.root.setOnClickListener {
                onTrackClick(track)
            }

            binding.favoriteButton.setOnClickListener {
                onFavoriteClick(track)
            }
        }

        private fun formatDuration(durationMs: Int): String {
            val minutes = (durationMs / 1000) / 60
            val seconds = (durationMs / 1000) % 60
            return String.format("%d:%02d", minutes, seconds)
        }
    }

    private class TrackDiffCallback : DiffUtil.ItemCallback<UniversalTrack>() {
        override fun areItemsTheSame(oldItem: UniversalTrack, newItem: UniversalTrack): Boolean {
            return oldItem.id == newItem.id && oldItem.source == newItem.source
        }

        override fun areContentsTheSame(oldItem: UniversalTrack, newItem: UniversalTrack): Boolean {
            return oldItem == newItem
        }
    }
}