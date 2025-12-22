package com.example.hearo.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hearo.R
import com.example.hearo.data.model.spotify.AlbumFull
import com.example.hearo.databinding.ItemAlbumBinding

class AlbumAdapter(
    private val onAlbumClick: (AlbumFull) -> Unit
) : ListAdapter<AlbumFull, AlbumAdapter.AlbumViewHolder>(AlbumDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val binding = ItemAlbumBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AlbumViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AlbumViewHolder(
        private val binding: ItemAlbumBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(album: AlbumFull) {
            binding.albumName.text = album.name

            val artistsText = album.artists.joinToString(", ") { it.name }
            binding.artistName.text = artistsText

            val albumType = when (album.albumType) {
                "album" -> "Album"
                "single" -> "Single"
                "compilation" -> "Compilation"
                else -> "Album"
            }

            val year = album.releaseDate?.take(4) ?: ""
            val tracks = album.totalTracks?.let { "$it tracks" } ?: ""

            val infoText = listOfNotNull(
                albumType,
                year.takeIf { it.isNotEmpty() },
                tracks.takeIf { it.isNotEmpty() }
            ).joinToString(" • ")

            binding.albumInfo.text = infoText

            val imageUrl = album.images.firstOrNull()?.url
            Glide.with(binding.root.context)
                .load(imageUrl)
                .placeholder(R.color.surface_dark)
                .error(R.color.surface_dark)
                .into(binding.albumImage)

            binding.root.setOnClickListener {
                onAlbumClick(album)
            }
        }
    }

    private class AlbumDiffCallback : DiffUtil.ItemCallback<AlbumFull>() {
        override fun areItemsTheSame(oldItem: AlbumFull, newItem: AlbumFull): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AlbumFull, newItem: AlbumFull): Boolean {
            return oldItem == newItem
        }
    }
}