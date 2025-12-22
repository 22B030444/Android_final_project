package com.example.hearo.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hearo.R
import com.example.hearo.data.model.spotify.ArtistFull
import com.example.hearo.databinding.ItemArtistBinding

class ArtistAdapter(
    private val onArtistClick: (ArtistFull) -> Unit
) : ListAdapter<ArtistFull, ArtistAdapter.ArtistViewHolder>(ArtistDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        val binding = ItemArtistBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ArtistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ArtistViewHolder(
        private val binding: ItemArtistBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(artist: ArtistFull) {
            binding.artistName.text = artist.name

            val followersCount = artist.followers?.total ?: 0
            binding.followersText.text = formatFollowers(followersCount)

            val genres = artist.genres?.take(3)?.joinToString(" • ") ?: ""
            if (genres.isEmpty()) {
                binding.genresText.text = "Artist"
            } else {
                binding.genresText.text = genres
            }

            val imageUrl = artist.images?.firstOrNull()?.url
            Glide.with(binding.root.context)
                .load(imageUrl)
                .placeholder(R.color.surface_dark)
                .error(R.color.surface_dark)
                .circleCrop()
                .into(binding.artistImage)

            binding.root.setOnClickListener {
                onArtistClick(artist)
            }
        }

        private fun formatFollowers(count: Int): String {
            return when {
                count >= 1_000_000 -> {
                    val millions = count / 1_000_000.0
                    String.format("%.1fM followers", millions)
                }
                count >= 1_000 -> {
                    val thousands = count / 1_000.0
                    String.format("%.1fK followers", thousands)
                }
                else -> "$count followers"
            }
        }
    }

    private class ArtistDiffCallback : DiffUtil.ItemCallback<ArtistFull>() {
        override fun areItemsTheSame(oldItem: ArtistFull, newItem: ArtistFull): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ArtistFull, newItem: ArtistFull): Boolean {
            return oldItem == newItem
        }
    }
}