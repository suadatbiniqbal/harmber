package com.harmber.suadat.innertube.pages

import com.harmber.suadat.innertube.models.YTItem

data class LibraryContinuationPage(
    val items: List<YTItem>,
    val continuation: String?,
)
