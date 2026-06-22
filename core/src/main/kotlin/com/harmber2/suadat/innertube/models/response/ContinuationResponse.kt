/*
 * harmber (2026)
 * © Rukamori — github.com/suadatbiniqbal
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.harmber2.suadat.innertube.models.response

import kotlinx.serialization.Serializable
import com.harmber2.suadat.innertube.models.MusicShelfRenderer

@Serializable
data class ContinuationResponse(
    val onResponseReceivedActions: List<ResponseAction>?,
) {
    @Serializable
    data class ResponseAction(
        val appendContinuationItemsAction: ContinuationItems?,
    )

    @Serializable
    data class ContinuationItems(
        val continuationItems: List<MusicShelfRenderer.Content>?,
    )
}
