package com.example.hearo

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.hearo.databinding.ActivityMainBinding
import com.example.hearo.service.MusicPlayerService
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MusicPlayerService.init(this)

        setupNavigation()
        setupMiniPlayer()
        observeMiniPlayer()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginFragment, R.id.playerFragment -> {
                    binding.bottomNavigation.visibility = View.GONE
                    binding.miniPlayerContainer.visibility = View.GONE
                }
                else -> {
                    binding.bottomNavigation.visibility = View.VISIBLE

                    if (MusicPlayerService.currentTrack.value != null) {
                        binding.miniPlayerContainer.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun setupMiniPlayer() {
        binding.miniPlayer.onPlayPauseClick = {
            MusicPlayerService.togglePlayPause()
        }

        binding.miniPlayer.onNextClick = {
            MusicPlayerService.playNext()
        }

        binding.miniPlayer.onPlayerClick = {
            MusicPlayerService.currentTrack.value?.let { track ->
                val bundle = bundleOf(
                    "track" to track,
                    "trackList" to ArrayList(listOf(track)),
                    "currentIndex" to 0
                )
                navController.navigate(R.id.playerFragment, bundle)
            }
        }
    }

    private fun observeMiniPlayer() {
        lifecycleScope.launch {
            MusicPlayerService.currentTrack.collect { track ->
                if (track != null) {
                    binding.miniPlayer.updateTrack(track)

                    val currentDestination = navController.currentDestination?.id
                    if (currentDestination != R.id.loginFragment &&
                        currentDestination != R.id.playerFragment) {
                        binding.miniPlayerContainer.visibility = View.VISIBLE
                    }
                } else {
                    binding.miniPlayerContainer.visibility = View.GONE
                }
            }
        }

        lifecycleScope.launch {
            MusicPlayerService.isPlaying.collect { isPlaying ->
                binding.miniPlayer.updatePlayingState(isPlaying)
            }
        }

        lifecycleScope.launch {
            MusicPlayerService.currentPosition.collect { position ->
                val duration = MusicPlayerService.duration.value
                if (duration > 0) {
                    val progress = position.toFloat() / duration.toFloat()
                    binding.miniPlayer.updateProgress(progress)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            MusicPlayerService.release()
        }
    }
}