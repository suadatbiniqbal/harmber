/*
 * harmber (2026)
 * © Rukamori — github.com/suadatbiniqbal
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.harmber2.suadat.models

import com.harmber2.suadat.innertube.models.YTItem

data class PlaylistSuggestion(
    val items: List<YTItem> = emptyList(),
    val continuation: String? = null,
    val currentQueryIndex: Int = 0,
    val totalQueries: Int = 0,
    val query: String = "",
    val hasMore: Boolean = true,
    val timestamp: Long = System.currentTimeMillis(),
)

data class PlaylistSuggestionPage(
    val items: List<YTItem> = emptyList(),
    val continuation: String? = null,
)

data class PlaylistSuggestionQuery(
    val query: String,
    val priority: Int,
)
