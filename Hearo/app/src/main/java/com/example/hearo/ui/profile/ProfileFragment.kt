package com.example.hearo.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.hearo.R
import com.example.hearo.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        observeData()
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.songsCard.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_likedSongsFragment)
        }

        binding.playlistsCard.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_playlistsFragment)
        }

        binding.artistsCard.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_artistsFragment)
        }
    }

    private fun observeData() {
        viewModel.likedSongsCount.observe(viewLifecycleOwner) { count ->
            binding.songsCount.text = count.toString()
        }

        viewModel.playlistsCount.observe(viewLifecycleOwner) { count ->
            binding.playlistsCount.text = count.toString()
        }

        viewModel.artistsCount.observe(viewLifecycleOwner) { count ->
            binding.artistsCount.text = count.toString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


