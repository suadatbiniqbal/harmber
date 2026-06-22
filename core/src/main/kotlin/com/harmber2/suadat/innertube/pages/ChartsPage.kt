/*
 * harmber (2026)
 * © Rukamori — github.com/suadatbiniqbal
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.harmber2.suadat.innertube.pages

import com.harmber2.suadat.innertube.models.*

data class ChartsPage(
    val sections: List<ChartSection>,
    val continuation: String?,
) {
    data class ChartSection(
        val title: String,
        val items: List<YTItem>,
        val chartType: ChartType,
    )

    enum class ChartType {
        TRENDING,
        TOP,
        GENRE,
        NEW_RELEASES,
    }
}
