package com.example.hearo.ui.player

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.hearo.R
import com.example.hearo.data.model.UniversalTrack
import com.example.hearo.data.model.MusicSource
import com.example.hearo.databinding.FragmentPlayerBinding
import kotlinx.coroutines.launch
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.example.hearo.utils.DownloadProgress

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

        // Получаем трек и список треков
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

        val duration = if (track.canDownloadFull) {
            track.durationMs
        } else {
            30000
        }

        binding.totalTimeText.text = formatTime(duration)
        binding.seekBar.max = duration

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

        // ⭐ Previous
        binding.previousButton.setOnClickListener {
            viewModel.playPrevious()
        }

        // ⭐ Next
        binding.nextButton.setOnClickListener {
            viewModel.playNext()
        }

        // ⭐ Shuffle
        binding.shuffleButton.setOnClickListener {
            viewModel.toggleShuffle()
        }

        // ⭐ Repeat
        binding.repeatButton.setOnClickListener {
            viewModel.toggleRepeat()
        }

        binding.favoriteButton.setOnClickListener {
            viewModel.toggleLike()
        }

        binding.downloadButton.setOnClickListener {
            viewModel.downloadTrack()
        }

        binding.menuButton.setOnClickListener {
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
        // Воспроизведение
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.mediaPlayer.isPlaying.collect { isPlaying ->
                if (isPlaying) {
                    binding.playPauseButton.setImageResource(R.drawable.ic_pause)
                } else {
                    binding.playPauseButton.setImageResource(R.drawable.ic_play)
                }
            }
        }

        // ⭐ Shuffle состояние
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isShuffleEnabled.collect { isEnabled ->
                if (isEnabled) {
                    binding.shuffleButton.setColorFilter(
                        ContextCompat.getColor(requireContext(), R.color.purple_primary)
                    )
                } else {
                    binding.shuffleButton.setColorFilter(
                        ContextCompat.getColor(requireContext(), R.color.white)
                    )
                }
            }
        }

        // ⭐ Repeat режим
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.repeatMode.collect { mode ->
                when (mode) {
                    RepeatMode.OFF -> {
                        binding.repeatButton.setImageResource(R.drawable.ic_repeat)
                        binding.repeatButton.setColorFilter(
                            ContextCompat.getColor(requireContext(), R.color.white)
                        )
                    }
                    RepeatMode.ALL -> {
                        binding.repeatButton.setImageResource(R.drawable.ic_repeat)
                        binding.repeatButton.setColorFilter(
                            ContextCompat.getColor(requireContext(), R.color.purple_primary)
                        )
                    }
                    RepeatMode.ONE -> {
                        binding.repeatButton.setImageResource(R.drawable.ic_repeat)
                        binding.repeatButton.setColorFilter(
                            ContextCompat.getColor(requireContext(), R.color.purple_primary)
                        )
                        // Можно добавить индикатор "1" на иконку
                    }
                }
            }
        }

        // Подготовка
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.mediaPlayer.isPreparing.collect { isPreparing ->
                // Можно показать индикатор загрузки
            }
        }

        // Ошибки
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.mediaPlayer.error.collect { error ->
                error?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Избранное
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLiked.collect { isLiked ->
                if (isLiked) {
                    binding.favoriteButton.setImageResource(R.drawable.ic_favorite)
                } else {
                    binding.favoriteButton.setImageResource(R.drawable.ic_favorite_border)
                }
            }
        }

        // Прогресс
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentPosition.collect { position ->
                binding.seekBar.progress = position
                binding.currentTimeText.text = formatTime(position)
            }
        }

        // Длительность
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.duration.collect { duration ->
                if (duration > 0) {
                    binding.seekBar.max = duration
                    binding.totalTimeText.text = formatTime(duration)
                }
            }
        }

        // Смена трека
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentTrack.collect { track ->
                track?.let {
                    setupUI(it)
                }
            }
        }

        // Прогресс скачивания
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.downloadProgress.collect { progress ->
                when (progress) {
                    is DownloadProgress.Downloading -> {
                        // Можно показать прогресс
                    }
                    is DownloadProgress.Success -> {
                        Toast.makeText(
                            requireContext(),
                            "Download completed!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    is DownloadProgress.Failed -> {
                        Toast.makeText(
                            requireContext(),
                            "Download failed: ${progress.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else -> {}
                }
            }
        }

        // Сообщения от ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.showMessage.collect { message ->
                message?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                    viewModel.clearMessage()
                }
            }
        }
    }

    private fun formatTime(milliseconds: Int): String {
        val seconds = (milliseconds / 1000) % 60
        val minutes = (milliseconds / 1000) / 60
        return String.format("%d:%02d", minutes, seconds)
    }

    override fun onPause() {
        super.onPause()
        viewModel.mediaPlayer.pause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

