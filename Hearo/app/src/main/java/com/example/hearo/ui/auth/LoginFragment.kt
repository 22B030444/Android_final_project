package com.example.hearo.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hearo.R
import com.example.hearo.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

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

        setupClickListeners()
    }

    private fun setupClickListeners() {

        binding.loginButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Please enter username and password",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            Toast.makeText(
                requireContext(),
                "Welcome, $username!",
                Toast.LENGTH_SHORT
            ).show()
            navigateToHome()
        }

        binding.forgotPasswordText.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Password recovery coming soon!",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.googleButton.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Google login coming soon!",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.appleButton.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Apple login coming soon!",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.facebookButton.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Facebook login coming soon!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun navigateToHome() {
        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}