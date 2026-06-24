/*
 * harmber (2026)
 * © Rukamori — github.com/suadatbiniqbal
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.harmber2.suadat.spotify

import android.content.Context
import com.harmber2.suadat.spotify.models.Canvas
import timber.log.Timber

object CanvasRepository {
    suspend fun getCanvas(
        context: Context,
        mediaId: String,
        title: String,
        artist: String,
        durationSec: Int?
    ): Canvas? {
        val accessToken = SpotifyAuthManager.getAccessToken(context) ?: return null
        
        val trackId = SpotifyTrackMatcher.matchTrack(mediaId, title, artist, durationSec) ?: return null
        
        val spotifyTrackUri = "spotify:track:$trackId"
        val spotifyCanvas = Spotify.canvas(spotifyTrackUri).getOrNull() ?: run {
            Timber.tag("CanvasRepo").w("No canvas found for $spotifyTrackUri")
            return null
        }
        
        if (spotifyCanvas.url.isBlank() || !spotifyCanvas.type.equals("VIDEO", ignoreCase = true)) {
            return null
        }
        
        return Canvas(
            videoUrl = spotifyCanvas.url,
            artist = spotifyCanvas.artistName ?: artist,
            trackId = trackId
        )
    }
}
