package com.harmber.suadat.innertube.pages

import com.harmber.suadat.innertube.models.SongItem

data class PlaylistContinuationPage(
    val songs: List<SongItem>,
    val continuation: String?,
)
