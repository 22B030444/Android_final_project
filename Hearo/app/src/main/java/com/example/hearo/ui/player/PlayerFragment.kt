package com.example.hearo.ui.player

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.hearo.R
import com.example.hearo.data.model.MusicSource
import com.example.hearo.data.model.UniversalTrack
import com.example.hearo.databinding.FragmentPlayerBinding
import kotlinx.coroutines.launch

class PlayerFragment : Fragment() {

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlayerViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val track = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("track", UniversalTrack::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable("track")
        }

        val trackList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelableArrayList("trackList", UniversalTrack::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelableArrayList("trackList")
        }

        val currentIndex = arguments?.getInt("currentIndex", 0) ?: 0

        if (track == null) {
            Toast.makeText(requireContext(), "Track not found", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }

        if (!trackList.isNullOrEmpty()) {
            viewModel.setTrackList(trackList, currentIndex)
        }

        viewModel.setTrack(track)

        setupClickListeners()
        observeStates()
    }

    private fun setupUI(track: UniversalTrack) {
        binding.trackNameText.text = track.name
        binding.artistNameText.text = track.artistName

        Glide.with(this)
            .load(track.imageUrl)
            .placeholder(R.color.surface_dark)
            .error(R.color.surface_dark)
            .into(binding.albumCoverImage)

        if (track.canDownloadFull && track.source == MusicSource.JAMENDO) {
            binding.downloadButton.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.jamendo_badge)
            )
        } else {
            binding.downloadButton.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.white)
            )
        }
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.playPauseButton.setOnClickListener {
            viewModel.togglePlayPause()
        }

        binding.previousButton.setOnClickListener {
            viewModel.playPrevious()
        }

        binding.nextButton.setOnClickListener {
            viewModel.playNext()
        }

        binding.shuffleButton.setOnClickListener {
            viewModel.toggleShuffle()
        }

        binding.repeatButton.setOnClickListener {
            viewModel.toggleRepeat()
        }

        binding.favoriteButton.setOnClickListener {
            viewModel.toggleLike()
        }

        binding.downloadButton.setOnClickListener {
            viewModel.downloadTrack()
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    binding.currentTimeText.text = formatTime(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    viewModel.seekTo(it.progress)
                }
            }
        })
    }

    private fun observeStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isPlaying.collect { isPlaying ->
                binding.playPauseButton.setImageResource(
                    if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                )
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isShuffleEnabled.collect { isEnabled ->
                binding.shuffleButton.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        if (isEnabled) R.color.purple_primary else R.color.white
                    )
                )
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.repeatMode.collect { mode ->
                val color = if (mode != RepeatMode.OFF) R.color.purple_primary else R.color.white
                binding.repeatButton.setColorFilter(ContextCompat.getColor(requireContext(), color))
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLiked.collect { isLiked ->
                binding.favoriteButton.setImageResource(
                    if (isLiked) R.drawable.ic_favorite else R.drawable.ic_favorite_border
                )
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentPosition.collect { position ->
                binding.seekBar.progress = position
                binding.currentTimeText.text = formatTime(position)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.duration.collect { duration ->
                if (duration > 0) {
                    binding.seekBar.max = duration
                    binding.totalTimeText.text = formatTime(duration)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentTrack.collect { track ->
                track?.let { setupUI(it) }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.showMessage.collect { message ->
                message?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                    viewModel.clearMessage()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.error.collect { error ->
                error?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isDownloaded.collect { isDownloaded ->
                if (isDownloaded) {
                    binding.downloadButton.setImageResource(R.drawable.ic_downloaded_indicator)
                    binding.downloadButton.alpha = 0.5f
                } else {
                    binding.downloadButton.setImageResource(R.drawable.ic_download)
                    binding.downloadButton.alpha = 1.0f
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.downloadProgress.collect { progress ->
                when {
                    progress.isDownloading -> {
                        binding.downloadProgressBar?.visibility = View.VISIBLE
                        binding.downloadProgressBar?.progress = progress.progress
                    }
                    progress.isComplete -> {
                        binding.downloadProgressBar?.visibility = View.GONE
                        Toast.makeText(requireContext(), "Download complete!", Toast.LENGTH_SHORT).show()
                    }
                    progress.error != null -> {
                        binding.downloadProgressBar?.visibility = View.GONE
                        Toast.makeText(requireContext(), progress.error, Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        binding.downloadProgressBar?.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun formatTime(milliseconds: Int): String {
        val seconds = (milliseconds / 1000) % 60
        val minutes = (milliseconds / 1000) / 60
        return String.format("%d:%02d", minutes, seconds)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}