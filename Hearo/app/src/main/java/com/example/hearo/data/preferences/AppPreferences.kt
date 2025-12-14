package com.example.hearo.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.hearo.utils.Constants

class AppPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        Constants.PREFS_NAME,
        Context.MODE_PRIVATE
    )

    var accessToken: String?
        get() = prefs.getString(Constants.KEY_ACCESS_TOKEN, null)
        set(value) = prefs.edit().putString(Constants.KEY_ACCESS_TOKEN, value).apply()

    var refreshToken: String?
        get() = prefs.getString(Constants.KEY_REFRESH_TOKEN, null)
        set(value) = prefs.edit().putString(Constants.KEY_REFRESH_TOKEN, value).apply()

    var tokenExpiry: Long
        get() = prefs.getLong(Constants.KEY_TOKEN_EXPIRY, 0)
        set(value) = prefs.edit().putLong(Constants.KEY_TOKEN_EXPIRY, value).apply()

    fun isTokenValid(): Boolean {
        val token = accessToken
        val expiry = tokenExpiry
        return token != null && System.currentTimeMillis() < expiry
    }

    fun clearTokens() {
        prefs.edit()
            .remove(Constants.KEY_ACCESS_TOKEN)
            .remove(Constants.KEY_REFRESH_TOKEN)
            .remove(Constants.KEY_TOKEN_EXPIRY)
            .apply()
    }


    fun saveSearchQuery(query: String) {
        val history = getSearchHistory().toMutableList()
        history.remove(query)
        history.add(0, query)
        val limitedHistory = history.take(10)
        prefs.edit()
            .putStringSet("search_history", limitedHistory.toSet())
            .apply()
    }

    fun getSearchHistory(): List<String> {
        return prefs.getStringSet("search_history", emptySet())
            ?.toList() ?: emptyList()
    }

    fun clearSearchHistory() {
        prefs.edit().remove("search_history").apply()
    }


    var isDarkTheme: Boolean
        get() = prefs.getBoolean("dark_theme", true)
        set(value) = prefs.edit().putBoolean("dark_theme", value).apply()
}