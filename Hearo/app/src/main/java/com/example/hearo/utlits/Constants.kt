package com.example.hearo.utils

object Constants {
    // Spotify Auth
    const val SPOTIFY_AUTH_URL = "https://accounts.spotify.com/authorize"
    const val SPOTIFY_TOKEN_URL = "https://accounts.spotify.com/api/token"
    const val SPOTIFY_API_BASE_URL = "https://api.spotify.com/v1/"

    const val REDIRECT_URI = "hearo://callback"

    // Scopes - что мы запрашиваем у пользователя
    val SPOTIFY_SCOPES = listOf(
        "user-read-private",           // Читать профиль
        "user-read-email",             // Читать email
        "user-library-read",           // Читать Liked Songs
        "user-library-modify",         // Добавлять/удалять Liked Songs
        "playlist-read-private",       // Читать приватные плейлисты
        "playlist-read-collaborative", // Читать коллаборативные плейлисты
        "playlist-modify-public",      // Изменять публичные плейлисты
        "playlist-modify-private",     // Изменять приватные плейлисты
        "user-read-recently-played",   // Recently played
        "user-top-read",               // Топ треки/артисты
        "user-follow-read",            // Читать followed артистов
        "user-follow-modify",          // Follow/unfollow артистов
        "user-read-playback-state",    // Статус воспроизведения
        "user-modify-playback-state",  // Управлять воспроизведением
        "user-read-currently-playing"  // Текущий трек
    ).joinToString(" ")

    // SharedPreferences keys
    const val PREFS_NAME = "hearo_prefs"
    const val KEY_ACCESS_TOKEN = "access_token"
    const val KEY_REFRESH_TOKEN = "refresh_token"
    const val KEY_TOKEN_EXPIRY = "token_expiry"
    const val JAMENDO_CLIENT_ID = "7337c84f"
}