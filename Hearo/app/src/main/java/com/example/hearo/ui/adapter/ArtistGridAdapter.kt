package com.example.hearo.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hearo.R
import com.example.hearo.data.model.UniversalArtist
import com.example.hearo.databinding.ItemArtistGridBinding
import com.example.hearo.databinding.ItemAddMoreBinding

class ArtistGridAdapter(
    private val onArtistClick: (UniversalArtist) -> Unit,
    private val onArtistLongClick: (UniversalArtist) -> Unit,
    private val onAddMoreClick: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var artists: List<UniversalArtist> = emptyList()
    private var showAddMore: Boolean = true

    companion object {
        private const val TYPE_ARTIST = 0
        private const val TYPE_ADD_MORE = 1
    }

    fun submitList(list: List<UniversalArtist>, showAddMore: Boolean = true) {
        this.artists = list
        this.showAddMore = showAddMore
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return if (showAddMore) artists.size + 1 else artists.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (showAddMore && position == artists.size) TYPE_ADD_MORE else TYPE_ARTIST
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_ADD_MORE -> {
                val binding = ItemAddMoreBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                AddMoreViewHolder(binding)
            }
            else -> {
                val binding = ItemArtistGridBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ArtistViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ArtistViewHolder -> holder.bind(artists[position])
            is AddMoreViewHolder -> holder.bind()
        }
    }

    inner class ArtistViewHolder(
        private val binding: ItemArtistGridBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(artist: UniversalArtist) {
            binding.artistName.text = artist.name

            Glide.with(binding.root.context)
                .load(artist.imageUrl)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .circleCrop()
                .into(binding.artistImage)

            binding.root.setOnClickListener {
                onArtistClick(artist)
            }

            binding.root.setOnLongClickListener {
                onArtistLongClick(artist)
                true
            }
        }
    }

    inner class AddMoreViewHolder(
        private val binding: ItemAddMoreBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            binding.root.setOnClickListener {
                onAddMoreClick()
            }
        }
    }
}


