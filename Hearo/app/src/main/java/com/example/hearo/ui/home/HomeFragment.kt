package com.example.hearo.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.hearo.data.preferences.AppPreferences
import com.example.hearo.data.repository.AuthRepository
import com.example.hearo.data.repository.MusicRepository
import com.example.hearo.databinding.FragmentHomeBinding
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var musicRepository: MusicRepository
    private lateinit var authRepository: AuthRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализируем repositories
        authRepository = AuthRepository(AppPreferences(requireContext()))
        musicRepository = MusicRepository(requireContext(), authRepository)

        // Загружаем профиль пользователя (тест API)
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewLifecycleOwner.lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE

            musicRepository.getUserProfile()
                .onSuccess { profile ->
                    binding.progressBar.visibility = View.GONE

                    // Показываем приветствие
                    val welcomeText = "Hi ${profile.displayName ?: "there"}!\nWelcome back!"
                    binding.textView.text = welcomeText

                    Toast.makeText(
                        requireContext(),
                        "Welcome, ${profile.displayName}!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .onFailure { error ->
                    binding.progressBar.visibility = View.GONE
                    binding.textView.text = "Failed to load profile\n${error.message}"

                    Toast.makeText(
                        requireContext(),
                        "Error: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}