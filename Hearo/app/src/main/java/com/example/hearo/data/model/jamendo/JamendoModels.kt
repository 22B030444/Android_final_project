package com.example.hearo.data.model.jamendo

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class JamendoSearchResponse(
    val headers: JamendoHeaders,
    val results: List<JamendoTrack>
) : Parcelable

@Parcelize
data class JamendoHeaders(
    val status: String,

    @SerializedName("results_count")
    val resultsCount: Int
) : Parcelable

@Parcelize
data class JamendoTrack(
    val id: String,
    val name: String,

    @SerializedName("artist_name")
    val artistName: String,

    @SerializedName("album_name")
    val albumName: String,

    @SerializedName("album_image")
    val albumImage: String?,

    val duration: Int, // в секундах

    val audio: String,          // Streaming URL
    val audiodownload: String,  // Download URL (полный трек!)

    @SerializedName("audiodownload_allowed")
    val audiodownloadAllowed: Boolean = true,

    val image: String?
) : Parcelable