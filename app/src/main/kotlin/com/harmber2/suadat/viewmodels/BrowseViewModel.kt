/*
 * harmber (2026)
 * © Rukamori — github.com/suadatbiniqbal
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.harmber2.suadat.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.harmber2.suadat.db.MusicDatabase
import com.harmber2.suadat.innertube.YouTube
import com.harmber2.suadat.innertube.models.AlbumItem
import com.harmber2.suadat.innertube.models.PlaylistItem
import com.harmber2.suadat.innertube.models.YTItem
import com.harmber2.suadat.innertube.utils.completed
import com.harmber2.suadat.utils.reportException
import javax.inject.Inject

@HiltViewModel
class BrowseViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val browseId: String? = savedStateHandle.get<String>("browseId")

        val items = MutableStateFlow<List<YTItem>?>(emptyList())
        val title = MutableStateFlow<String?>("")

        init {
            viewModelScope.launch {
                browseId?.let {
                    YouTube
                        .browse(browseId, null)
                        .onSuccess { result ->
                            // Store the title
                            title.value = result.title

                            // Flatten the nested structure to get all YTItems
                            val allItems = result.items.flatMap { it.items }
                            items.value = allItems
                        }.onFailure {
                            reportException(it)
                        }
                }
            }
        }
    }
