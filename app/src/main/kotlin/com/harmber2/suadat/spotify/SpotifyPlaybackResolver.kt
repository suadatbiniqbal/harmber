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
import timber.log.Timber

object SpotifyPlaybackResolver {
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
                Timber.tag("SpotifyPlaybackResolver").d("Searching for: $query")
                // Try with account context first
                var searchResult = YouTube.search(query, YouTube.SearchFilter.FILTER_SONG).getOrNull()
                
                // Fallback to searching without account context if no results (prevents breakage if cookies are bad)
                if (searchResult == null || searchResult.items.isEmpty()) {
                    searchResult = YouTube.search(query, YouTube.SearchFilter.FILTER_SONG, useAccountContext = false).getOrNull()
                }

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
                    
                    Timber.tag("SpotifyPlaybackResolver").d("Best candidate: ${currentBest.title}, score: $currentScore")
                    if (currentScore > bestScore) {
                        bestCandidate = currentBest
                        bestScore = currentScore
                    }
                    
                    if (bestScore >= 0.9) break // Good enough
                }
            }

            // Fallback 1: Search videos if song match is weak
            if (bestScore < 0.7) {
                Timber.tag("SpotifyPlaybackResolver").d("Weak match, falling back to video search")
                var fallbackResult = YouTube.search(queries.first(), YouTube.SearchFilter.FILTER_VIDEO).getOrNull()
                if (fallbackResult == null || fallbackResult.items.isEmpty()) {
                    fallbackResult = YouTube.search(queries.first(), YouTube.SearchFilter.FILTER_VIDEO, useAccountContext = false).getOrNull()
                }

                val fallbackCandidates = fallbackResult?.items?.filterIsInstance<SongItem>().orEmpty()
                if (fallbackCandidates.isNotEmpty()) {
                    val (currentBest, currentScore) = fallbackCandidates
                        .map { candidate ->
                            candidate to SpotifyMapper.matchScorePrecomputed(
                                precomputed = precomputed,
                                candidateTitle = candidate.title,
                                candidateArtist = candidate.artists.joinToString(" ") { it.name },
                                candidateDurationSec = candidate.duration,
                            )
                        }.maxByOrNull { it.second } ?: (null to 0.0)
                    
                    if (currentBest != null && currentScore > bestScore) {
                        bestCandidate = currentBest
                        bestScore = currentScore
                        Timber.tag("SpotifyPlaybackResolver").d("Best video candidate: ${currentBest.title}, score: $currentScore")
                    }
                }
            }

            // Fallback 2: Search without any filter if still no good match
            if (bestCandidate == null || bestScore < 0.2) {
                Timber.tag("SpotifyPlaybackResolver").d("Still no good match, trying search without filters")
                val filterlessResult = YouTube.search(queries.first(), YouTube.SearchFilter("")).getOrNull()
                val filterlessCandidates = filterlessResult?.items?.filterIsInstance<SongItem>().orEmpty()
                if (filterlessCandidates.isNotEmpty()) {
                    val (currentBest, currentScore) = filterlessCandidates
                        .map { candidate ->
                            candidate to SpotifyMapper.matchScorePrecomputed(
                                precomputed = precomputed,
                                candidateTitle = candidate.title,
                                candidateArtist = candidate.artists.joinToString(" ") { it.name },
                                candidateDurationSec = candidate.duration,
                            )
                        }.maxByOrNull { it.second } ?: (null to 0.0)

                    if (currentBest != null && currentScore > bestScore) {
                        bestCandidate = currentBest
                        bestScore = currentScore
                        Timber.tag("SpotifyPlaybackResolver").d("Best filterless candidate: ${currentBest.title}, score: $currentScore")
                    }
                }
            }

            // Fallback 3: Search just the track name if still nothing
            if (bestCandidate == null) {
                Timber.tag("SpotifyPlaybackResolver").d("No candidate found, trying title-only search")
                val broadResult = YouTube.search(track.name, YouTube.SearchFilter.FILTER_SONG).getOrNull()
                bestCandidate = broadResult?.items?.filterIsInstance<SongItem>()?.firstOrNull()
                if (bestCandidate != null) bestScore = 0.15
            }
            
            val resultCandidate = bestCandidate ?: run {
                Timber.tag("SpotifyPlaybackResolver").w("No result found for track ${track.name}")
                return@withContext null
            }
            if (bestScore < 0.05) return@withContext null

            val metadata = resolveToMetadataFromSongItem(resultCandidate, track)
            mutex.withLock { cache[track.id] = metadata }
            metadata
        }

    private fun resolveToMetadataFromSongItem(songItem: SongItem, track: SpotifyTrack): MediaMetadata {
        val bestMetadata = songItem.toMediaMetadata()
        return bestMetadata.copy(
            thumbnailUrl = SpotifyMapper.getTrackThumbnail(track) ?: songItem.thumbnail,
            duration = if (track.durationMs > 0) track.durationMs / 1000 else songItem.duration ?: -1,
            explicit = track.explicit || songItem.explicit,
            album =
                track.album?.let { MediaMetadata.Album(id = it.id, title = it.name) }
                    ?: bestMetadata.album,
            spotifyTrackId = track.id.takeIf(String::isNotBlank),
        )
    }
}
