package com.example.hearo.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hearo.databinding.ItemSearchHistoryBinding

class SearchHistoryAdapter(
    private val onItemClick: (String) -> Unit,
    private val onRemoveClick: (String) -> Unit
) : ListAdapter<String, SearchHistoryAdapter.HistoryViewHolder>(HistoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemSearchHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HistoryViewHolder(
        private val binding: ItemSearchHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(query: String) {
            binding.historyQueryText.text = query

            binding.root.setOnClickListener {
                onItemClick(query)
            }

            binding.removeHistoryItem.setOnClickListener {
                onRemoveClick(query)
            }
        }
    }

    class HistoryDiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
}