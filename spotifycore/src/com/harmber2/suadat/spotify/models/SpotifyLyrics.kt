/*
 * harmber (2026)
 * © Rukamori — github.com/suadatbiniqbal
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.harmber2.suadat.spotify.models

import kotlinx.serialization.Serializable

@Serializable
data class SpotifyLyricsResponse(
    val lyrics: SpotifyLyrics? = null
)

@Serializable
data class SpotifyLyrics(
    val syncType: String = "", // SYNCED, UNSYNCED
    val lines: List<SpotifyLyricsLine> = emptyList(),
    val provider: String = "",
    val providerLyricsId: String = "",
    val providerDisplayName: String = "",
    val syncLinePrecision: String = ""
)

@Serializable
data class SpotifyLyricsLine(
    val startTimeMs: String = "",
    val words: String = "",
    val syllables: List<String> = emptyList(),
    val endTimeMs: String = "0"
)
