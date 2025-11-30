package com.example.hearo.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hearo.data.model.UiState
import com.example.hearo.databinding.FragmentSearchBinding
import com.example.hearo.ui.adapter.AlbumAdapter
import com.example.hearo.ui.adapter.TrackAdapter
import com.google.android.material.tabs.TabLayout

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SearchViewModel by viewModels()

    private val trackAdapter by lazy {
        TrackAdapter(
            onTrackClick = { track ->
                Toast.makeText(requireContext(), "Playing: ${track.name}", Toast.LENGTH_SHORT).show()
            },
            onFavoriteClick = { track ->
                viewModel.toggleLocalLike(track)
            }
        )
    }

    private val albumAdapter by lazy {
        AlbumAdapter(
            onAlbumClick = { album ->
                Toast.makeText(requireContext(), "Album: ${album.name}", Toast.LENGTH_SHORT).show()
                // TODO: Открыть экран с треками альбома
            }
        )
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
        observeStates()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        // По умолчанию показываем треки
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
                        // Tracks
                        viewModel.setSearchType(SearchType.TRACKS)
                        binding.recyclerView.adapter = trackAdapter
                        val currentQuery = binding.searchEditText.text.toString()
                        if (currentQuery.isNotEmpty()) {
                            viewModel.search(currentQuery)
                        }
                    }
                    1 -> {
                        // Albums
                        viewModel.setSearchType(SearchType.ALBUMS)
                        binding.recyclerView.adapter = albumAdapter
                        val currentQuery = binding.searchEditText.text.toString()
                        if (currentQuery.isNotEmpty()) {
                            viewModel.search(currentQuery)
                        }
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun observeStates() {
        // Наблюдаем за состоянием треков
        viewModel.searchTracksState.observe(viewLifecycleOwner) { state ->
            if (viewModel.currentSearchType.value == SearchType.TRACKS) {
                handleTracksState(state)
            }
        }

        // Наблюдаем за состоянием альбомов
        viewModel.searchAlbumsState.observe(viewLifecycleOwner) { state ->
            if (viewModel.currentSearchType.value == SearchType.ALBUMS) {
                handleAlbumsState(state)
            }
        }

        // Наблюдаем за лайкнутыми треками
        viewModel.likedTracksIds.observe(viewLifecycleOwner) { likedIds ->
            trackAdapter.updateLikedTracks(likedIds)
        }
    }

    private fun handleTracksState(state: UiState<List<com.example.hearo.data.model.spotify.Track>>) {
        when (state) {
            is UiState.Idle -> {
                binding.progressBar.visibility = View.GONE
                binding.recyclerView.visibility = View.GONE
                binding.emptyStateText.visibility = View.VISIBLE
                binding.emptyStateText.text = "Search for songs"
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
                } else {
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.emptyStateText.visibility = View.GONE
                    trackAdapter.submitList(state.data)
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

    private fun handleAlbumsState(state: UiState<List<com.example.hearo.data.model.spotify.AlbumFull>>) {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}