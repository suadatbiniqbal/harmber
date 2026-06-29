/*
 * harmber (2026)
 * © Rukamori — github.com/suadatbiniqbal
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.harmber2.suadat.models

import androidx.compose.runtime.Immutable
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
@Immutable
data class BannerAd(
    var id: String = "",
    var active: Boolean = false,
    var mediaUrl: String = "", // Image, Gif or Video URL
    var mediaType: String = "image", // "image", "gif", "video"
    var adFormat: String = "banner", // "banner" (Home screen) or "overlay" (Player art)
    var title: String = "",
    var subtitle: String = "",
    var buttonText: String = "Listen now",
    var actionUrl: String = "",
    var priority: Int = 0,
    // Overlay Specific
    var dailyLimit: Int = 3,
    var startHour: Int = 0,
    var endHour: Int = 23
)
