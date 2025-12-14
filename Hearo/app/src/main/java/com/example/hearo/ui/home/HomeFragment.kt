package com.example.hearo.ui.home

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
import com.example.hearo.databinding.FragmentHomeBinding
import com.example.hearo.ui.adapter.HorizontalTrackAdapter
import com.example.hearo.ui.adapter.UniversalTrackAdapter
import com.example.hearo.ui.playlist.AddToPlaylistDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    private var recentTracks: List<com.example.hearo.data.model.UniversalTrack> = emptyList()
    private var trendingTracks: List<com.example.hearo.data.model.UniversalTrack> = emptyList()

    private val recentlyPlayedAdapter by lazy {
        HorizontalTrackAdapter(
            onTrackClick = { track ->
                playTrack(track, recentTracks)
            },
            onTrackLongClick = { track ->
                showTrackOptionsDialog(track)
            }
        )
    }

    private val trendingAdapter by lazy {
        HorizontalTrackAdapter(
            onTrackClick = { track ->
                playTrack(track, trendingTracks)
            },
            onTrackLongClick = { track ->
                showTrackOptionsDialog(track)
            }
        )
    }

    private val recommendationsAdapter by lazy {
        UniversalTrackAdapter(
            onTrackClick = { track ->
                if (track.previewUrl.isNullOrEmpty()) {
                    Toast.makeText(requireContext(), "No preview available", Toast.LENGTH_SHORT).show()
                    return@UniversalTrackAdapter
                }
                val tracks = viewModel.recommendations.value ?: emptyList()
                playTrack(track, tracks)
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
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupGreeting()
        setupRecyclerViews()
        setupSwipeRefresh()
        setupClickListeners()
        observeData()
    }

    private fun setupGreeting() {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val greeting = when {
            hour < 12 -> "Good morning"
            hour < 18 -> "Good afternoon"
            else -> "Good evening"
        }
        binding.greetingText.text = greeting
    }

    private fun setupRecyclerViews() {
        binding.recentlyPlayedRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = recentlyPlayedAdapter
        }

        binding.trendingRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = trendingAdapter
        }

        binding.recommendationsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recommendationsAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setColorSchemeResources(R.color.purple_primary)
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refresh()
        }
    }

    private fun setupClickListeners() {
        binding.clearHistoryButton.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Clear History")
                .setMessage("Are you sure you want to clear your listening history?")
                .setPositiveButton("Clear") { _, _ ->
                    viewModel.clearHistory()
                    Toast.makeText(requireContext(), "History cleared", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.seeAllRecentButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_historyFragment)
        }
    }

    private fun observeData() {
        viewModel.recentlyPlayed.observe(viewLifecycleOwner) { tracks ->
            recentTracks = tracks
            if (tracks.isEmpty()) {
                binding.recentlyPlayedSection.visibility = View.GONE
            } else {
                binding.recentlyPlayedSection.visibility = View.VISIBLE
                recentlyPlayedAdapter.submitList(tracks)
            }
        }

        viewModel.trendingTracks.observe(viewLifecycleOwner) { tracks ->
            trendingTracks = tracks
            if (tracks.isEmpty()) {
                binding.trendingSection.visibility = View.GONE
            } else {
                binding.trendingSection.visibility = View.VISIBLE
                trendingAdapter.submitList(tracks)
            }
        }

        viewModel.recommendations.observe(viewLifecycleOwner) { tracks ->
            if (tracks.isEmpty()) {
                binding.recommendationsSection.visibility = View.GONE
            } else {
                binding.recommendationsSection.visibility = View.VISIBLE
                recommendationsAdapter.submitList(tracks)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefreshLayout.isRefreshing = isLoading
        }

        viewModel.likedTracksIds.observe(viewLifecycleOwner) { likedIds ->
            recommendationsAdapter.updateLikedTracks(likedIds)
        }
    }

    private fun playTrack(track: com.example.hearo.data.model.UniversalTrack, trackList: List<com.example.hearo.data.model.UniversalTrack>) {
        if (track.previewUrl.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "No preview available", Toast.LENGTH_SHORT).show()
            return
        }

        val position = trackList.indexOf(track)
        val bundle = bundleOf(
            "track" to track,
            "trackList" to ArrayList(trackList),
            "currentIndex" to position
        )
        findNavController().navigate(R.id.action_homeFragment_to_playerFragment, bundle)
    }

    private fun showTrackOptionsDialog(track: com.example.hearo.data.model.UniversalTrack) {
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