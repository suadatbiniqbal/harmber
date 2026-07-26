/*
 * harmber (2026)
 * © Rukamori — github.com/suadatbiniqbal
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.harmber2.suadat.models

import androidx.annotation.Keep
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
@Keep
data class AdConfig(
    var autoSwipeEnabled: Boolean = true,
    var swipeIntervalMs: Long = 5000,
    var seasonalEffect: String = "none", // "none", "snow", "rain", "hearts"
    var seasonalEffectDurationSeconds: Int = 10,
    var homeTitleColor: String = "",
    var festivalImageUrl: String = "",
    var fallingEffectUrl: String = ""
)
