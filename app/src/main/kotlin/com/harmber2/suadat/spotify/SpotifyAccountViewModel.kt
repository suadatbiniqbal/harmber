/*
 * harmber (2026)
 * © Rukamori — github.com/suadatbiniqbal
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.harmber2.suadat.spotify

import androidx.compose.runtime.Immutable
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
import com.harmber2.suadat.utils.reportException
import javax.inject.Inject

@HiltViewModel
class SpotifyAccountViewModel
    @Inject
    constructor(
        private val repository: SpotifyLibraryRepository,
        private val database: com.harmber2.suadat.db.MusicDatabase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(SpotifyAccountUiState(isLoading = true))
        val uiState: StateFlow<SpotifyAccountUiState> = _uiState.asStateFlow()

        init {
            restoreSession()
        }

        fun restoreSession() {
            viewModelScope.launch(Dispatchers.IO) {
                runCatching { repository.restoreSession() }
                    .onSuccess { session ->
                        _uiState.update {
                            it.copy(
                                isAuthenticated = session.isAuthenticated,
                                accountName = session.accountName,
                                accountAvatarUrl = session.accountAvatarUrl,
                                isLoading = false,
                            )
                        }
                        if (session.isAuthenticated) reloadPlaylists()
                    }.onFailure { error ->
                        if (error is CancellationException) throw error
                        reportException(error)
                        _uiState.update {
                            it.copy(
                                isAuthenticated = false,
                                isLoading = false,
                                errorMessage = error.message,
                            )
                        }
                    }
            }
        }

        fun connectWithCookies(
            spDc: String,
            spKey: String,
        ) {
            if (spDc.isBlank()) return
            viewModelScope.launch(Dispatchers.IO) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                runCatching { repository.connectWithCookies(spDc = spDc, spKey = spKey) }
                    .onSuccess { session ->
                        _uiState.update {
                            it.copy(
                                isAuthenticated = true,
                                accountName = session.accountName,
                                accountAvatarUrl = session.accountAvatarUrl,
                                isLoading = false,
                            )
                        }
                        reloadPlaylists()
                    }.onFailure { error ->
                        if (error is CancellationException) throw error
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

        fun reloadPlaylists() {
            if (_uiState.value.isLoading) return
            viewModelScope.launch(Dispatchers.IO) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                val playlists = repository.refreshPlaylists()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        playlistCount = playlists.size,
                        errorMessage = repository.errorMessage.value,
                    )
                }
            }
        }

        fun logout() {
            if (_uiState.value.isLoading) return
            viewModelScope.launch(Dispatchers.IO) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                runCatching { repository.logout() }
                    .onSuccess {
                        _uiState.value = SpotifyAccountUiState()
                    }.onFailure { error ->
                        if (error is CancellationException) throw error
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

        fun importAllPlaylists() {
            viewModelScope.launch(Dispatchers.IO) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                try {
                    val playlists = repository.refreshPlaylists()
                    if (playlists.isEmpty()) {
                        _uiState.update { it.copy(isLoading = false, importAllState = ImportAllState.Success) }
                        return@launch
                    }

                    _uiState.update { it.copy(isLoading = false, importAllState = ImportAllState.Loading(0, playlists.size, "")) }

                    playlists.forEachIndexed { pIndex, playlist ->
                        _uiState.update { 
                            if (it.importAllState is ImportAllState.Loading) {
                                it.copy(importAllState = it.importAllState.copy(progress = pIndex, currentPlaylist = playlist.name))
                            } else it
                        }

                        val tracks = repository.playlistTracks(playlist.id)
                        if (tracks.isNotEmpty()) {
                            val localPlaylistId = com.harmber2.suadat.db.entities.PlaylistEntity.generatePlaylistId()
                            val playlistEntity = com.harmber2.suadat.db.entities.PlaylistEntity(
                                id = localPlaylistId,
                                name = playlist.name,
                                thumbnailUrl = SpotifyMapper.getPlaylistThumbnail(playlist),
                                bookmarkedAt = java.time.LocalDateTime.now(),
                                isEditable = true,
                            )
                            database.withTransaction {
                                insert(playlistEntity)
                            }

                            var resolvedCount = 0
                            tracks.forEach { trackWrapper ->
                                val spotifyTrack = trackWrapper.track ?: return@forEach
                                try {
                                    val resolvedMetadata = SpotifyPlaybackResolver.resolveToMetadata(spotifyTrack)
                                    if (resolvedMetadata != null) {
                                        database.withTransaction {
                                            insert(resolvedMetadata)
                                            insert(
                                                com.harmber2.suadat.db.entities.PlaylistSongMap(
                                                    playlistId = localPlaylistId,
                                                    songId = resolvedMetadata.id,
                                                    position = resolvedCount,
                                                )
                                            )
                                        }
                                        resolvedCount++
                                    }
                                } catch (_: Exception) {}
                            }
                        }
                        // Add delay to prevent rate limiting
                        kotlinx.coroutines.delay(1000)
                    }

                    _uiState.update { it.copy(importAllState = ImportAllState.Success) }
                } catch (e: Exception) {
                    reportException(e)
                    _uiState.update { it.copy(importAllState = ImportAllState.Error(e.message ?: "Failed to import all")) }
                }
            }
        }

        fun clearImportAllState() {
            _uiState.update { it.copy(importAllState = null) }
        }

        fun dismissError() {
            _uiState.update { it.copy(errorMessage = null) }
        }
    }

@Immutable
data class SpotifyAccountUiState(
    val isAuthenticated: Boolean = false,
    val accountName: String = "",
    val accountAvatarUrl: String? = null,
    val playlistCount: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val importAllState: ImportAllState? = null,
)

sealed interface ImportAllState {
    data class Loading(val progress: Int, val total: Int, val currentPlaylist: String) : ImportAllState
    object Success : ImportAllState
    data class Error(val message: String) : ImportAllState
}
