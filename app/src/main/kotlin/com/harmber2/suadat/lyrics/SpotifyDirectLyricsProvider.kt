/*
 * harmber (2026)
 * © Rukamori — github.com/suadatbiniqbal
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.harmber2.suadat.lyrics

import android.content.Context
import com.harmber2.suadat.spotify.Spotify
import com.harmber2.suadat.spotify.SpotifyTrackMatcher
import com.harmber2.suadat.spotify.models.SpotifyLyrics
import com.harmber2.suadat.utils.dataStore
import com.harmber2.suadat.utils.get
import java.util.Locale

object SpotifyDirectLyricsProvider : LyricsProvider {
    override val name = "Spotify (Direct)"

    override fun isEnabled(context: Context): Boolean = Spotify.isAuthenticated()

    override suspend fun getLyrics(
        id: String,
        title: String,
        artist: String,
        album: String?,
        duration: Int,
    ): Result<String> = runCatching {
        if (!Spotify.isAuthenticated()) throw IllegalStateException("Spotify not connected")

        val spotifyTrackId = getSpotifyTrackId(id, title, artist, duration)
            ?: throw IllegalStateException("Spotify match not found")

        val lyrics = Spotify.lyrics(spotifyTrackId).getOrNull()
            ?: throw IllegalStateException("Spotify lyrics not available")

        formatSpotifyLyrics(lyrics)
    }

    private suspend fun getSpotifyTrackId(
        mediaId: String,
        title: String,
        artist: String,
        durationSec: Int?
    ): String? {
        return SpotifyTrackMatcher.matchTrack(mediaId, title, artist, durationSec)
    }

    private fun formatSpotifyLyrics(lyrics: SpotifyLyrics): String {
        if (lyrics.lines.isEmpty()) return ""
        
        val isSynced = lyrics.syncType.equals("SYNCED", ignoreCase = true)
        
        return lyrics.lines.joinToString("\n") { line ->
            if (isSynced) {
                val ms = line.startTimeMs.toLongOrNull() ?: 0L
                val minutes = ms / 1000 / 60
                val seconds = (ms / 1000) % 60
                val hundredths = (ms % 1000) / 10
                val time = String.format(Locale.US, "[%02d:%02d.%02d]", minutes, seconds, hundredths)
                "$time${line.words}"
            } else {
                line.words
            }
        }
    }

    override suspend fun getAllLyrics(
        id: String,
        title: String,
        artist: String,
        album: String?,
        duration: Int,
        callback: (String) -> Unit,
    ) {
        getLyrics(id, title, artist, album, duration).onSuccess(callback)
    }
}
