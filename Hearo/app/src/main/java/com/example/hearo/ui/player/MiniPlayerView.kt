package com.example.hearo.ui.player

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.example.hearo.R
import com.example.hearo.data.model.UniversalTrack
import com.example.hearo.databinding.ViewMiniPlayerBinding

class MiniPlayerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: ViewMiniPlayerBinding

    var onPlayPauseClick: (() -> Unit)? = null
    var onNextClick: (() -> Unit)? = null
    var onPlayerClick: (() -> Unit)? = null

    init {
        binding = ViewMiniPlayerBinding.inflate(LayoutInflater.from(context), this, true)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.playPauseButton.setOnClickListener {
            onPlayPauseClick?.invoke()
        }

        binding.nextButton.setOnClickListener {
            onNextClick?.invoke()
        }

        binding.root.setOnClickListener {
            onPlayerClick?.invoke()
        }
    }

    fun updateTrack(track: UniversalTrack) {
        binding.trackName.text = track.name
        binding.artistName.text = track.artistName

        Glide.with(context)
            .load(track.imageUrl)
            .placeholder(R.color.surface_dark)
            .error(R.color.surface_dark)
            .into(binding.albumImage)
    }

    fun updatePlayingState(isPlaying: Boolean) {
        if (isPlaying) {
            binding.playPauseButton.setImageResource(R.drawable.ic_pause)
        } else {
            binding.playPauseButton.setImageResource(R.drawable.ic_play)
        }
    }

    fun updateProgress(progress: Float) {
        binding.progressBar.progress = (progress * 100).toInt()
    }
}