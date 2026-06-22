/*
 * harmber (2026)
 * © Rukamori — github.com/suadatbiniqbal
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.harmber2.suadat.innertube.models.response

import kotlinx.serialization.Serializable
import com.harmber2.suadat.innertube.models.NavigationEndpoint
import com.harmber2.suadat.innertube.models.PlaylistPanelRenderer
import com.harmber2.suadat.innertube.models.Tabs
import com.harmber2.suadat.innertube.models.YouTubeDataPage

@Serializable
data class NextResponse(
    val contents: Contents,
    val continuationContents: ContinuationContents?,
    val currentVideoEndpoint: NavigationEndpoint?,
) {
    @Serializable
    data class Contents(
        val singleColumnMusicWatchNextResultsRenderer: SingleColumnMusicWatchNextResultsRenderer?,
        val twoColumnWatchNextResults: YouTubeDataPage.Contents.TwoColumnWatchNextResults?,
    ) {
        @Serializable
        data class SingleColumnMusicWatchNextResultsRenderer(
            val tabbedRenderer: TabbedRenderer?,
        ) {
            @Serializable
            data class TabbedRenderer(
                val watchNextTabbedResultsRenderer: WatchNextTabbedResultsRenderer?,
            ) {
                @Serializable
                data class WatchNextTabbedResultsRenderer(
                    val tabs: List<Tabs.Tab>,
                )
            }
        }
    }

    @Serializable
    data class ContinuationContents(
        val playlistPanelContinuation: PlaylistPanelRenderer,
    )
}
