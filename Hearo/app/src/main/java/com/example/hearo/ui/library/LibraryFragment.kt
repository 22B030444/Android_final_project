package com.example.hearo.ui.library

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
import com.example.hearo.databinding.FragmentLibraryBinding
import com.example.hearo.ui.adapter.RecentTrackAdapter

class LibraryFragment : Fragment() {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LibraryViewModel by viewModels()

    private var recentTracks: List<UniversalTrack> = emptyList()

    private val recentTrackAdapter by lazy {
        RecentTrackAdapter { track ->
            playTrack(track)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        observeData()
        observeCounts()
    }

    private fun setupRecyclerView() {
        binding.recentlyPlayedRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recentTrackAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupClickListeners() {
        binding.profileImage.setOnClickListener {
            findNavController().navigate(R.id.action_libraryFragment_to_profileFragment)
        }

        binding.playlistsCard.setOnClickListener {
            findNavController().navigate(R.id.action_libraryFragment_to_playlistsFragment)
        }

        binding.downloadsCard.setOnClickListener {
            findNavController().navigate(R.id.action_libraryFragment_to_downloadsFragment)
        }

        binding.likedSongsCard.setOnClickListener {
            findNavController().navigate(R.id.action_libraryFragment_to_likedSongsFragment)
        }

        binding.artistsCard.setOnClickListener {
            findNavController().navigate(R.id.action_libraryFragment_to_artistsFragment)
        }
    }

    private fun observeData() {
        viewModel.recentlyPlayed.observe(viewLifecycleOwner) { tracks ->
            recentTracks = tracks
            if (tracks.isEmpty()) {
                binding.recentlyPlayedTitle.visibility = View.GONE
                binding.recentlyPlayedRecyclerView.visibility = View.GONE
            } else {
                binding.recentlyPlayedTitle.visibility = View.VISIBLE
                binding.recentlyPlayedRecyclerView.visibility = View.VISIBLE
                recentTrackAdapter.submitList(tracks)
            }
        }
    }

    private fun playTrack(track: UniversalTrack) {
        if (track.previewUrl.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "No preview available", Toast.LENGTH_SHORT).show()
            return
        }

        val position = recentTracks.indexOf(track)
        val bundle = bundleOf(
            "track" to track,
            "trackList" to ArrayList(recentTracks),
            "currentIndex" to position
        )
        findNavController().navigate(R.id.action_libraryFragment_to_playerFragment, bundle)
    }
    private fun observeCounts() {
        viewModel.likedSongsCount.observe(viewLifecycleOwner) { count ->
            binding.likedSongsCountText.text = if (count == 1) "1 song" else "$count songs"
        }

        viewModel.downloadsCount.observe(viewLifecycleOwner) { count ->
            binding.downloadsCountText?.text = if (count == 1) "1 track" else "$count tracks"
        }

        viewModel.artistsCount.observe(viewLifecycleOwner) { count ->
            binding.artistsCountText?.text = if (count == 1) "1 artist" else "$count artists"
        }

        viewModel.playlistsCount.observe(viewLifecycleOwner) { count ->
            binding.playlistsCountText?.text = if (count == 1) "1 playlist" else "$count playlists"
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}