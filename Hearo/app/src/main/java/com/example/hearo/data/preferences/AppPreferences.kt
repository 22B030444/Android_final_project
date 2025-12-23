package com.example.hearo.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.hearo.utils.Constants
import androidx.core.content.edit

class AppPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        Constants.PREFS_NAME,
        Context.MODE_PRIVATE
    )

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
        prefs.edit { remove("search_history") }
    }
}