package com.harmber.suadat.innertube.pages

import com.harmber.suadat.innertube.models.YTItem

data class ArtistItemsContinuationPage(
    val items: List<YTItem>,
    val continuation: String?,
)
