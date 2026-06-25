/*
 * harmber (2026)
 * © Rukamori — github.com/suadatbiniqbal
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package com.harmber2.suadat.viewmodels

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.exoplayer.offline.Download
import com.google.common.collect.ImmutableList
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.harmber2.suadat.R
import com.harmber2.suadat.constants.AiApiKeyKey
import com.harmber2.suadat.constants.AiApiValidationStatus
import com.harmber2.suadat.constants.AiApiValidationStatusKey
import com.harmber2.suadat.constants.AiCustomEndpointKey
import com.harmber2.suadat.constants.AiProvider
import com.harmber2.suadat.constants.AiProviderKey
import com.harmber2.suadat.constants.AlbumFilter
import com.harmber2.suadat.constants.AlbumFilterKey
import com.harmber2.suadat.constants.AlbumSortDescendingKey
import com.harmber2.suadat.constants.AlbumSortType
import com.harmber2.suadat.constants.AlbumSortTypeKey
import com.harmber2.suadat.constants.ArtistFilter
import com.harmber2.suadat.constants.ArtistFilterKey
import com.harmber2.suadat.constants.ArtistSongSortDescendingKey
import com.harmber2.suadat.constants.ArtistSongSortType
import com.harmber2.suadat.constants.ArtistSongSortTypeKey
import com.harmber2.suadat.constants.ArtistSortDescendingKey
import com.harmber2.suadat.constants.ArtistSortType
import com.harmber2.suadat.constants.ArtistSortTypeKey
import com.harmber2.suadat.constants.HideExplicitKey
import com.harmber2.suadat.constants.HideVideoKey
import com.harmber2.suadat.constants.LibraryFilter
import com.harmber2.suadat.constants.PlaylistSortDescendingKey
import com.harmber2.suadat.constants.PlaylistSortType
import com.harmber2.suadat.constants.PlaylistSortTypeKey
import com.harmber2.suadat.constants.SongFilter
import com.harmber2.suadat.constants.SongFilterKey
import com.harmber2.suadat.constants.SongSortDescendingKey
import com.harmber2.suadat.constants.SongSortType
import com.harmber2.suadat.constants.SongSortTypeKey
import com.harmber2.suadat.constants.TopSize
import com.harmber2.suadat.db.MusicDatabase
import com.harmber2.suadat.db.entities.Playlist
import com.harmber2.suadat.db.entities.PlaylistEntity
import com.harmber2.suadat.db.entities.Song
import com.harmber2.suadat.extensions.filterExplicit
import com.harmber2.suadat.extensions.filterExplicitAlbums
import com.harmber2.suadat.extensions.reversed
import com.harmber2.suadat.extensions.toEnum
import com.harmber2.suadat.innertube.YouTube
import com.harmber2.suadat.library.LibraryTopMix
import com.harmber2.suadat.library.ObserveLibraryTopMixesUseCase
import com.harmber2.suadat.library.RefreshLibraryTopMixesResult
import com.harmber2.suadat.library.RefreshLibraryTopMixesUseCase
import com.harmber2.suadat.library.TopMixGenerationFailure
import com.harmber2.suadat.models.MediaMetadata
import com.harmber2.suadat.models.toMediaMetadata
import com.harmber2.suadat.playback.DownloadUtil
import com.harmber2.suadat.spotify.SpotifyLibraryRepository
import com.harmber2.suadat.spotify.models.SpotifyPlaylist
import com.harmber2.suadat.utils.SyncUtils
import com.harmber2.suadat.utils.dataStore
import com.harmber2.suadat.utils.get
import com.harmber2.suadat.utils.reportException
import java.text.Collator
import java.time.Duration
import java.time.LocalDateTime
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class LibrarySongsViewModel
    @Inject
    constructor(
        @ApplicationContext context: Context,
        database: MusicDatabase,
        downloadUtil: DownloadUtil,
        private val syncUtils: SyncUtils,
    ) : ViewModel() {
        private val _isRefreshing = MutableStateFlow(false)
        val isRefreshing = _isRefreshing.asStateFlow()

        val allSongs =
            context.dataStore.data
                .map {
                    Triple(
                        Triple(
                            it[SongFilterKey].toEnum(SongFilter.LIKED),
                            it[SongSortTypeKey].toEnum(SongSortType.CREATE_DATE),
                            (it[SongSortDescendingKey] ?: true),
                        ),
                        it[HideExplicitKey] ?: false,
                        it[HideVideoKey] ?: false,
                    )
                }.distinctUntilChanged()
                .flatMapLatest { (filterSort, hideExplicit, hideVideo) ->
                    val (filter, sortType, descending) = filterSort
                    when (filter) {
                        SongFilter.LIBRARY -> {
                            database.songs(sortType, descending, hideVideo).map { it.filterExplicit(hideExplicit) }
                        }

                        SongFilter.LIKED -> {
                            database.likedSongs(sortType, descending, hideVideo).map { it.filterExplicit(hideExplicit) }
                        }

                        SongFilter.DOWNLOADED -> {
                            downloadUtil.downloads.flatMapLatest { downloads ->
                                database
                                    .allSongs()
                                    .flowOn(Dispatchers.IO)
                                    .map { songs ->
                                        songs.filter { song: Song ->
                                            downloads[song.id]?.state == Download.STATE_COMPLETED
                                        }
                                    }.map { songs ->
                                        when (sortType) {
                                            SongSortType.CREATE_DATE -> {
                                                songs.sortedBy { song: Song ->
                                                    downloads[song.id]?.updateTimeMs ?: 0L
                                                }
                                            }

                                            SongSortType.NAME -> {
                                                songs.sortedBy { song: Song -> song.song.title }
                                            }

                                            SongSortType.ARTIST -> {
                                                val collator =
                                                    Collator.getInstance(Locale.getDefault())
                                                collator.strength = Collator.PRIMARY
                                                songs.sortedWith(
                                                    compareBy<Song, String>(collator) { song ->
                                                        song.artists.joinToString("") { artist -> artist.name }
                                                    },
                                                )
                                            }

                                            SongSortType.PLAY_TIME -> {
                                                songs.sortedBy { song: Song -> song.song.totalPlayTime }
                                            }
                                        }.reversed(descending).filterExplicit(hideExplicit)
                                    }
                            }
                        }
                    }
                }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        fun refresh(filter: SongFilter) {
            if (_isRefreshing.value) return
            viewModelScope.launch(Dispatchers.IO) {
                _isRefreshing.value = true
                try {
                    when (filter) {
                        SongFilter.LIKED -> syncUtils.syncLikedSongs()
                        SongFilter.LIBRARY -> syncUtils.syncLibrarySongs()
                        SongFilter.DOWNLOADED -> Unit
                    }
                } catch (e: Exception) {
                    reportException(e)
                } finally {
                    _isRefreshing.value = false
                }
            }
        }

        fun syncLikedSongs() {
            refresh(SongFilter.LIKED)
        }

        fun syncLibrarySongs() {
            refresh(SongFilter.LIBRARY)
        }
    }

@HiltViewModel
class LibraryArtistsViewModel
    @Inject
    constructor(
        @ApplicationContext context: Context,
        database: MusicDatabase,
        private val syncUtils: SyncUtils,
    ) : ViewModel() {
        private val _isRefreshing = MutableStateFlow(false)
        val isRefreshing = _isRefreshing.asStateFlow()

        val allArtists =
            context.dataStore.data
                .map {
                    Triple(
                        it[ArtistFilterKey].toEnum(ArtistFilter.LIKED),
                        it[ArtistSortTypeKey].toEnum(ArtistSortType.CREATE_DATE),
                        it[ArtistSortDescendingKey] ?: true,
                    )
                }.distinctUntilChanged()
                .flatMapLatest { (filter, sortType, descending) ->
                    when (filter) {
                        ArtistFilter.LIBRARY -> database.artists(sortType, descending)
                        ArtistFilter.LIKED -> database.artistsBookmarked(sortType, descending)
                    }
                }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        fun refresh(filter: ArtistFilter) {
            if (filter != ArtistFilter.LIKED) return
            if (_isRefreshing.value) return
            viewModelScope.launch(Dispatchers.IO) {
                _isRefreshing.value = true
                try {
                    syncUtils.syncArtistsSubscriptions()
                } catch (e: Exception) {
                    reportException(e)
                } finally {
                    _isRefreshing.value = false
                }
            }
        }

        fun sync() {
            refresh(ArtistFilter.LIKED)
        }

        init {
            viewModelScope.launch(Dispatchers.IO) {
                allArtists.collectLatest { artists ->
                    artists
                        .map { it.artist }
                        .filter {
                            it.thumbnailUrl == null || Duration.between(
                                it.lastUpdateTime,
                                LocalDateTime.now(),
                            ) > Duration.ofDays(10)
                        }
                        .take(10) // Only update 10 at a time to prevent overload
                        .forEach { artist ->
                            YouTube.artist(artist.id).onSuccess { artistPage ->
                                database.query {
                                    update(artist, artistPage)
                                }
                            }
                        }
                }
            }
        }
    }

@HiltViewModel
class LibraryAlbumsViewModel
    @Inject
    constructor(
        @ApplicationContext context: Context,
        database: MusicDatabase,
        downloadUtil: DownloadUtil,
        private val syncUtils: SyncUtils,
    ) : ViewModel() {
        private val _isRefreshing = MutableStateFlow(false)
        val isRefreshing = _isRefreshing.asStateFlow()

        val allAlbums =
            context.dataStore.data
                .map {
                    Pair(
                        Triple(
                            it[AlbumFilterKey].toEnum(AlbumFilter.LIKED),
                            it[AlbumSortTypeKey].toEnum(AlbumSortType.CREATE_DATE),
                            it[AlbumSortDescendingKey] ?: true,
                        ),
                        it[HideExplicitKey] ?: false,
                    )
                }.distinctUntilChanged()
                .flatMapLatest { (filterSort, hideExplicit) ->
                    val (filter, sortType, descending) = filterSort
                    when (filter) {
                        AlbumFilter.DOWNLOADED -> {
                            downloadUtil.downloads.flatMapLatest { downloads ->
                                database
                                    .allSongs()
                                    .flowOn(Dispatchers.IO)
                                    .map { songs ->
                                        songs
                                            .filter { song -> downloads[song.id]?.state == Download.STATE_COMPLETED }
                                            .mapNotNull { it.song.albumId }
                                            .toSet()
                                    }.flatMapLatest { downloadedAlbumIds ->
                                        database
                                            .albumsByIds(downloadedAlbumIds, sortType, descending)
                                            .map { albums -> albums.filterExplicitAlbums(hideExplicit) }
                                    }
                            }
                        }

                        AlbumFilter.DOWNLOADED_FULL -> {
                            downloadUtil.downloads.flatMapLatest { downloads ->
                                database
                                    .allSongs()
                                    .flowOn(Dispatchers.IO)
                                    .map { songs ->
                                        songs
                                            .filter { song -> downloads[song.id]?.state == Download.STATE_COMPLETED }
                                            .mapNotNull { song -> song.song.albumId?.let { albumId -> albumId to song } }
                                            .groupBy({ it.first }, { it.second })
                                            .mapValues { (_, songList) -> songList.size }
                                    }.flatMapLatest { downloadedCountByAlbum ->
                                        database
                                            .albumsByIds(downloadedCountByAlbum.keys, sortType, descending)
                                            .map { albums ->
                                                albums
                                                    .filter { album ->
                                                        val totalSongsInAlbum = album.album.songCount
                                                        val downloadedSongsCount = downloadedCountByAlbum[album.album.id] ?: 0
                                                        totalSongsInAlbum > 0 && downloadedSongsCount >= totalSongsInAlbum
                                                    }.filterExplicitAlbums(hideExplicit)
                                            }
                                    }
                            }
                        }

                        AlbumFilter.LIBRARY -> {
                            database.albums(sortType, descending).map { it.filterExplicitAlbums(hideExplicit) }
                        }

                        AlbumFilter.LIKED -> {
                            database.albumsLiked(sortType, descending).map { it.filterExplicitAlbums(hideExplicit) }
                        }
                    }
                }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        fun refresh(filter: AlbumFilter) {
            if (filter != AlbumFilter.LIKED) return
            if (_isRefreshing.value) return
            viewModelScope.launch(Dispatchers.IO) {
                _isRefreshing.value = true
                try {
                    syncUtils.syncLikedAlbums()
                } catch (e: Exception) {
                    reportException(e)
                } finally {
                    _isRefreshing.value = false
                }
            }
        }

        fun sync() {
            refresh(AlbumFilter.LIKED)
        }

        init {
            viewModelScope.launch(Dispatchers.IO) {
                allAlbums.collect { albums ->
                    albums
                        .filter {
                            it.album.songCount == 0
                        }.forEach { album ->
                            YouTube
                                .album(album.id)
                                .onSuccess { albumPage ->
                                    database.query {
                                        update(album.album, albumPage, album.artists)
                                    }
                                }.onFailure {
                                    reportException(it)
                                    if (it.message?.contains("NOT_FOUND") == true) {
                                        database.query {
                                            delete(album.album)
                                        }
                                    }
                                }
                        }
                }
            }
        }
    }

@HiltViewModel
class LibraryPlaylistsViewModel
    @Inject
    constructor(
        @ApplicationContext context: Context,
        database: MusicDatabase,
        private val syncUtils: SyncUtils,
        private val spotifyRepository: SpotifyLibraryRepository,
    ) : ViewModel() {
        private val localPlaylists =
            context.dataStore.data
                .map {
                    it[PlaylistSortTypeKey].toEnum(PlaylistSortType.CUSTOM) to (
                        it[PlaylistSortDescendingKey]
                            ?: true
                    )
                }.distinctUntilChanged()
                .flatMapLatest { (sortType, descending) ->
                    database.playlists(sortType, descending)
                }

        val allPlaylists =
            combine(localPlaylists, spotifyRepository.playlists) { local, spotify ->
                local + spotify.map { it.toPlaylist() }
            }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        private val _isRefreshing = MutableStateFlow(false)
        val isRefreshing = _isRefreshing.asStateFlow()

        fun sync() {
            viewModelScope.launch(Dispatchers.IO) {
                _isRefreshing.value = true
                syncUtils.syncSavedPlaylists()
                syncUtils.syncAutoSyncPlaylists()
                spotifyRepository.refreshPlaylists()
                _isRefreshing.value = false
            }
        }

        private fun SpotifyPlaylist.toPlaylist(): Playlist {
            return Playlist(
                playlist = PlaylistEntity(
                    id = "SP_$id",
                    name = name,
                    thumbnailUrl = images.firstOrNull()?.url,
                    browseId = id, // store raw spotify id in browseId
                    isEditable = true // we implemented editing
                ),
                songCount = tracks?.total ?: 0,
                songThumbnails = emptyList<String?>()
            )
        }

        val topValue =
            context.dataStore.data
                .map { it[TopSize] ?: "50" }
                .distinctUntilChanged()
    }

@HiltViewModel
class ArtistSongsViewModel
    @Inject
    constructor(
        @ApplicationContext context: Context,
        database: MusicDatabase,
        savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val artistId = savedStateHandle.get<String>("artistId")!!
        val artist =
            database
                .artist(artistId)
                .stateIn(viewModelScope, SharingStarted.Lazily, null)

        val songs =
            context.dataStore.data
                .map {
                    Pair(
                        it[ArtistSongSortTypeKey].toEnum(ArtistSongSortType.CREATE_DATE) to (
                            it[ArtistSongSortDescendingKey]
                                ?: true
                        ),
                        it[HideExplicitKey] ?: false,
                    )
                }.distinctUntilChanged()
                .flatMapLatest { (sortDesc, hideExplicit) ->
                    val (sortType, descending) = sortDesc
                    database.artistSongs(artistId, sortType, descending).map { it.filterExplicit(hideExplicit) }
                }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    }

@HiltViewModel
class LibraryMixViewModel
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val database: MusicDatabase,
        private val syncUtils: SyncUtils,
        observeLibraryTopMixes: ObserveLibraryTopMixesUseCase,
        private val refreshLibraryTopMixes: RefreshLibraryTopMixesUseCase,
    ) : ViewModel() {
        private val _isRefreshing = MutableStateFlow(false)
        val isRefreshing = _isRefreshing.asStateFlow()
        private val _isTopMixRefreshing = MutableStateFlow(false)
        private val _topMixInitialError = MutableStateFlow<String?>(null)
        private val _topMixEvents = MutableSharedFlow<String>()
        val topMixEvents = _topMixEvents.asSharedFlow()
        private var hasRequestedInitialTopMixGeneration = false

        private val isTopMixAiAvailable =
            context.dataStore.data
                .map { prefs ->
                    val provider = prefs[AiProviderKey].toEnum(AiProvider.NONE)
                    provider != AiProvider.NONE &&
                        prefs[AiApiKeyKey].orEmpty().isNotBlank() &&
                        (provider != AiProvider.CUSTOM || prefs[AiCustomEndpointKey].orEmpty().isNotBlank()) &&
                        prefs[AiApiValidationStatusKey].toEnum(AiApiValidationStatus.UNKNOWN) != AiApiValidationStatus.FAILED
                }.distinctUntilChanged()
                .stateIn(viewModelScope, SharingStarted.Lazily, false)

        private val observedTopMixes =
            observeLibraryTopMixes()
                .map<List<LibraryTopMix>, List<LibraryTopMix>?> { it }
                .catch { throwable ->
                    if (throwable is CancellationException) throw throwable
                    reportException(throwable)
                    _topMixInitialError.value = context.getString(R.string.library_top_mixes_failed)
                    emit(emptyList())
                }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

        val topMixesUiState =
            combine(
                observedTopMixes,
                isTopMixAiAvailable,
                _isTopMixRefreshing,
                _topMixInitialError,
            ) { mixes, isAiAvailable, isRefreshing, initialError ->
                when {
                    mixes == null -> {
                        LibraryTopMixesUiState.Loading
                    }

                    initialError != null && mixes.isEmpty() -> {
                        LibraryTopMixesUiState.Error(initialError)
                    }

                    mixes.isNotEmpty() -> {
                        LibraryTopMixesUiState.Success(
                            mixes = ImmutableList.copyOf(mixes.map { it.toUiModel() }),
                            isRefreshing = isRefreshing,
                        )
                    }

                    !isAiAvailable -> {
                        LibraryTopMixesUiState.Empty(
                            reason = LibraryTopMixEmptyReason.AI_NOT_CONFIGURED,
                            isRefreshing = isRefreshing,
                        )
                    }

                    else -> {
                        LibraryTopMixesUiState.Empty(
                            reason = LibraryTopMixEmptyReason.NO_RECENT_HISTORY,
                            isRefreshing = isRefreshing,
                        )
                    }
                }
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryTopMixesUiState.Loading)

        init {
            viewModelScope.launch {
                combine(observedTopMixes, isTopMixAiAvailable) { mixes, isAiAvailable ->
                    mixes != null && mixes.isEmpty() && isAiAvailable
                }.distinctUntilChanged()
                    .collect { shouldGenerate ->
                        if (shouldGenerate && !hasRequestedInitialTopMixGeneration) {
                            hasRequestedInitialTopMixGeneration = true
                            refreshTopMixesInternal(isInitialGeneration = true)
                        }
                    }
            }
        }

        fun syncAllLibrary() {
            if (_isRefreshing.value) return
            _isRefreshing.value = true
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    syncUtils.performFullSync()
                } catch (e: Exception) {
                    timber.log.Timber.e(e, "Error during manual sync")
                    reportException(e)
                } finally {
                    _isRefreshing.value = false
                }
            }
        }

        fun refreshTopMixes() {
            hasRequestedInitialTopMixGeneration = true
            refreshTopMixesInternal(isInitialGeneration = false)
        }

        private fun refreshTopMixesInternal(isInitialGeneration: Boolean) {
            if (_isTopMixRefreshing.value) return
            viewModelScope.launch(Dispatchers.IO) {
                _isTopMixRefreshing.value = true
                _topMixInitialError.value = null
                val hasVisibleMixes = observedTopMixes.value.orEmpty().isNotEmpty()
                try {
                    when (val result = refreshLibraryTopMixes()) {
                        RefreshLibraryTopMixesResult.Success -> {
                            _topMixInitialError.value = null
                        }

                        is RefreshLibraryTopMixesResult.Failure -> {
                            result.cause?.let(::reportException)
                            val message = result.reason.toTopMixMessage(result.cause)
                            if (!isInitialGeneration && hasVisibleMixes) {
                                _topMixEvents.emit(message)
                            } else {
                                _topMixInitialError.value = message
                            }
                        }
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    reportException(e)
                    val message = TopMixGenerationFailure.AI_REQUEST_FAILED.toTopMixMessage(e)
                    if (!isInitialGeneration && hasVisibleMixes) {
                        _topMixEvents.emit(message)
                    } else {
                        _topMixInitialError.value = message
                    }
                } finally {
                    _isTopMixRefreshing.value = false
                }
            }
        }

        private fun TopMixGenerationFailure.toTopMixMessage(cause: Throwable?): String =
            when (this) {
                TopMixGenerationFailure.AI_NOT_CONFIGURED -> {
                    context.getString(R.string.library_top_mixes_ai_not_configured_desc)
                }

                TopMixGenerationFailure.NO_RECENT_HISTORY -> {
                    context.getString(R.string.library_top_mixes_no_recent_history)
                }

                TopMixGenerationFailure.NO_VALID_MIXES -> {
                    context.getString(R.string.library_top_mixes_no_valid_mixes)
                }

                TopMixGenerationFailure.AI_REQUEST_FAILED -> {
                    buildString {
                        append(context.getString(R.string.library_top_mixes_failed))
                        cause?.localizedMessage?.takeIf(String::isNotBlank)?.let { message ->
                            append(": ")
                            append(message)
                        }
                    }
                }
            }

        val topValue =
            context.dataStore.data
                .map { it[TopSize] ?: "50" }
                .distinctUntilChanged()
        var artists =
            database
                .artistsBookmarked(
                    ArtistSortType.CREATE_DATE,
                    true,
                ).stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        var albums =
            context.dataStore.data
                .map { it[HideExplicitKey] ?: false }
                .distinctUntilChanged()
                .flatMapLatest { hideExplicit ->
                    database.albumsLiked(AlbumSortType.CREATE_DATE, true).map { it.filterExplicitAlbums(hideExplicit) }
                }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        var playlists =
            context.dataStore.data
                .map {
                    it[PlaylistSortTypeKey].toEnum(PlaylistSortType.CUSTOM) to (it[PlaylistSortDescendingKey] ?: true)
                }.distinctUntilChanged()
                .flatMapLatest { (sortType, descending) -> database.playlists(sortType, descending) }
                .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        init {
            viewModelScope.launch(Dispatchers.IO) {
                albums.collect { albums ->
                    albums
                        .filter {
                            it.album.songCount == 0
                        }.forEach { album ->
                            YouTube
                                .album(album.id)
                                .onSuccess { albumPage ->
                                    database.query {
                                        update(album.album, albumPage, album.artists)
                                    }
                                }.onFailure {
                                    reportException(it)
                                    if (it.message?.contains("NOT_FOUND") == true) {
                                        database.query {
                                            delete(album.album)
                                        }
                                    }
                                }
                        }
                }
            }
            viewModelScope.launch(Dispatchers.IO) {
                artists.collect { artists ->
                    artists
                        .map { it.artist }
                        .filter {
                            it.thumbnailUrl == null ||
                                Duration.between(
                                    it.lastUpdateTime,
                                    LocalDateTime.now(),
                                ) > Duration.ofDays(10)
                        }.forEach { artist ->
                            YouTube.artist(artist.id).onSuccess { artistPage ->
                                database.query {
                                    update(artist, artistPage)
                                }
                            }
                        }
                }
            }
        }
    }

@Immutable
sealed interface LibraryTopMixesUiState {
    data object Loading : LibraryTopMixesUiState

    @Immutable
    data class Success(
        val mixes: ImmutableList<LibraryTopMixUiModel>,
        val isRefreshing: Boolean,
    ) : LibraryTopMixesUiState

    @Immutable
    data class Empty(
        val reason: LibraryTopMixEmptyReason,
        val isRefreshing: Boolean,
    ) : LibraryTopMixesUiState

    @Immutable
    data class Error(
        val message: String,
    ) : LibraryTopMixesUiState
}

enum class LibraryTopMixEmptyReason {
    AI_NOT_CONFIGURED,
    NO_RECENT_HISTORY,
}

@Immutable
data class LibraryTopMixUiModel(
    val id: String,
    val title: String,
    val description: String,
    val tracks: ImmutableList<MediaMetadata>,
)

private fun LibraryTopMix.toUiModel() =
    LibraryTopMixUiModel(
        id = id,
        title = title,
        description = description,
        tracks = ImmutableList.copyOf(tracks),
    )

@HiltViewModel
class LibraryViewModel
    @Inject
    constructor() : ViewModel() {
        private val curScreen = mutableStateOf(LibraryFilter.LIBRARY)
        val filter: MutableState<LibraryFilter> = curScreen
    }
