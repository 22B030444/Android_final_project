package com.example.hearo.data.model.auth

import com.google.gson.annotations.SerializedName

data class TokenResponse(
    @SerializedName("access_token")
    val accessToken: String,

    @SerializedName("token_type")
    val tokenType: String,

    @SerializedName("expires_in")
    val expiresIn: Int, // секунды (обычно 3600 = 1 час)

    @SerializedName("refresh_token")
    val refreshToken: String?,

    @SerializedName("scope")
    val scope: String?
)