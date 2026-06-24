/*
 * harmber (2026)
 * © Rukamori — github.com/suadatbiniqbal
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.harmber2.suadat.spotify.models

import kotlinx.serialization.Serializable

@Serializable
data class Canvas(
    val videoUrl: String,
    val artist: String,
    val trackId: String
)
