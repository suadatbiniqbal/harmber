package com.harmber.suadat.innertube.models.body

import com.harmber.suadat.innertube.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class PlaylistDeleteBody(
    val context: Context,
    val playlistId: String
)
