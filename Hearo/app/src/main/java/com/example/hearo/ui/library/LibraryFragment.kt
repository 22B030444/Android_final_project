package com.example.hearo.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.hearo.R
import com.example.hearo.databinding.FragmentLibraryBinding

class LibraryFragment : Fragment() {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LibraryViewModel by viewModels()

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

        setupClickListeners()
        observeCounts()
    }

    private fun setupClickListeners() {
        binding.likedSongsCard.setOnClickListener {
            findNavController().navigate(R.id.action_libraryFragment_to_likedSongsFragment)
        }

        binding.playlistsCard.setOnClickListener {
            findNavController().navigate(R.id.action_libraryFragment_to_playlistsFragment)
        }

        binding.artistsCard.setOnClickListener {
            findNavController().navigate(R.id.action_libraryFragment_to_artistsFragment)
        }

        binding.downloadsCard.setOnClickListener {
            findNavController().navigate(R.id.action_libraryFragment_to_downloadsFragment)
        }

        binding.profileImage.setOnClickListener {
            findNavController().navigate(R.id.action_libraryFragment_to_profileFragment)
        }

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