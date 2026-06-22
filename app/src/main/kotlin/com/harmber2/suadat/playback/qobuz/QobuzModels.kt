/*
 * harmber (2026)
 * © Rukamori — github.com/suadatbiniqbal
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.harmber2.suadat.playback.qobuz

import kotlinx.serialization.Serializable

@Serializable
data class QobuzResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: String? = null
)

@Serializable
data class QobuzSearchData(
    val tracks: QobuzTrackList
)

@Serializable
data class QobuzTrackList(
    val items: List<QobuzTrack>
)

@Serializable
data class QobuzTrack(
    val id: Long,
    val title: String,
    val duration: Int, // seconds
    val performer: QobuzPerformer,
    val album: QobuzAlbum,
    val maximum_bit_depth: Int,
    val maximum_sampling_rate: Float,
    val streamable: Boolean
)

@Serializable
data class QobuzPerformer(
    val name: String
)

@Serializable
data class QobuzAlbum(
    val title: String,
    val image: QobuzImage
)

@Serializable
data class QobuzImage(
    val large: String
)

@Serializable
data class QobuzDownloadData(
    val url: String
)
