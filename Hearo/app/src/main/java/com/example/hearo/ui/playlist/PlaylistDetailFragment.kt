package com.example.hearo.ui.playlist

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
import com.bumptech.glide.Glide
import com.example.hearo.R
import com.example.hearo.data.model.MusicSource
import com.example.hearo.data.model.UniversalTrack
import com.example.hearo.databinding.FragmentPlaylistDetailBinding
import com.example.hearo.ui.adapter.UniversalTrackAdapter

class PlaylistDetailFragment : Fragment() {

    private var _binding: FragmentPlaylistDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlaylistDetailViewModel by viewModels()

    private var currentTracks: List<UniversalTrack> = emptyList()

    private val tracksAdapter by lazy {
        UniversalTrackAdapter(
            onTrackClick = { track ->
                if (track.previewUrl.isNullOrEmpty()) {
                    Toast.makeText(requireContext(), "No preview available", Toast.LENGTH_SHORT).show()
                    return@UniversalTrackAdapter
                }

                val position = currentTracks.indexOf(track)
                val bundle = bundleOf(
                    "track" to track,
                    "trackList" to ArrayList(currentTracks),
                    "currentIndex" to position
                )
                findNavController().navigate(
                    R.id.action_playlistDetailFragment_to_playerFragment,
                    bundle
                )
            },
            onFavoriteClick = { track ->
                viewModel.removeTrackFromPlaylist(track.id)
                Toast.makeText(requireContext(), "Removed from playlist", Toast.LENGTH_SHORT).show()
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val playlistId = arguments?.getLong("playlistId") ?: -1
        val playlistName = arguments?.getString("playlistName") ?: "Playlist"

        if (playlistId == -1L) {
            findNavController().navigateUp()
            return
        }

        binding.playlistNameText.text = playlistName

        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        observeTracks()

        viewModel.loadPlaylist(playlistId)
    }

    private fun setupToolbar() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = tracksAdapter
        }
    }

    private fun setupClickListeners() {
        binding.playButton.setOnClickListener {
            if (currentTracks.isNotEmpty()) {
                val firstPlayable = currentTracks.firstOrNull { !it.previewUrl.isNullOrEmpty() }
                if (firstPlayable != null) {
                    val position = currentTracks.indexOf(firstPlayable)
                    val bundle = bundleOf(
                        "track" to firstPlayable,
                        "trackList" to ArrayList(currentTracks),
                        "currentIndex" to position
                    )
                    findNavController().navigate(
                        R.id.action_playlistDetailFragment_to_playerFragment,
                        bundle
                    )
                }
            }
        }

        binding.shuffleButton.setOnClickListener {
            if (currentTracks.isNotEmpty()) {
                val shuffled = currentTracks.filter { !it.previewUrl.isNullOrEmpty() }.shuffled()
                if (shuffled.isNotEmpty()) {
                    val bundle = bundleOf(
                        "track" to shuffled.first(),
                        "trackList" to ArrayList(shuffled),
                        "currentIndex" to 0
                    )
                    findNavController().navigate(
                        R.id.action_playlistDetailFragment_to_playerFragment,
                        bundle
                    )
                }
            }
        }
    }

    private fun observeTracks() {
        viewModel.playlist.observe(viewLifecycleOwner) { playlist ->
            playlist?.let {
                binding.playlistNameText.text = it.playlist.name
                binding.tracksCountText.text = "${it.tracks.size} songs"

                val imageUrl = it.playlist.imageUrl ?: it.tracks.firstOrNull()?.imageUrl
                Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_library)
                    .error(R.drawable.ic_library)
                    .into(binding.playlistImage)
            }
        }

        viewModel.tracks.observe(viewLifecycleOwner) { tracks ->
            currentTracks = tracks.map { trackEntity ->
                UniversalTrack(
                    id = trackEntity.trackId,
                    name = trackEntity.trackName,
                    artistName = trackEntity.artistName,
                    albumName = trackEntity.albumName,
                    imageUrl = trackEntity.imageUrl,
                    previewUrl = trackEntity.previewUrl,
                    downloadUrl = null,
                    durationMs = trackEntity.durationMs,
                    source = MusicSource.valueOf(trackEntity.source),
                    canDownloadFull = false
                )
            }

            if (currentTracks.isEmpty()) {
                binding.emptyStateLayout.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            } else {
                binding.emptyStateLayout.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                tracksAdapter.submitList(currentTracks)
                tracksAdapter.updateLikedTracks(currentTracks.map { it.id }.toSet())
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


