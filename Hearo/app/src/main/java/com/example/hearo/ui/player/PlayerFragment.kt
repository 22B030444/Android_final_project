package com.example.hearo.ui.player

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.hearo.R
import com.example.hearo.data.model.spotify.Track
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

        // ⭐ ПОЛУЧАЕМ ТРЕК ИЗ BUNDLE
        val track = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("track", Track::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable("track")
        }

        if (track == null) {
            // Если трека нет - возвращаемся назад
            findNavController().navigateUp()
            return
        }

        // Устанавливаем трек в ViewModel
        viewModel.setTrack(track)

        setupUI(track)
        setupClickListeners()
        observePlaybackState()
        observeLikeState()
        observeProgress()
    }

    private fun setupUI(track: Track) {
        // Название и исполнитель
        binding.trackNameText.text = track.name
        binding.artistNameText.text = track.artists.joinToString(", ") { it.name }

        // Обложка альбома
        val imageUrl = track.album.images.firstOrNull()?.url
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.color.surface_dark)
            .error(R.color.surface_dark)
            .into(binding.albumCoverImage)

        // Общее время (превью всегда 30 сек)
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

        // Favorite
        binding.favoriteButton.setOnClickListener {
            viewModel.toggleLike()
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

        // Заглушки для остальных кнопок
        binding.previousButton.setOnClickListener {
            // TODO: Previous track
        }

        binding.nextButton.setOnClickListener {
            // TODO: Next track
        }

        binding.shuffleButton.setOnClickListener {
            // TODO: Shuffle
        }

        binding.repeatButton.setOnClickListener {
            // TODO: Repeat
        }

        binding.downloadButton.setOnClickListener {
            // TODO: Download
        }

        binding.menuButton.setOnClickListener {
            // TODO: Menu
        }
    }

    private fun observePlaybackState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.mediaPlayer.isPlaying.collect { isPlaying ->
                // Меняем иконку Play/Pause
                if (isPlaying) {
                    binding.playPauseButton.setImageResource(R.drawable.ic_pause)
                } else {
                    binding.playPauseButton.setImageResource(R.drawable.ic_play)
                }
            }
        }
    }

    private fun observeLikeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLiked.collect { isLiked ->
                // Меняем иконку избранного
                if (isLiked) {
                    binding.favoriteButton.setImageResource(R.drawable.ic_favorite)
                } else {
                    binding.favoriteButton.setImageResource(R.drawable.ic_favorite_border)
                }
            }
        }
    }

    private fun observeProgress() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentPosition.collect { position ->
                binding.seekBar.progress = position
                binding.currentTimeText.text = formatTime(position)
            }
        }
    }

    /**
     * Форматировать время в мм:сс
     */
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


