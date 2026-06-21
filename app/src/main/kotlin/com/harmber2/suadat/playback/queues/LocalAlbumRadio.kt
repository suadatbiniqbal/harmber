/*
 * harmber (2026)
 * © Rukamori — github.com/suadatbiniqbal
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.harmber2.suadat.playback.queues

import androidx.media3.common.MediaItem
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import com.harmber2.suadat.db.entities.AlbumWithSongs
import com.harmber2.suadat.extensions.toMediaItem
import com.harmber2.suadat.innertube.YouTube
import com.harmber2.suadat.innertube.models.WatchEndpoint
import com.harmber2.suadat.models.MediaMetadata

class LocalAlbumRadio(
    internal val albumWithSongs: AlbumWithSongs,
    internal val startIndex: Int = 0,
) : Queue {
    override val preloadItem: MediaMetadata? = null

    private lateinit var playlistId: String
    private val endpoint: WatchEndpoint
        get() =
            WatchEndpoint(
                playlistId = playlistId,
                params = "wAEB",
            )

    private var continuation: String? = null
    private var firstTimeLoaded: Boolean = false

    override suspend fun getInitialStatus(): Queue.Status =
        withContext(IO) {
            Queue.Status(
                title = albumWithSongs.album.title,
                items = albumWithSongs.songs.map { it.toMediaItem() },
                mediaItemIndex = startIndex,
            )
        }

    override fun hasNextPage(): Boolean = !firstTimeLoaded || continuation != null

    override suspend fun nextPage(): List<MediaItem> =
        withContext(IO) {
            if (!firstTimeLoaded) {
                playlistId =
                    YouTube
                        .album(albumWithSongs.album.id)
                        .getOrThrow()
                        .album.playlistId
                val nextResult = YouTube.next(endpoint, continuation).getOrThrow()
                continuation = nextResult.continuation
                firstTimeLoaded = true
                return@withContext nextResult.items
                    .subList(
                        albumWithSongs.songs.size,
                        nextResult.items.size,
                    ).map { it.toMediaItem() }
            }
            val nextResult = YouTube.next(endpoint, continuation).getOrThrow()
            continuation = nextResult.continuation
            nextResult.items.map { it.toMediaItem() }
        }
}
