package com.harmber.suadat.innertube.models.body

import com.harmber.suadat.innertube.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class GetSearchSuggestionsBody(
    val context: Context,
    val input: String,
)
