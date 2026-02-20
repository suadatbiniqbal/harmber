package com.harmber.suadat.innertube.models.body

import com.harmber.suadat.innertube.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class GetTranscriptBody(
    val context: Context,
    val params: String,
)
