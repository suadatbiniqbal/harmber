/*
 * harmber (2026)
 * © Rukamori — github.com/suadatbiniqbal
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.harmber2.suadat.spotify

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.harmber2.suadat.db.MusicDatabase
import com.harmber2.suadat.db.entities.PlaylistEntity
import com.harmber2.suadat.db.entities.PlaylistSongMap
import com.harmber2.suadat.spotify.models.SpotifyPlaylist
import com.harmber2.suadat.spotify.models.SpotifyPlaylistTrack
import com.harmber2.suadat.spotify.models.SpotifyTrack
import com.harmber2.suadat.utils.reportException
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class SpotifyPlaylistViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val repository: SpotifyLibraryRepository,
        private val database: MusicDatabase,
    ) : ViewModel() {
        private val playlistId: String = savedStateHandle.get<String>("playlistId").orEmpty()

        private val _uiState = MutableStateFlow(SpotifyPlaylistUiState(isLoading = true))
        val uiState: StateFlow<SpotifyPlaylistUiState> = _uiState.asStateFlow()

        init {
            reload()
        }

        fun reload() {
            if (playlistId.isBlank()) {
                _uiState.value = SpotifyPlaylistUiState(errorMessage = "Missing Spotify playlist")
                return
            }
            viewModelScope.launch(Dispatchers.IO) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                try {
                    val playlist = repository.playlist(playlistId)
                    val tracks = repository.playlistTracks(playlistId)
                    _uiState.value =
                        SpotifyPlaylistUiState(
                            playlist = playlist,
                            tracks = tracks,
                            isLoading = false,
                        )
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Throwable) {
                    reportException(error)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message,
                        )
                    }
                }
            }
        }

        fun importPlaylist(customName: String) {
            val currentPlaylist = _uiState.value.playlist ?: return
            val tracksToImport = _uiState.value.tracks
            if (tracksToImport.isEmpty()) return

            viewModelScope.launch(Dispatchers.IO) {
                _uiState.update { it.copy(importState = ImportState.Loading(0, tracksToImport.size)) }
                try {
                    val localPlaylistId = PlaylistEntity.generatePlaylistId()
                    val thumbnailUrl = SpotifyMapper.getPlaylistThumbnail(currentPlaylist)
                    val playlistEntity = PlaylistEntity(
                        id = localPlaylistId,
                        name = customName,
                        thumbnailUrl = thumbnailUrl,
                        bookmarkedAt = LocalDateTime.now(),
                        isEditable = true,
                    )
                    database.withTransaction {
                        insert(playlistEntity)
                    }

                    var resolvedCount = 0
                    tracksToImport.forEachIndexed { index, trackWrapper ->
                        val spotifyTrack = trackWrapper.track ?: return@forEachIndexed
                        try {
                            val resolvedMetadata = SpotifyPlaybackResolver.resolveToMetadata(spotifyTrack)
                            if (resolvedMetadata != null) {
                                database.withTransaction {
                                    insert(resolvedMetadata)
                                    insert(
                                        PlaylistSongMap(
                                            playlistId = localPlaylistId,
                                            songId = resolvedMetadata.id,
                                            position = resolvedCount,
                                        )
                                    )
                                }
                                resolvedCount++
                            }
                        } catch (e: Exception) {
                            reportException(e)
                        }
                        _uiState.update {
                            it.copy(
                                importState = ImportState.Loading(
                                    progress = index + 1,
                                    total = tracksToImport.size
                                )
                            )
                        }
                    }

                    _uiState.update { it.copy(importState = ImportState.Success) }
                } catch (e: Throwable) {
                    reportException(e)
                    _uiState.update { it.copy(importState = ImportState.Error(e.message ?: "Failed to import")) }
                }
            }
        }

        fun removeTrack(track: SpotifyPlaylistTrack) {
            val playlistId = _uiState.value.playlist?.id ?: return
            val uid = track.uid ?: return
            val uri = track.track?.uri ?: return

            viewModelScope.launch(Dispatchers.IO) {
                try {
                    repository.removeTracksFromPlaylist(
                        playlistId = playlistId,
                        items = listOf(Spotify.PlaylistItemRef(uri = uri, uid = uid))
                    )
                    reload()
                } catch (e: Exception) {
                    reportException(e)
                }
            }
        }

        fun editPlaylist(newName: String) {
            val playlistId = _uiState.value.playlist?.id ?: return
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    repository.editPlaylist(playlistId, newName)
                    reload()
                } catch (e: Exception) {
                    reportException(e)
                }
            }
        }

        fun clearImportState() {
            _uiState.update { it.copy(importState = null) }
        }
    }

sealed interface ImportState {
    object Idle : ImportState
    data class Loading(val progress: Int, val total: Int) : ImportState
    object Success : ImportState
    data class Error(val message: String) : ImportState
}

@Immutable
data class SpotifyPlaylistUiState(
    val playlist: SpotifyPlaylist? = null,
    val tracks: List<SpotifyPlaylistTrack> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val importState: ImportState? = null,
)
