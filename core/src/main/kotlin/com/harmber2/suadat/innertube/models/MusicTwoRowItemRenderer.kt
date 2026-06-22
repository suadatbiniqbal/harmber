/*
 * harmber (2026)
 * © Rukamori — github.com/suadatbiniqbal
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.harmber2.suadat.innertube.models

import kotlinx.serialization.Serializable
import com.harmber2.suadat.innertube.models.BrowseEndpoint.BrowseEndpointContextSupportedConfigs.BrowseEndpointContextMusicConfig.Companion.MUSIC_PAGE_TYPE_ALBUM
import com.harmber2.suadat.innertube.models.BrowseEndpoint.BrowseEndpointContextSupportedConfigs.BrowseEndpointContextMusicConfig.Companion.MUSIC_PAGE_TYPE_ARTIST
import com.harmber2.suadat.innertube.models.BrowseEndpoint.BrowseEndpointContextSupportedConfigs.BrowseEndpointContextMusicConfig.Companion.MUSIC_PAGE_TYPE_AUDIOBOOK
import com.harmber2.suadat.innertube.models.BrowseEndpoint.BrowseEndpointContextSupportedConfigs.BrowseEndpointContextMusicConfig.Companion.MUSIC_PAGE_TYPE_PLAYLIST

/**
 * Two row: a big thumbnail, a title, and a subtitle
 * Used in [GridRenderer] and [MusicCarouselShelfRenderer]
 * Item type: song, video, album, playlist, artist
 */
@Serializable
data class MusicTwoRowItemRenderer(
    val title: Runs,
    val subtitle: Runs?,
    val subtitleBadges: List<Badges>?,
    val menu: Menu?,
    val thumbnailRenderer: ThumbnailRenderer,
    val navigationEndpoint: NavigationEndpoint,
    val thumbnailOverlay: MusicResponsiveListItemRenderer.Overlay?,
) {
    val isSong: Boolean
        get() = navigationEndpoint.endpoint is WatchEndpoint
    val isPlaylist: Boolean
        get() =
            navigationEndpoint.browseEndpoint
                ?.browseEndpointContextSupportedConfigs
                ?.browseEndpointContextMusicConfig
                ?.pageType ==
                MUSIC_PAGE_TYPE_PLAYLIST
    val isAlbum: Boolean
        get() =
            navigationEndpoint.browseEndpoint
                ?.browseEndpointContextSupportedConfigs
                ?.browseEndpointContextMusicConfig
                ?.pageType ==
                MUSIC_PAGE_TYPE_ALBUM ||
                navigationEndpoint.browseEndpoint
                    ?.browseEndpointContextSupportedConfigs
                    ?.browseEndpointContextMusicConfig
                    ?.pageType ==
                MUSIC_PAGE_TYPE_AUDIOBOOK
    val isArtist: Boolean
        get() =
            navigationEndpoint.browseEndpoint
                ?.browseEndpointContextSupportedConfigs
                ?.browseEndpointContextMusicConfig
                ?.pageType ==
                MUSIC_PAGE_TYPE_ARTIST
}
