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
            val query = if (primaryArtist.isNotEmpty()) "$primaryArtist ${track.name}" else track.name
            
            val searchResult =
                YouTube
                    .search(
                        query = query,
                        filter = YouTube.SearchFilter.FILTER_SONG,
                    ).getOrNull() 
                    ?: YouTube.search(
                        query = "${track.name} $primaryArtist",
                        filter = YouTube.SearchFilter.FILTER_SONG,
                    ).getOrNull()
                    ?: YouTube.search(
                        query = track.name,
                        filter = YouTube.SearchFilter.FILTER_SONG,
                    ).getOrNull()
                    ?: return@withContext null

            val candidates =
                searchResult.items
                    .filterIsInstance<SongItem>()
                    .distinctBy { it.id }
            
            if (candidates.isEmpty()) {
                // Try searching with only the title if combined query fails
                YouTube.search(track.name, YouTube.SearchFilter.FILTER_SONG).getOrNull()?.items
                    ?.filterIsInstance<SongItem>()
                    ?.firstOrNull()?.let { return@withContext resolveToMetadataFromSongItem(it, track) }
                return@withContext null
            }

            val precomputed =
                SpotifyMapper.precompute(
                    title = track.name,
                    artist = track.artists.joinToString(" ") { it.name },
                    durationMs = track.durationMs,
                )

            val (best, score) =
                candidates
                    .map { candidate ->
                        candidate to
                            SpotifyMapper.matchScorePrecomputed(
                                precomputed = precomputed,
                                candidateTitle = candidate.title,
                                candidateArtist = candidate.artists.joinToString(" ") { it.name },
                                candidateDurationSec = candidate.duration,
                            )
                    }.maxByOrNull { it.second }
                ?: return@withContext null
            
            if (score < MIN_MATCH_THRESHOLD) return@withContext null

            resolveToMetadataFromSongItem(best, track)
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
