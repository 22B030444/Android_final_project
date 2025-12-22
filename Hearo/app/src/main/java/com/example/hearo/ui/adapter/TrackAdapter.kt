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

    private var likedTrackIds: Set<String> = emptySet()

    fun updateLikedTracks(likedIds: Set<String>) {
        likedTrackIds = likedIds
        notifyDataSetChanged()
    }

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
            binding.trackName.text = track.name

            val artistsText = track.artists.joinToString(", ") { it.name }
            binding.artistName.text = artistsText

            val imageUrl = track.album.images.firstOrNull()?.url
            Glide.with(binding.root.context)
                .load(imageUrl)
                .placeholder(R.color.surface_dark)
                .error(R.color.surface_dark)
                .into(binding.trackImage)

            val isLiked = likedTrackIds.contains(track.id)
            if (isLiked) {
                binding.favoriteButton.setImageResource(R.drawable.ic_favorite)
            } else {
                binding.favoriteButton.setImageResource(R.drawable.ic_favorite_border)
            }

            binding.root.setOnClickListener {
                onTrackClick(track)
            }

            binding.favoriteButton.setOnClickListener {
                onFavoriteClick(track)
            }
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