package com.example.hearo.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hearo.R
import com.example.hearo.data.model.UniversalTrack
import com.example.hearo.databinding.ItemRecentTrackBinding

class RecentTrackAdapter(
    private val onTrackClick: (UniversalTrack) -> Unit
) : ListAdapter<UniversalTrack, RecentTrackAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecentTrackBinding.inflate(
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
        private val binding: ItemRecentTrackBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(track: UniversalTrack) {
            binding.trackName.text = track.name
            binding.artistName.text = track.artistName

            Glide.with(binding.root.context)
                .load(track.imageUrl)
                .placeholder(R.color.surface_dark)
                .error(R.color.surface_dark)
                .into(binding.trackImage)

            binding.root.setOnClickListener {
                onTrackClick(track)
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<UniversalTrack>() {
        override fun areItemsTheSame(oldItem: UniversalTrack, newItem: UniversalTrack): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: UniversalTrack, newItem: UniversalTrack): Boolean {
            return oldItem == newItem
        }
    }
}