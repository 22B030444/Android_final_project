package com.example.hearo.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.hearo.R
import com.example.hearo.data.auth.SpotifyAuthActivity
import com.example.hearo.data.preferences.AppPreferences
import com.example.hearo.data.repository.AuthRepository
import com.example.hearo.databinding.FragmentLoginBinding
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var authRepository: AuthRepository

    private val authLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            SpotifyAuthActivity.RESULT_AUTH_SUCCESS -> {
                val code = result.data?.getStringExtra(SpotifyAuthActivity.EXTRA_AUTH_CODE)
                code?.let { handleAuthCode(it) }
            }
            SpotifyAuthActivity.RESULT_AUTH_FAILED -> {
                Toast.makeText(
                    requireContext(),
                    "Authorization cancelled",
                    Toast.LENGTH_SHORT
                ).show()
                hideLoading()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authRepository = AuthRepository(AppPreferences(requireContext()))

        // Проверяем, может уже залогинен
        if (authRepository.isLoggedIn()) {
            navigateToHome()
            return
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Основная кнопка Log in
        binding.loginButton.setOnClickListener {
            onLoginClick()
        }

        // Социальные кнопки (все ведут к Spotify OAuth)
        binding.googleButton.setOnClickListener {
            onLoginClick()
        }

        binding.appleButton.setOnClickListener {
            onLoginClick()
        }

        binding.facebookButton.setOnClickListener {
            onLoginClick()
        }

        // Forgot password (заглушка)
        binding.forgotPasswordText.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Please use Spotify app to reset password",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun onLoginClick() {
        val username = binding.usernameEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()

        // Валидация (опционально)
        if (username.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter username", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter password", Toast.LENGTH_SHORT).show()
            return
        }

        // Показываем что-то происходит
        showLoading()

        // Небольшая задержка для эффекта (опционально)
        binding.root.postDelayed({
            // Запускаем реальный Spotify OAuth
            startSpotifyAuth()
        }, 500)
    }

    private fun startSpotifyAuth() {
        val intent = Intent(requireContext(), SpotifyAuthActivity::class.java)
        authLauncher.launch(intent)
    }

    private fun handleAuthCode(code: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = authRepository.exchangeCodeForToken(code)

            result.onSuccess {
                Toast.makeText(
                    requireContext(),
                    "Welcome back!",
                    Toast.LENGTH_SHORT
                ).show()
                navigateToHome()
            }.onFailure { error ->
                hideLoading()
                Toast.makeText(
                    requireContext(),
                    "Login failed: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun navigateToHome() {
        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.loginButton.isEnabled = false
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.loginButton.isEnabled = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


