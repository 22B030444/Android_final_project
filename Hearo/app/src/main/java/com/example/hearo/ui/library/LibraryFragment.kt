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

    private val viewModel: LikedSongsViewModel by viewModels()

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
        observeLikedTracksCount()
    }

    private fun setupClickListeners() {
        // Переход к Liked Songs
        binding.likedSongsCard.setOnClickListener {
            findNavController().navigate(R.id.action_libraryFragment_to_likedSongsFragment)
        }

        // Остальные пункты (заглушки)
        binding.playlistsCard.setOnClickListener {
            // TODO: Playlists
        }

        binding.downloadsCard.setOnClickListener {
            // TODO: Downloads
        }

        binding.artistsCard.setOnClickListener {
            // TODO: Artists
        }
    }

    private fun observeLikedTracksCount() {
        viewModel.likedTracksCount.observe(viewLifecycleOwner) { count ->
            binding.likedSongsCountText.text = if (count == 1) {
                "1 song"
            } else {
                "$count songs"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


