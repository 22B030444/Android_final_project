package com.example.hearo.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hearo.R
import com.example.hearo.data.database.entity.PlaylistWithTracks
import com.example.hearo.databinding.ItemPlaylistSelectBinding

class PlaylistSelectAdapter(
    private val onPlaylistClick: (PlaylistWithTracks) -> Unit
) : ListAdapter<PlaylistWithTracks, PlaylistSelectAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPlaylistSelectBinding.inflate(
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
        private val binding: ItemPlaylistSelectBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(playlistWithTracks: PlaylistWithTracks) {
            val playlist = playlistWithTracks.playlist

            binding.playlistName.text = playlist.name
            binding.tracksCount.text = "${playlistWithTracks.tracks.size} songs"

            val imageUrl = playlist.imageUrl ?: playlistWithTracks.tracks.firstOrNull()?.imageUrl
            Glide.with(binding.root.context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_library)
                .error(R.drawable.ic_library)
                .into(binding.playlistImage)

            binding.root.setOnClickListener {
                onPlaylistClick(playlistWithTracks)
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<PlaylistWithTracks>() {
        override fun areItemsTheSame(oldItem: PlaylistWithTracks, newItem: PlaylistWithTracks): Boolean {
            return oldItem.playlist.id == newItem.playlist.id
        }

        override fun areContentsTheSame(oldItem: PlaylistWithTracks, newItem: PlaylistWithTracks): Boolean {
            return oldItem == newItem
        }
    }
}


