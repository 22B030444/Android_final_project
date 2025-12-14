package com.example.hearo.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hearo.R
import com.example.hearo.data.model.UniversalTrack
import com.example.hearo.databinding.FragmentHistoryBinding
import com.example.hearo.ui.adapter.UniversalTrackAdapter
import com.example.hearo.ui.playlist.AddToPlaylistDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HistoryViewModel by viewModels()

    private var historyTracks: List<UniversalTrack> = emptyList()

    private val tracksAdapter by lazy {
        UniversalTrackAdapter(
            onTrackClick = { track ->
                if (track.previewUrl.isNullOrEmpty()) {
                    Toast.makeText(requireContext(), "No preview available", Toast.LENGTH_SHORT).show()
                    return@UniversalTrackAdapter
                }

                val position = historyTracks.indexOf(track)
                val bundle = bundleOf(
                    "track" to track,
                    "trackList" to ArrayList(historyTracks),
                    "currentIndex" to position
                )
                findNavController().navigate(R.id.action_historyFragment_to_playerFragment, bundle)
            },
            onFavoriteClick = { track ->
                viewModel.toggleLike(track)
            },
            onLongClick = { track ->
                showTrackOptionsDialog(track)
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        observeData()
    }

    private fun setupToolbar() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.clearButton.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Clear History")
                .setMessage("Are you sure you want to clear all listening history?")
                .setPositiveButton("Clear") { _, _ ->
                    viewModel.clearHistory()
                    Toast.makeText(requireContext(), "History cleared", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = tracksAdapter
        }
    }

    private fun observeData() {
        viewModel.recentlyPlayed.observe(viewLifecycleOwner) { tracks ->
            historyTracks = tracks
            if (tracks.isEmpty()) {
                binding.emptyStateLayout.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            } else {
                binding.emptyStateLayout.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                tracksAdapter.submitList(tracks)
            }
        }

        viewModel.likedTracksIds.observe(viewLifecycleOwner) { likedIds ->
            tracksAdapter.updateLikedTracks(likedIds)
        }
    }

    private fun showTrackOptionsDialog(track: UniversalTrack) {
        val options = arrayOf("Add to playlist", "Add to Liked Songs")

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(track.name)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        val dialog = AddToPlaylistDialog.newInstance(track)
                        dialog.show(parentFragmentManager, "AddToPlaylistDialog")
                    }
                    1 -> {
                        viewModel.toggleLike(track)
                        Toast.makeText(requireContext(), "Added to Liked Songs", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}