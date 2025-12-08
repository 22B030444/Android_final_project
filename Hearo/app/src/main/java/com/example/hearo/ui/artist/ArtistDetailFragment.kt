package com.example.hearo.ui.artist

import android.os.Build
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
import com.example.hearo.data.model.UniversalArtist
import com.example.hearo.data.model.UiState
import com.example.hearo.databinding.FragmentArtistDetailBinding
import com.example.hearo.ui.adapter.UniversalTrackAdapter

class ArtistDetailFragment : Fragment() {

    private var _binding: FragmentArtistDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ArtistDetailViewModel by viewModels()

    private val tracksAdapter by lazy {
        UniversalTrackAdapter(
            onTrackClick = { track ->
                if (track.previewUrl.isNullOrEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        "No preview available for this track",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@UniversalTrackAdapter
                }

                val position = viewModel.getCurrentTracks().indexOf(track)
                val bundle = bundleOf(
                    "track" to track,
                    "trackList" to ArrayList(viewModel.getCurrentTracks()),
                    "currentIndex" to position
                )

                findNavController().navigate(
                    R.id.action_artistDetailFragment_to_playerFragment,
                    bundle
                )
            },
            onFavoriteClick = { track ->
                viewModel.toggleLikeTrack(track)
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArtistDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get artist from arguments
        val artist = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("artist", UniversalArtist::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable("artist")
        }

        val artistId = arguments?.getString("artistId") ?: artist?.id
        val artistName = arguments?.getString("artistName") ?: artist?.name

        if (artistId == null) {
            Toast.makeText(requireContext(), "Artist not found", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }

        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        observeStates()

        // Set initial artist info if available
        artist?.let { setupArtistInfo(it) }

        // Load artist details
        viewModel.loadArtistDetails(artistId, artistName)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        binding.songsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = tracksAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupClickListeners() {
        binding.likeButton.setOnClickListener {
            viewModel.toggleFollow()
        }

        binding.downloadButton.setOnClickListener {
            Toast.makeText(requireContext(), "Download coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.playButton.setOnClickListener {
            val tracks = viewModel.getCurrentTracks()
            if (tracks.isNotEmpty()) {
                val firstPlayable = tracks.firstOrNull { !it.previewUrl.isNullOrEmpty() }
                if (firstPlayable != null) {
                    val position = tracks.indexOf(firstPlayable)
                    val bundle = bundleOf(
                        "track" to firstPlayable,
                        "trackList" to ArrayList(tracks),
                        "currentIndex" to position
                    )
                    findNavController().navigate(
                        R.id.action_artistDetailFragment_to_playerFragment,
                        bundle
                    )
                } else {
                    Toast.makeText(requireContext(), "No playable tracks", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupArtistInfo(artist: UniversalArtist) {
        binding.collapsingToolbar.title = artist.name.uppercase()

        // Load artist image
        if (!artist.imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(artist.imageUrl)
                .placeholder(R.color.surface_dark)
                .error(R.color.surface_dark)
                .into(binding.artistHeaderImage)
        }

        // Monthly listeners
        val listenersText = when {
            artist.monthlyListeners != null -> "${artist.monthlyListeners} monthly listeners"
            artist.followersCount > 0 -> formatFollowers(artist.followersCount) + " followers"
            else -> ""
        }
        binding.monthlyListenersText.text = listenersText
    }

    private fun observeStates() {
        viewModel.artistState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    // Show loading in header area if needed
                }
                is UiState.Success -> {
                    setupArtistInfo(state.data)
                }
                is UiState.Error -> {
                    // Keep existing data if any
                }
                is UiState.Idle -> {}
            }
        }

        viewModel.tracksState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.songsRecyclerView.visibility = View.GONE
                    binding.emptyStateText.visibility = View.GONE
                }
                is UiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    if (state.data.isEmpty()) {
                        binding.songsRecyclerView.visibility = View.GONE
                        binding.emptyStateText.visibility = View.VISIBLE
                    } else {
                        binding.songsRecyclerView.visibility = View.VISIBLE
                        binding.emptyStateText.visibility = View.GONE
                        tracksAdapter.submitList(state.data)
                    }
                }
                is UiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.songsRecyclerView.visibility = View.GONE
                    binding.emptyStateText.visibility = View.VISIBLE
                    binding.emptyStateText.text = state.message
                }
                is UiState.Idle -> {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }

        viewModel.isFollowing.observe(viewLifecycleOwner) { isFollowing ->
            if (isFollowing) {
                binding.likeButton.setImageResource(R.drawable.ic_favorite)
            } else {
                binding.likeButton.setImageResource(R.drawable.ic_favorite_border)
            }
        }

        viewModel.likedTracksIds.observe(viewLifecycleOwner) { likedIds ->
            tracksAdapter.updateLikedTracks(likedIds)
        }
    }

    private fun formatFollowers(count: Int): String {
        return when {
            count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
            count >= 1_000 -> String.format("%.1fK", count / 1_000.0)
            else -> count.toString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
