/*
 * harmber (2026)
 * © Rukamori — github.com/suadatbiniqbal
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.harmber2.suadat.spotify

import androidx.media3.common.MediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import com.harmber2.suadat.extensions.toMediaItem
import com.harmber2.suadat.innertube.YouTube
import com.harmber2.suadat.innertube.models.SongItem
import com.harmber2.suadat.models.MediaMetadata
import com.harmber2.suadat.models.toMediaMetadata
import com.harmber2.suadat.spotify.models.SpotifyTrack

object SpotifyPlaybackResolver {
    private const val MIN_MATCH_THRESHOLD = 0.25
    private const val CACHE_MAX_SIZE = 512

    private val mutex = Mutex()
    private val cache =
        object : LinkedHashMap<String, MediaMetadata>(CACHE_MAX_SIZE, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, MediaMetadata>?): Boolean = size > CACHE_MAX_SIZE
        }

    suspend fun resolveToMediaItem(track: SpotifyTrack): MediaItem? = resolveToMetadata(track)?.toMediaItem()

    suspend fun resolveToMetadata(track: SpotifyTrack): MediaMetadata? =
        withContext(Dispatchers.IO) {
            val cached = mutex.withLock { cache[track.id] }
            if (cached != null) return@withContext cached

            val primaryArtist = track.artists.firstOrNull()?.name.orEmpty()
            val artistsString = track.artists.joinToString(" ") { it.name }
            val queries = listOf(
                "$artistsString ${track.name}",
                "$primaryArtist ${track.name}",
                track.name
            ).distinct()
            
            var bestCandidate: SongItem? = null
            var bestScore = 0.0

            val precomputed =
                SpotifyMapper.precompute(
                    title = track.name,
                    artist = artistsString,
                    durationMs = track.durationMs,
                )

            for (query in queries) {
                val searchResult = YouTube.search(query, YouTube.SearchFilter.FILTER_SONG).getOrNull()
                val candidates = searchResult?.items?.filterIsInstance<SongItem>().orEmpty()
                
                if (candidates.isNotEmpty()) {
                    val (currentBest, currentScore) = candidates
                        .map { candidate ->
                            candidate to SpotifyMapper.matchScorePrecomputed(
                                precomputed = precomputed,
                                candidateTitle = candidate.title,
                                candidateArtist = candidate.artists.joinToString(" ") { it.name },
                                candidateDurationSec = candidate.duration,
                            )
                        }.maxByOrNull { it.second } ?: continue
                    
                    if (currentScore > bestScore) {
                        bestCandidate = currentBest
                        bestScore = currentScore
                    }
                    
                    if (bestScore >= 0.9) break // Good enough
                }
            }
            
            val resultCandidate = bestCandidate ?: return@withContext null
            if (bestScore < MIN_MATCH_THRESHOLD) return@withContext null

            val metadata = resolveToMetadataFromSongItem(resultCandidate, track)
            mutex.withLock { cache[track.id] = metadata }
            metadata
        }

    private suspend fun resolveToMetadataFromSongItem(songItem: SongItem, track: SpotifyTrack): MediaMetadata {
        val bestMetadata = songItem.toMediaMetadata()
        val metadata =
            bestMetadata.copy(
                thumbnailUrl = SpotifyMapper.getTrackThumbnail(track) ?: songItem.thumbnail,
                duration = if (track.durationMs > 0) track.durationMs / 1000 else songItem.duration ?: -1,
                explicit = track.explicit || songItem.explicit,
                album =
                    track.album?.let { MediaMetadata.Album(id = it.id, title = it.name) }
                        ?: bestMetadata.album,
                spotifyTrackId = track.id.takeIf(String::isNotBlank),
            )

        mutex.withLock {
            cache[track.id] = metadata
        }
        return metadata
    }
}
