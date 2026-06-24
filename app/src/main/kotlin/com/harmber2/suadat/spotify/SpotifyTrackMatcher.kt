/*
 * harmber (2026)
 * © Rukamori — github.com/suadatbiniqbal
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.harmber2.suadat.spotify

import timber.log.Timber

object SpotifyTrackMatcher {
    private val trackIdCache = mutableMapOf<String, String>() // mediaId -> spotifyTrackId

    suspend fun matchTrack(
        mediaId: String,
        title: String,
        artist: String,
        durationSec: Int?
    ): String? {
        trackIdCache[mediaId]?.let { return it }

        val query = "$artist $title"
        val searchResult = Spotify.search(query, listOf("track"), limit = 5).getOrNull()
        val tracks = searchResult?.tracks?.items ?: return null

        val precomputed = SpotifyMapper.precompute(title, artist, durationSec?.times(1000) ?: 0)
        
        val bestMatch = tracks.map { track ->
            track to SpotifyMapper.matchScorePrecomputed(
                precomputed = precomputed,
                candidateTitle = track.name,
                candidateArtist = track.artists.joinToString(" ") { it.name },
                candidateDurationSec = track.durationMs / 1000
            )
        }.filter { it.second > 0.4 }.maxByOrNull { it.second }

        val trackId = bestMatch?.first?.id
        if (trackId != null) {
            trackIdCache[mediaId] = trackId
            Timber.tag("SpotifyMatcher").d("Matched $title to Spotify track: $trackId (score: ${bestMatch.second})")
        } else {
            Timber.tag("SpotifyMatcher").w("No Spotify match found for $title by $artist")
        }
        return trackId
    }
}
