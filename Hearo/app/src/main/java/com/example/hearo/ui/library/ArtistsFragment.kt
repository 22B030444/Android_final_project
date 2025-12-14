package com.example.hearo.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.hearo.R
import com.example.hearo.databinding.FragmentArtistsBinding
import com.example.hearo.ui.adapter.ArtistGridAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ArtistsFragment : Fragment() {

    private var _binding: FragmentArtistsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ArtistsViewModel by viewModels()

    private val artistsAdapter by lazy {
        ArtistGridAdapter(
            onArtistClick = { artist ->
                // Навигация к экрану артиста
                val bundle = Bundle().apply {
                    putString("artistName", artist.name)
                    putString("artistId", artist.id)
                }
                findNavController().navigate(R.id.action_artistsFragment_to_artistDetailFragment, bundle)
            },
            onArtistLongClick = { artist ->
                showArtistOptionsDialog(artist)
            },
            onAddMoreClick = {
                // Переход к поиску артистов
                findNavController().navigate(R.id.action_artistsFragment_to_searchFragment)
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArtistsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupSearch()
        setupRecyclerView()
        observeData()
    }

    private fun setupToolbar() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupSearch() {
        binding.searchEditText.doAfterTextChanged { text ->
            viewModel.searchArtists(text?.toString() ?: "")
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = artistsAdapter
        }
    }

    private fun observeData() {
        viewModel.artists.observe(viewLifecycleOwner) { artists ->
            if (artists.isEmpty() && binding.searchEditText.text.isNullOrEmpty()) {
                binding.emptyStateLayout.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            } else {
                binding.emptyStateLayout.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                artistsAdapter.submitList(artists, showAddMore = binding.searchEditText.text.isNullOrEmpty())
            }
        }
    }

    private fun showArtistOptionsDialog(artist: com.example.hearo.data.model.UniversalArtist) {
        val options = arrayOf("View Artist", "Unfollow")

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(artist.name)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        val bundle = Bundle().apply {
                            putString("artistName", artist.name)
                            putString("artistId", artist.id)
                        }
                        findNavController().navigate(R.id.action_artistsFragment_to_artistDetailFragment, bundle)
                    }
                    1 -> {
                        viewModel.unfollowArtist(artist)
                        Toast.makeText(requireContext(), "Unfollowed ${artist.name}", Toast.LENGTH_SHORT).show()
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


