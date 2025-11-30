package com.example.hearo.ui.player

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.hearo.R
import com.example.hearo.data.model.spotify.Track
import com.example.hearo.databinding.FragmentPlayerBinding
import kotlinx.coroutines.launch
import android.view.LayoutInflater
import android.view.ViewGroup

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

        // Получаем трек из Bundle
        val track = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("track", Track::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable("track")
        }

        if (track == null) {
            findNavController().navigateUp()
            return
        }

        // Устанавливаем трек
        viewModel.setTrack(track)

        setupUI(track)
        setupClickListeners()
        observeStates()
    }

    private fun setupUI(track: Track) {
        binding.trackNameText.text = track.name
        binding.artistNameText.text = track.artists.joinToString(", ") { it.name }

        val imageUrl = track.album.images.firstOrNull()?.url
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.color.surface_dark)
            .error(R.color.surface_dark)
            .into(binding.albumCoverImage)

        binding.totalTimeText.text = formatTime(30000)
        binding.seekBar.max = 30000
    }

    private fun setupClickListeners() {
        // Кнопка назад
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        // Play/Pause
        binding.playPauseButton.setOnClickListener {
            viewModel.togglePlayPause()
        }

        // Previous
        binding.previousButton.setOnClickListener {
            viewModel.playPrevious()
        }

        // Next
        binding.nextButton.setOnClickListener {
            viewModel.playNext()
        }

        // Shuffle
        binding.shuffleButton.setOnClickListener {
            viewModel.toggleShuffle()
        }

        // Repeat
        binding.repeatButton.setOnClickListener {
            viewModel.toggleRepeat()
        }

        // Favorite
        binding.favoriteButton.setOnClickListener {
            viewModel.toggleLike()
        }

        // Download (заглушка)
        binding.downloadButton.setOnClickListener {
            Toast.makeText(requireContext(), "Download not available for preview", Toast.LENGTH_SHORT).show()
        }

        // Menu (заглушка)
        binding.menuButton.setOnClickListener {
            Toast.makeText(requireContext(), "Menu", Toast.LENGTH_SHORT).show()
        }

        // SeekBar
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

        // Подготовка
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.mediaPlayer.isPreparing.collect { isPreparing ->
                // Можно показать индикатор загрузки
                if (isPreparing) {
                    // Показываем что идет загрузка
                }
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

        // Shuffle
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isShuffle.collect { isShuffle ->
                if (isShuffle) {
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

        // Repeat
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.repeatMode.collect { mode ->
                when (mode) {
                    RepeatMode.OFF -> {
                        binding.repeatButton.setColorFilter(
                            ContextCompat.getColor(requireContext(), R.color.white)
                        )
                    }
                    RepeatMode.ALL, RepeatMode.ONE -> {
                        binding.repeatButton.setColorFilter(
                            ContextCompat.getColor(requireContext(), R.color.purple_primary)
                        )
                    }
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
    }

    private fun formatTime(milliseconds: Int): String {
        val seconds = (milliseconds / 1000) % 60
        val minutes = (milliseconds / 1000) / 60
        return String.format("%d:%02d", minutes, seconds)
    }

    override fun onPause() {
        super.onPause()
        // Приостанавливаем воспроизведение когда уходим с экрана
        viewModel.mediaPlayer.pause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


