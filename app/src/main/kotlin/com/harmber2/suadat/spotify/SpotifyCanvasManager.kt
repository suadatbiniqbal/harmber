/*
 * harmber (2026)
 * © Rukamori — github.com/suadatbiniqbal
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.harmber2.suadat.spotify

import android.content.Context
import com.harmber2.suadat.canvas.models.CanvasArtwork
import com.harmber2.suadat.constants.SpotifyCanvasEnabledKey
import com.harmber2.suadat.spotify.models.SpotifyTrack
import com.harmber2.suadat.ui.player.CanvasArtworkPlaybackCache
import com.harmber2.suadat.utils.dataStore
import com.harmber2.suadat.utils.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber

object SpotifyCanvasManager {
    private val mutex = Mutex()
    private val spotifyTrackIdCache = mutableMapOf<String, String>() // mediaId -> spotifyTrackId

    suspend fun isEnabled(context: Context): Boolean {
        val enabled = context.dataStore.get(SpotifyCanvasEnabledKey, false)
        return enabled && Spotify.isAuthenticated()
    }

    suspend fun getCanvas(
        context: Context,
        mediaId: String,
        title: String,
        artist: String,
        durationSec: Int?
    ): CanvasArtwork? = withContext(Dispatchers.IO) {
        if (!isEnabled(context)) return@withContext null

        CanvasArtworkPlaybackCache.get(mediaId)?.let {
            if (!it.preferredAnimationUrl.isNullOrBlank()) return@withContext it
        }

        val canvas = CanvasRepository.getCanvas(context, mediaId, title, artist, durationSec) ?: return@withContext null
        
        Timber.tag("SpotifyCanvas").d("Found canvas for $title: ${canvas.videoUrl}")

        val artwork = CanvasArtwork(
            name = title,
            artist = canvas.artist,
            animated = canvas.videoUrl,
            videoUrl = canvas.videoUrl,
            animatedVertical = canvas.videoUrl,
            videoUrlVertical = canvas.videoUrl,
        )

        if (artwork.preferredAnimationUrl != null) {
            CanvasArtworkPlaybackCache.put(mediaId, artwork)
        }
        
        artwork
    }

    internal suspend fun getSpotifyTrackId(
        mediaId: String,
        title: String,
        artist: String,
        durationSec: Int?
    ): String? = mutex.withLock {
        spotifyTrackIdCache[mediaId]?.let { return it }

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
            spotifyTrackIdCache[mediaId] = trackId
            Timber.tag("SpotifyCanvas").d("Matched $title to Spotify track: $trackId (score: ${bestMatch.second})")
        } else {
            Timber.tag("SpotifyCanvas").w("No Spotify match found for $title by $artist")
        }
        trackId
    }

    suspend fun getRecommendations(
        context: Context,
        mediaId: String,
        title: String,
        artist: String,
        durationSec: Int?
    ): List<SpotifyTrack> = withContext(Dispatchers.IO) {
        if (!Spotify.isAuthenticated()) return@withContext emptyList()

        val trackId = getSpotifyTrackId(mediaId, title, artist, durationSec) ?: return@withContext emptyList()
        
        Spotify.recommendations(seedTrackIds = listOf(trackId)).getOrNull()?.tracks ?: emptyList()
    }
}
