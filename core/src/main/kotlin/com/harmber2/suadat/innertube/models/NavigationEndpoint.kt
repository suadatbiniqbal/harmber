/*
 * harmber (2026)
 * © Rukamori — github.com/suadatbiniqbal
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.harmber2.suadat.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class NavigationEndpoint(
    val watchEndpoint: WatchEndpoint? = null,
    val watchPlaylistEndpoint: WatchEndpoint? = null,
    val browseEndpoint: BrowseEndpoint? = null,
    val searchEndpoint: SearchEndpoint? = null,
    val queueAddEndpoint: QueueAddEndpoint? = null,
    val shareEntityEndpoint: ShareEntityEndpoint? = null,
) {
    val endpoint: Endpoint?
        get() =
            watchEndpoint
                ?: watchPlaylistEndpoint
                ?: browseEndpoint
                ?: searchEndpoint
                ?: queueAddEndpoint
                ?: shareEntityEndpoint

    val anyWatchEndpoint: WatchEndpoint?
        get() =
            watchEndpoint
                ?: watchPlaylistEndpoint
}
