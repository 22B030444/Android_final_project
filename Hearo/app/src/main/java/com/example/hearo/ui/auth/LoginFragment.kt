package com.example.hearo.ui.auth

import android.app.Activity
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

    // Activity Result Launcher для SpotifyAuthActivity
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

        // Инициализируем repository
        authRepository = AuthRepository(AppPreferences(requireContext()))

        // Проверяем, может пользователь уже залогинен
        if (authRepository.isLoggedIn()) {
            navigateToHome()
            return
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.loginButton.setOnClickListener {
            startSpotifyAuth()
        }
    }

    private fun startSpotifyAuth() {
        showLoading()
        val intent = Intent(requireContext(), SpotifyAuthActivity::class.java)
        authLauncher.launch(intent)
    }

    private fun handleAuthCode(code: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            showLoading()

            val result = authRepository.exchangeCodeForToken(code)

            result.onSuccess {
                Toast.makeText(
                    requireContext(),
                    "Login successful!",
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