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
import com.example.hearo.data.model.UiState
import com.example.hearo.databinding.FragmentSearchBinding
import com.example.hearo.ui.adapter.UniversalTrackAdapter

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SearchViewModel by viewModels()

    // ⭐ Сохраняем текущий список результатов
    private var currentSearchResults: List<com.example.hearo.data.model.UniversalTrack> = emptyList()

    private val trackAdapter by lazy {
        UniversalTrackAdapter(
            onTrackClick = { track ->
                // Проверяем наличие preview
                if (track.previewUrl.isNullOrEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        "No preview available for this track",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@UniversalTrackAdapter
                }

                // ⭐ Находим позицию кликнутого трека
                val position = currentSearchResults.indexOf(track)

                // ⭐ Передаем весь список и позицию
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

    private fun setupSourceFilter() {
        binding.sourceChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
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

                        // ⭐ Сохраняем текущие результаты
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

        viewModel.likedTracksIds.observe(viewLifecycleOwner) { likedIds ->
            trackAdapter.updateLikedTracks(likedIds)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

