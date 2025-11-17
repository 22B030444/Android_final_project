package com.example.hearo.data.api

import com.example.hearo.data.preferences.AppPreferences
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val preferences: AppPreferences
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Не добавляем токен для запросов авторизации
        if (originalRequest.url.encodedPath.contains("/api/token")) {
            return chain.proceed(originalRequest)
        }

        // Добавляем Bearer token ко всем остальным запросам
        val token = preferences.accessToken

        val newRequest = if (token != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(newRequest)
    }
}