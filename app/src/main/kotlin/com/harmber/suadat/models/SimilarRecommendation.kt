package com.harmber.suadat.models

import com.harmber.suadat.innertube.models.YTItem
import com.harmber.suadat.db.entities.LocalItem

data class SimilarRecommendation(
    val title: LocalItem,
    val items: List<YTItem>,
)
