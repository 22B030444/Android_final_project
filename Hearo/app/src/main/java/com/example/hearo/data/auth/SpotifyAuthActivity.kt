package com.example.hearo.data.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.example.hearo.BuildConfig
import com.example.hearo.databinding.ActivitySpotifyAuthBinding
import com.example.hearo.utils.Constants

class SpotifyAuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySpotifyAuthBinding

    companion object {
        const val EXTRA_AUTH_CODE = "auth_code"
        const val RESULT_AUTH_SUCCESS = 100
        const val RESULT_AUTH_FAILED = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySpotifyAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWebView()
        loadAuthUrl()
    }

    private fun setupWebView() {
        binding.webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
        }

        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                url?.let {
                    // Проверяем если это redirect URI
                    if (it.startsWith(Constants.REDIRECT_URI)) {
                        handleRedirect(it)
                        return true
                    }
                }
                return false
            }
        }
    }

    private fun loadAuthUrl() {
        // Строим URL для авторизации
        val authUrl = Uri.parse(Constants.SPOTIFY_AUTH_URL).buildUpon()
            .appendQueryParameter("client_id", BuildConfig.SPOTIFY_CLIENT_ID)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("redirect_uri", Constants.REDIRECT_URI)
            .appendQueryParameter("scope", Constants.SPOTIFY_SCOPES)
            .appendQueryParameter("show_dialog", "true") // Всегда показывать диалог
            .build()
            .toString()

        binding.webView.loadUrl(authUrl)
    }

    private fun handleRedirect(url: String) {
        val uri = Uri.parse(url)

        // Получаем код авторизации
        val code = uri.getQueryParameter("code")
        val error = uri.getQueryParameter("error")

        if (code != null) {
            // Успешная авторизация
            val resultIntent = Intent().apply {
                putExtra(EXTRA_AUTH_CODE, code)
            }
            setResult(RESULT_AUTH_SUCCESS, resultIntent)
            finish()
        } else if (error != null) {
            // Ошибка или пользователь отменил
            setResult(RESULT_AUTH_FAILED)
            finish()
        }
    }
}