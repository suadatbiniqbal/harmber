/*
 * harmber (2026)
 * © Rukamori — github.com/suadatbiniqbal
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.harmber2.suadat.spotify

import androidx.media3.common.MediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import com.harmber2.suadat.models.MediaMetadata
import com.harmber2.suadat.playback.queues.Queue
import com.harmber2.suadat.spotify.models.SpotifyTrack

class SpotifyPlaylistQueue(
    private val playlistId: String,
    private val title: String? = null,
    private val initialTracks: List<SpotifyTrack> = emptyList(),
    private val startIndex: Int = 0,
    override val preloadItem: MediaMetadata? = null,
) : Queue {
    private val allTracks = mutableListOf<SpotifyTrack>()
    private var resolveOffset = 0
    private var apiFetchOffset = 0
    private var apiTotal = 0
    private var apiHasMore = true

    override suspend fun getInitialStatus(): Queue.Status =
        withContext(Dispatchers.IO) {
            if (initialTracks.isNotEmpty()) {
                allTracks += initialTracks
                apiTotal = initialTracks.size
                apiFetchOffset = apiTotal
                apiHasMore = false
            } else {
                fetchNextApiPage()
            }

            while (startIndex >= allTracks.size && apiHasMore) {
                fetchNextApiPage()
            }

            if (allTracks.isEmpty()) {
                return@withContext Queue.Status(title = title, items = emptyList(), mediaItemIndex = 0)
            }

            // Optimize: Resolve tracks until we find at least one playable, with a window
            val maxInitialToResolve = 15
            val resolvedEntries = mutableListOf<Pair<Int, MediaItem>>()
            var currentTargetIndex = startIndex.coerceIn(allTracks.indices)
            
            var checkOffset = currentTargetIndex
            while (resolvedEntries.isEmpty() && checkOffset < (currentTargetIndex + maxInitialToResolve).coerceAtMost(allTracks.size)) {
                val windowTracks = allTracks.subList(checkOffset, (checkOffset + 5).coerceAtMost(allTracks.size))
                resolvedEntries += resolveTrackEntries(windowTracks, startOffset = checkOffset)
                checkOffset += windowTracks.size
            }
            
            // If still empty, try backwards from target
            if (resolvedEntries.isEmpty()) {
                checkOffset = (currentTargetIndex - 1).coerceAtLeast(0)
                while (resolvedEntries.isEmpty() && checkOffset >= (currentTargetIndex - 5).coerceAtLeast(0)) {
                    val track = allTracks[checkOffset]
                    SpotifyPlaybackResolver.resolveToMediaItem(track)?.let {
                        resolvedEntries.add(checkOffset to it)
                    }
                    checkOffset--
                }
            }

            val resolvedItems = resolvedEntries.map { it.second }
            resolveOffset = (currentTargetIndex + maxInitialToResolve).coerceAtMost(allTracks.size)

            if (resolvedItems.isEmpty()) {
                return@withContext Queue.Status(title = title, items = emptyList(), mediaItemIndex = 0)
            }

            Queue.Status(
                title = title,
                items = resolvedItems,
                mediaItemIndex =
                    resolvedEntries
                        .indexOfFirst { it.first >= currentTargetIndex }
                        .takeIf { it >= 0 }
                        ?: 0,
            )
        }

    override fun hasNextPage(): Boolean = resolveOffset < allTracks.size || apiHasMore

    override suspend fun nextPage(): List<MediaItem> =
        withContext(Dispatchers.IO) {
            if (resolveOffset >= allTracks.size && apiHasMore) {
                fetchNextApiPage()
            }
            if (resolveOffset >= allTracks.size) return@withContext emptyList()

            val end = (resolveOffset + RESOLVE_BATCH_SIZE).coerceAtMost(allTracks.size)
            val batch = allTracks.subList(resolveOffset, end)
            val resolved = resolveTrackEntries(batch, startOffset = resolveOffset).map { it.second }
            resolveOffset = end
            resolved
        }

    private suspend fun resolveTrackEntries(
        tracks: List<SpotifyTrack>,
        startOffset: Int
    ): List<Pair<Int, MediaItem>> =
        coroutineScope {
            tracks.mapIndexed { index, track ->
                async {
                    SpotifyPlaybackResolver
                        .resolveToMediaItem(track)
                        ?.let { mediaItem -> (startOffset + index) to mediaItem }
                }
            }.awaitAll().filterNotNull()
        }

    private suspend fun fetchNextApiPage() {
        if (!apiHasMore) return
        val result =
            Spotify
                .playlistTracks(
                    playlistId = playlistId,
                    limit = SPOTIFY_PAGE_SIZE,
                    offset = apiFetchOffset,
                ).getOrThrow()
        apiTotal = result.total
        val fetched = result.items.mapNotNull { it.track?.takeUnless(SpotifyTrack::isLocal) }
        allTracks += fetched
        apiFetchOffset += result.items.size
        apiHasMore = apiFetchOffset < apiTotal
    }

    companion object {
        private const val SPOTIFY_PAGE_SIZE = 50
        private const val RESOLVE_BATCH_SIZE = 20
    }
}
