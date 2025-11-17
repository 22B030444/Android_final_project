package com.example.hearo.data.repository

import android.util.Base64
import android.util.Log
import com.example.hearo.BuildConfig
import com.example.hearo.data.api.RetrofitClient
import com.example.hearo.data.api.SpotifyAuthService
import com.example.hearo.data.model.auth.TokenResponse
import com.example.hearo.data.preferences.AppPreferences
import com.example.hearo.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
    private val preferences: AppPreferences
) {

    private val authService: SpotifyAuthService = RetrofitClient.getAuthService()

    /**
     * Обменивает authorization code на access token
     */
    suspend fun exchangeCodeForToken(code: String): Result<TokenResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val authHeader = getBasicAuthHeader()

                val response = authService.getAccessToken(
                    authorization = authHeader,
                    grantType = "authorization_code",
                    code = code,
                    redirectUri = Constants.REDIRECT_URI
                )

                // Сохраняем токены
                saveTokens(response)

                Log.d("AuthRepository", "Token received successfully")
                Result.success(response)

            } catch (e: Exception) {
                Log.e("AuthRepository", "Failed to get token", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Обновляет access token используя refresh token
     */
    suspend fun refreshAccessToken(): Result<TokenResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val refreshToken = preferences.refreshToken
                    ?: return@withContext Result.failure(Exception("No refresh token"))

                val authHeader = getBasicAuthHeader()

                val response = authService.refreshAccessToken(
                    authorization = authHeader,
                    grantType = "refresh_token",
                    refreshToken = refreshToken
                )

                // Сохраняем новые токены
                saveTokens(response)

                Log.d("AuthRepository", "Token refreshed successfully")
                Result.success(response)

            } catch (e: Exception) {
                Log.e("AuthRepository", "Failed to refresh token", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Проверяет валидность токена и обновляет если нужно
     */
    suspend fun ensureValidToken(): Boolean {
        return withContext(Dispatchers.IO) {
            if (preferences.isTokenValid()) {
                true
            } else {
                // Токен истек, пытаемся обновить
                val result = refreshAccessToken()
                result.isSuccess
            }
        }
    }

    /**
     * Выход из аккаунта
     */
    fun logout() {
        preferences.clearTokens()
    }

    /**
     * Проверяет авторизован ли пользователь
     */
    fun isLoggedIn(): Boolean {
        return preferences.accessToken != null
    }

    // ========================================
    // PRIVATE METHODS
    // ========================================

    private fun saveTokens(response: TokenResponse) {
        preferences.accessToken = response.accessToken
        preferences.refreshToken = response.refreshToken

        // Вычисляем время истечения (текущее время + expires_in)
        val expiryTime = System.currentTimeMillis() + (response.expiresIn * 1000)
        preferences.tokenExpiry = expiryTime
    }

    private fun getBasicAuthHeader(): String {
        val credentials = "${BuildConfig.SPOTIFY_CLIENT_ID}:${BuildConfig.SPOTIFY_CLIENT_SECRET}"
        val encodedCredentials = Base64.encodeToString(
            credentials.toByteArray(),
            Base64.NO_WRAP
        )
        return "Basic $encodedCredentials"
    }
}