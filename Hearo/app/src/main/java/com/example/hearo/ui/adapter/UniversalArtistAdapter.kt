package com.example.hearo.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hearo.R
import com.example.hearo.data.model.UniversalArtist
import com.example.hearo.databinding.ItemUniversalArtistBinding

class UniversalArtistAdapter(
    private val onArtistClick: (UniversalArtist) -> Unit
) : ListAdapter<UniversalArtist, UniversalArtistAdapter.ArtistViewHolder>(ArtistDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        val binding = ItemUniversalArtistBinding.inflate(
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
        private val binding: ItemUniversalArtistBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(artist: UniversalArtist) {
            binding.artistName.text = artist.name

            // Genres
            val genresText = if (artist.genres.isNotEmpty()) {
                artist.genres.take(3).joinToString(" • ")
            } else {
                "Artist"
            }
            binding.genresText.text = genresText

            // Artist image (circular)
            Glide.with(binding.root.context)
                .load(artist.imageUrl)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .circleCrop()
                .into(binding.artistImage)

            // Click listener
            binding.root.setOnClickListener {
                onArtistClick(artist)
            }
        }
    }

    private class ArtistDiffCallback : DiffUtil.ItemCallback<UniversalArtist>() {
        override fun areItemsTheSame(oldItem: UniversalArtist, newItem: UniversalArtist): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: UniversalArtist, newItem: UniversalArtist): Boolean {
            return oldItem == newItem
        }
    }
}
