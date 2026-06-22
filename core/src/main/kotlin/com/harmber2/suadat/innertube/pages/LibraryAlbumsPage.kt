/*
 * harmber (2026)
 * © Rukamori — github.com/suadatbiniqbal
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.harmber2.suadat.innertube.pages

import com.harmber2.suadat.innertube.models.Album
import com.harmber2.suadat.innertube.models.AlbumItem
import com.harmber2.suadat.innertube.models.Artist
import com.harmber2.suadat.innertube.models.ArtistItem
import com.harmber2.suadat.innertube.models.MusicResponsiveListItemRenderer
import com.harmber2.suadat.innertube.models.MusicTwoRowItemRenderer
import com.harmber2.suadat.innertube.models.PlaylistItem
import com.harmber2.suadat.innertube.models.SongItem
import com.harmber2.suadat.innertube.models.YTItem
import com.harmber2.suadat.innertube.models.oddElements
import com.harmber2.suadat.innertube.utils.parseTime

data class LibraryAlbumsPage(
    val albums: List<AlbumItem>,
    val continuation: String?,
) {
    companion object {
        fun fromMusicTwoRowItemRenderer(renderer: MusicTwoRowItemRenderer): AlbumItem? {
            val browseId = renderer.navigationEndpoint.browseEndpoint?.browseId ?: return null
            val playlistId =
                renderer.thumbnailOverlay
                    ?.musicItemThumbnailOverlayRenderer
                    ?.content
                    ?.musicPlayButtonRenderer
                    ?.playNavigationEndpoint
                    ?.watchPlaylistEndpoint
                    ?.playlistId
                    ?: renderer.menu
                        ?.menuRenderer
                        ?.items
                        ?.firstOrNull()
                        ?.menuNavigationItemRenderer
                        ?.navigationEndpoint
                        ?.watchPlaylistEndpoint
                        ?.playlistId
                    ?: browseId.removePrefix("MPREb_").let { "OLAK5uy_$it" }

            return AlbumItem(
                browseId = browseId,
                playlistId = playlistId,
                title =
                    renderer.title.runs
                        ?.firstOrNull()
                        ?.text ?: return null,
                artists = null,
                year =
                    renderer.subtitle
                        ?.runs
                        ?.lastOrNull()
                        ?.text
                        ?.toIntOrNull(),
                thumbnail = renderer.thumbnailRenderer.musicThumbnailRenderer?.getThumbnailUrl() ?: return null,
                explicit =
                    renderer.subtitleBadges?.find {
                        it.musicInlineBadgeRenderer?.icon?.iconType == "MUSIC_EXPLICIT_BADGE"
                    } != null,
            )
        }
    }
}
