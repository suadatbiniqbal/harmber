/*
 * harmber (2026)
 * © Rukamori — github.com/suadatbiniqbal
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.harmber2.suadat.spotify

import androidx.media3.common.MediaItem
import com.harmber2.suadat.extensions.toMediaItem
import com.harmber2.suadat.models.MediaMetadata
import com.harmber2.suadat.playback.queues.Queue
import com.harmber2.suadat.spotify.models.SpotifyTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

class SpotifyRecommendationsQueue(
    private val seedTracks: List<SpotifyTrack>,
    private val title: String? = "Spotify Recommendations",
    private val startIndex: Int = 0,
    override val preloadItem: MediaMetadata? = null,
) : Queue {
    private val allTracks = seedTracks
    private var resolveOffset = 0

    override suspend fun getInitialStatus(): Queue.Status =
        withContext(Dispatchers.IO) {
            if (allTracks.isEmpty()) {
                return@withContext Queue.Status(title = title, items = emptyList(), mediaItemIndex = 0)
            }

            val targetIndex = startIndex.coerceIn(allTracks.indices)
            val resolvedEntries = mutableListOf<Pair<Int, MediaItem>>()
            
            preloadItem?.let {
                resolvedEntries.add(targetIndex to it.toMediaItem())
            }

            val initialBatch = allTracks.take(RESOLVE_BATCH_SIZE)
            val batchResults = resolveTrackEntries(initialBatch, startOffset = 0)
            
            // Add batch results that aren't the preloaded one
            resolvedEntries.addAll(batchResults.filter { entry -> preloadItem == null || entry.first != targetIndex })
            resolvedEntries.sortBy { it.first }

            val resolvedItems = resolvedEntries.map { it.second }
            resolveOffset = minOf(allTracks.size, RESOLVE_BATCH_SIZE)
            
            if (resolvedItems.isEmpty()) {
                return@withContext Queue.Status(title = title, items = emptyList(), mediaItemIndex = 0)
            }

            Queue.Status(
                title = title,
                items = resolvedItems,
                mediaItemIndex =
                    resolvedEntries
                        .indexOfFirst { it.first >= targetIndex }
                        .takeIf { it >= 0 }
                        ?: 0,
            )
        }

    override fun hasNextPage(): Boolean = resolveOffset < allTracks.size

    override suspend fun nextPage(): List<MediaItem> =
        withContext(Dispatchers.IO) {
            if (resolveOffset >= allTracks.size) return@withContext emptyList()

            val end = (resolveOffset + RESOLVE_BATCH_SIZE).coerceAtMost(allTracks.size)
            val batch = allTracks.subList(resolveOffset, end)
            val resolved = resolveTrackEntries(batch, startOffset = resolveOffset).map { it.second }
            resolveOffset = end
            resolved
        }

    private suspend fun resolveTracks(tracks: List<SpotifyTrack>, startOffset: Int): List<MediaItem> = 
        resolveTrackEntries(tracks, startOffset).map { it.second }

    private suspend fun resolveTrackEntries(tracks: List<SpotifyTrack>, startOffset: Int): List<Pair<Int, MediaItem>> =
        coroutineScope {
            tracks.mapIndexed { index, track ->
                async {
                    SpotifyPlaybackResolver
                        .resolveToMediaItem(track)
                        ?.let { mediaItem -> (startOffset + index) to mediaItem }
                }
            }.awaitAll().filterNotNull()
        }

    companion object {
        private const val RESOLVE_BATCH_SIZE = 10
    }
}
