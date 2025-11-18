package com.example.hearo.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hearo.databinding.FragmentLikedSongsBinding
import com.example.hearo.ui.adapter.TrackAdapter

class LikedSongsFragment : Fragment() {

    private var _binding: FragmentLikedSongsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LikedSongsViewModel by viewModels()

    private val adapter by lazy {
        TrackAdapter(
            onTrackClick = { track ->
                Toast.makeText(
                    requireContext(),
                    "Playing: ${track.name}",
                    Toast.LENGTH_SHORT
                ).show()
                // TODO: Откроем PlayerFragment позже
            },
            onFavoriteClick = { track ->
                // Удаляем из избранного
                viewModel.removeTrack(track)
                Toast.makeText(
                    requireContext(),
                    "Removed from Liked Songs",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLikedSongsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        observeLikedTracks()
        observeTracksCount()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@LikedSongsFragment.adapter
        }
    }

    private fun setupClickListeners() {
        // Кнопка назад
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        // Поиск внутри Liked Songs (опционально)
        binding.searchButton.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Search in Liked Songs - Coming soon",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun observeLikedTracks() {
        viewModel.likedTracks.observe(viewLifecycleOwner) { tracks ->
            if (tracks.isEmpty()) {
                // Показываем empty state
                binding.emptyStateLayout.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
                binding.tracksCountText.visibility = View.GONE
            } else {
                // Показываем список
                binding.emptyStateLayout.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                binding.tracksCountText.visibility = View.VISIBLE

                adapter.submitList(tracks)

                // Обновляем состояние лайков (все треки лайкнуты)
                val likedIds = tracks.map { it.id }.toSet()
                adapter.updateLikedTracks(likedIds)
            }
        }
    }

    private fun observeTracksCount() {
        viewModel.likedTracksCount.observe(viewLifecycleOwner) { count ->
            binding.tracksCountText.text = if (count == 1) {
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


