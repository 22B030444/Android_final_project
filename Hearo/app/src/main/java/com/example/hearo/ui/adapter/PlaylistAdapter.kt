package com.example.hearo.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hearo.R
import com.example.hearo.data.database.entity.PlaylistWithTracks
import com.example.hearo.databinding.ItemPlaylistBinding

class PlaylistAdapter(
    private val onPlaylistClick: (PlaylistWithTracks) -> Unit,
    private val onPlaylistLongClick: (PlaylistWithTracks) -> Unit
) : ListAdapter<PlaylistWithTracks, PlaylistAdapter.PlaylistViewHolder>(PlaylistDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val binding = ItemPlaylistBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlaylistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PlaylistViewHolder(
        private val binding: ItemPlaylistBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(playlistWithTracks: PlaylistWithTracks) {
            val playlist = playlistWithTracks.playlist
            val tracksCount = playlistWithTracks.tracks.size

            binding.playlistName.text = playlist.name

            binding.tracksCount.text = if (tracksCount == 1) {
                "1 song"
            } else {
                "$tracksCount songs"
            }

            // Обложка плейлиста
            val imageUrl = playlist.imageUrl ?: playlistWithTracks.tracks.firstOrNull()?.imageUrl
            Glide.with(binding.root.context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_library)
                .error(R.drawable.ic_library)
                .into(binding.playlistImage)

            binding.root.setOnClickListener {
                onPlaylistClick(playlistWithTracks)
            }

            binding.root.setOnLongClickListener {
                onPlaylistLongClick(playlistWithTracks)
                true
            }
        }
    }

    private class PlaylistDiffCallback : DiffUtil.ItemCallback<PlaylistWithTracks>() {
        override fun areItemsTheSame(oldItem: PlaylistWithTracks, newItem: PlaylistWithTracks): Boolean {
            return oldItem.playlist.id == newItem.playlist.id
        }

        override fun areContentsTheSame(oldItem: PlaylistWithTracks, newItem: PlaylistWithTracks): Boolean {
            return oldItem == newItem
        }
    }
}


