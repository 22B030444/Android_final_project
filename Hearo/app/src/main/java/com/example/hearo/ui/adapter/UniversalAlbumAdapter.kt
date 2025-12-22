package com.example.hearo.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hearo.R
import com.example.hearo.data.model.UniversalAlbum
import com.example.hearo.databinding.ItemUniversalAlbumBinding

class UniversalAlbumAdapter(
    private val onAlbumClick: (UniversalAlbum) -> Unit
) : ListAdapter<UniversalAlbum, UniversalAlbumAdapter.AlbumViewHolder>(AlbumDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val binding = ItemUniversalAlbumBinding.inflate(
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
        private val binding: ItemUniversalAlbumBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(album: UniversalAlbum) {
            binding.albumName.text = album.name
            binding.artistName.text = album.artistName

            val year = album.releaseDate?.take(4) ?: ""
            val tracks = if (album.totalTracks > 0) "${album.totalTracks} tracks" else ""

            val infoText = listOfNotNull(
                album.albumType?.replaceFirstChar { it.uppercase() },
                year.takeIf { it.isNotEmpty() },
                tracks.takeIf { it.isNotEmpty() }
            ).joinToString(" • ")

            binding.albumInfo.text = infoText

            Glide.with(binding.root.context)
                .load(album.imageUrl)
                .placeholder(R.color.surface_dark)
                .error(R.color.surface_dark)
                .into(binding.albumImage)

            binding.root.setOnClickListener {
                onAlbumClick(album)
            }
        }
    }

    private class AlbumDiffCallback : DiffUtil.ItemCallback<UniversalAlbum>() {
        override fun areItemsTheSame(oldItem: UniversalAlbum, newItem: UniversalAlbum): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: UniversalAlbum, newItem: UniversalAlbum): Boolean {
            return oldItem == newItem
        }
    }
}
