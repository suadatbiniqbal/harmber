/*
 * harmber (2026)
 * © Rukamori — github.com/suadatbiniqbal
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.harmber2.suadat.models

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class AdConfig(
    var autoSwipeEnabled: Boolean = true,
    var swipeIntervalMs: Long = 5000
)
