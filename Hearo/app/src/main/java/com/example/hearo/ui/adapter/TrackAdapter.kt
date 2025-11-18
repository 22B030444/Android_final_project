package com.example.hearo.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hearo.R
import com.example.hearo.data.model.spotify.Track
import com.example.hearo.databinding.ItemTrackBinding

class TrackAdapter(
    private val onTrackClick: (Track) -> Unit,
    private val onFavoriteClick: (Track) -> Unit
) : ListAdapter<Track, TrackAdapter.TrackViewHolder>(TrackDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val binding = ItemTrackBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TrackViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TrackViewHolder(
        private val binding: ItemTrackBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(track: Track) {
            // Название трека
            binding.trackName.text = track.name

            // Исполнители (может быть несколько)
            val artistsText = track.artists.joinToString(", ") { it.name }
            binding.artistName.text = artistsText

            // Обложка альбома
            val imageUrl = track.album.images.firstOrNull()?.url
            Glide.with(binding.root.context)
                .load(imageUrl)
                .placeholder(R.color.surface_dark)
                .error(R.color.surface_dark)
                .into(binding.trackImage)

            // Клик по треку
            binding.root.setOnClickListener {
                onTrackClick(track)
            }

            // Клик по кнопке избранного
            binding.favoriteButton.setOnClickListener {
                onFavoriteClick(track)
            }

            // TODO: Позже добавим проверку, в избранном ли трек
            // и будем менять иконку ic_favorite_border <-> ic_favorite
        }
    }

    private class TrackDiffCallback : DiffUtil.ItemCallback<Track>() {
        override fun areItemsTheSame(oldItem: Track, newItem: Track): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Track, newItem: Track): Boolean {
            return oldItem == newItem
        }
    }
}


