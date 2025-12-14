package com.example.hearo.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hearo.R
import com.example.hearo.data.model.UniversalAlbum
import com.example.hearo.data.model.UniversalArtist
import com.example.hearo.data.model.UniversalTrack
import com.example.hearo.data.model.UiState
import com.example.hearo.databinding.FragmentSearchBinding
import com.example.hearo.ui.adapter.UniversalAlbumAdapter
import com.example.hearo.ui.adapter.UniversalArtistAdapter
import com.example.hearo.ui.adapter.UniversalTrackAdapter
import com.google.android.material.tabs.TabLayout

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SearchViewModel by viewModels()

    private var currentSearchResults: List<UniversalTrack> = emptyList()

    private val trackAdapter by lazy {
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

                val position = currentSearchResults.indexOf(track)
                val bundle = bundleOf(
                    "track" to track,
                    "trackList" to ArrayList(currentSearchResults),
                    "currentIndex" to position
                )

                findNavController().navigate(
                    R.id.action_searchFragment_to_playerFragment,
                    bundle
                )
            },
            onFavoriteClick = { track ->
                viewModel.toggleLocalLike(track)
            },
            onLongClick = { track ->
                showTrackOptionsDialog(track)
            }
        )
    }

    private fun showTrackOptionsDialog(track: UniversalTrack) {
        val options = arrayOf("Add to playlist", "Add to Liked Songs")

        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle(track.name)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        val dialog = com.example.hearo.ui.playlist.AddToPlaylistDialog.newInstance(track)
                        dialog.show(parentFragmentManager, "AddToPlaylistDialog")
                    }
                    1 -> {
                        viewModel.toggleLocalLike(track)
                        Toast.makeText(requireContext(), "Added to Liked Songs", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .show()
    }




    private val albumAdapter by lazy {
        UniversalAlbumAdapter { album ->
            val bundle = bundleOf(
                "albumId" to album.id,
                "albumName" to album.name,
                "artistName" to album.artistName,
                "artistId" to album.artistId,
                "isAlbum" to true
            )
            findNavController().navigate(
                R.id.action_searchFragment_to_artistDetailFragment,
                bundle
            )
        }
    }

    private val artistAdapter by lazy {
        UniversalArtistAdapter { artist ->
            val bundle = bundleOf(
                "artist" to artist,
                "artistId" to artist.id,
                "artistName" to artist.name
            )
            findNavController().navigate(
                R.id.action_searchFragment_to_artistDetailFragment,
                bundle
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        setupTabs()
        setupSourceFilter()
        observeStates()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = trackAdapter
    }

    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener { text ->
            val query = text.toString()
            viewModel.search(query)
        }
    }

    private fun setupTabs() {
        binding.searchTypeTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        viewModel.setSearchType(SearchType.TRACKS)
                        binding.recyclerView.adapter = trackAdapter
                        binding.sourceChipGroup.visibility = View.VISIBLE
                    }
                    1 -> {
                        viewModel.setSearchType(SearchType.ALBUMS)
                        binding.recyclerView.adapter = albumAdapter
                        binding.sourceChipGroup.visibility = View.GONE
                    }
                    2 -> {
                        viewModel.setSearchType(SearchType.ARTISTS)
                        binding.recyclerView.adapter = artistAdapter
                        binding.sourceChipGroup.visibility = View.GONE
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupSourceFilter() {
        binding.sourceChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            when (checkedIds.firstOrNull()) {
                R.id.chipAll -> viewModel.setFilter(SearchFilter.ALL)
                R.id.chipSpotify -> viewModel.setFilter(SearchFilter.ITUNES)
                R.id.chipJamendo -> viewModel.setFilter(SearchFilter.JAMENDO)
            }

            val query = binding.searchEditText.text.toString()
            if (query.isNotEmpty()) {
                viewModel.search(query)
            }
        }
    }

    private fun observeStates() {
        viewModel.searchState.observe(viewLifecycleOwner) { state ->
            if (viewModel.currentSearchType.value == SearchType.TRACKS) {
                handleTracksState(state)
            }
        }

        viewModel.albumsState.observe(viewLifecycleOwner) { state ->
            if (viewModel.currentSearchType.value == SearchType.ALBUMS) {
                handleAlbumsState(state)
            }
        }

        viewModel.artistsState.observe(viewLifecycleOwner) { state ->
            if (viewModel.currentSearchType.value == SearchType.ARTISTS) {
                handleArtistsState(state)
            }
        }

        viewModel.likedTracksIds.observe(viewLifecycleOwner) { likedIds ->
            trackAdapter.updateLikedTracks(likedIds)
        }

        viewModel.currentSearchType.observe(viewLifecycleOwner) { type ->
            when (type) {
                SearchType.TRACKS -> viewModel.searchState.value?.let { handleTracksState(it) }
                SearchType.ALBUMS -> viewModel.albumsState.value?.let { handleAlbumsState(it) }
                SearchType.ARTISTS -> viewModel.artistsState.value?.let { handleArtistsState(it) }
                null -> {}
            }
        }
    }

    private fun handleTracksState(state: UiState<List<UniversalTrack>>) {
        when (state) {
            is UiState.Idle -> {
                binding.progressBar.visibility = View.GONE
                binding.recyclerView.visibility = View.GONE
                binding.emptyStateText.visibility = View.VISIBLE
                binding.emptyStateText.text = "Search for songs"
                currentSearchResults = emptyList()
            }
            is UiState.Loading -> {
                binding.progressBar.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
                binding.emptyStateText.visibility = View.GONE
            }
            is UiState.Success -> {
                binding.progressBar.visibility = View.GONE
                if (state.data.isEmpty()) {
                    binding.recyclerView.visibility = View.GONE
                    binding.emptyStateText.visibility = View.VISIBLE
                    binding.emptyStateText.text = "No tracks found"
                    currentSearchResults = emptyList()
                } else {
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.emptyStateText.visibility = View.GONE
                    currentSearchResults = state.data
                    trackAdapter.submitList(state.data)
                }
            }
            is UiState.Error -> {
                binding.progressBar.visibility = View.GONE
                binding.recyclerView.visibility = View.GONE
                binding.emptyStateText.visibility = View.VISIBLE
                binding.emptyStateText.text = "Error: ${state.message}"
                currentSearchResults = emptyList()
            }
        }
    }

    private fun handleAlbumsState(state: UiState<List<UniversalAlbum>>) {
        when (state) {
            is UiState.Idle -> {
                binding.progressBar.visibility = View.GONE
                binding.recyclerView.visibility = View.GONE
                binding.emptyStateText.visibility = View.VISIBLE
                binding.emptyStateText.text = "Search for albums"
            }
            is UiState.Loading -> {
                binding.progressBar.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
                binding.emptyStateText.visibility = View.GONE
            }
            is UiState.Success -> {
                binding.progressBar.visibility = View.GONE
                if (state.data.isEmpty()) {
                    binding.recyclerView.visibility = View.GONE
                    binding.emptyStateText.visibility = View.VISIBLE
                    binding.emptyStateText.text = "No albums found"
                } else {
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.emptyStateText.visibility = View.GONE
                    albumAdapter.submitList(state.data)
                }
            }
            is UiState.Error -> {
                binding.progressBar.visibility = View.GONE
                binding.recyclerView.visibility = View.GONE
                binding.emptyStateText.visibility = View.VISIBLE
                binding.emptyStateText.text = "Error: ${state.message}"
            }
        }
    }

    private fun handleArtistsState(state: UiState<List<UniversalArtist>>) {
        when (state) {
            is UiState.Idle -> {
                binding.progressBar.visibility = View.GONE
                binding.recyclerView.visibility = View.GONE
                binding.emptyStateText.visibility = View.VISIBLE
                binding.emptyStateText.text = "Search for artists"
            }
            is UiState.Loading -> {
                binding.progressBar.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
                binding.emptyStateText.visibility = View.GONE
            }
            is UiState.Success -> {
                binding.progressBar.visibility = View.GONE
                if (state.data.isEmpty()) {
                    binding.recyclerView.visibility = View.GONE
                    binding.emptyStateText.visibility = View.VISIBLE
                    binding.emptyStateText.text = "No artists found"
                } else {
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.emptyStateText.visibility = View.GONE
                    artistAdapter.submitList(state.data)
                }
            }
            is UiState.Error -> {
                binding.progressBar.visibility = View.GONE
                binding.recyclerView.visibility = View.GONE
                binding.emptyStateText.visibility = View.VISIBLE
                binding.emptyStateText.text = "Error: ${state.message}"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
