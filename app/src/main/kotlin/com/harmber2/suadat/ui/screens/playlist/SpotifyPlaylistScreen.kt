/*
 * harmber (2026)
 * © Rukamori — github.com/suadatbiniqbal
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.harmber2.suadat.ui.screens.playlist

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.palette.graphics.Palette
import coil3.compose.AsyncImage
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.harmber2.suadat.LocalPlayerAwareWindowInsets
import com.harmber2.suadat.LocalPlayerConnection
import com.harmber2.suadat.R
import com.harmber2.suadat.constants.AppBarHeight
import com.harmber2.suadat.constants.DisableBlurKey
import com.harmber2.suadat.extensions.togglePlayPause
import com.harmber2.suadat.models.MediaMetadata
import com.harmber2.suadat.spotify.SpotifyMapper
import com.harmber2.suadat.spotify.ImportState
import com.harmber2.suadat.spotify.SpotifyPlaybackResolver
import com.harmber2.suadat.spotify.SpotifyPlaylistQueue
import com.harmber2.suadat.spotify.SpotifyPlaylistViewModel
import com.harmber2.suadat.spotify.models.SpotifyPlaylistTrack
import com.harmber2.suadat.spotify.models.SpotifyTrack
import com.harmber2.suadat.ui.component.DraggableScrollbar
import com.harmber2.suadat.ui.component.EmptyPlaceholder
import com.harmber2.suadat.ui.component.ExpressivePullToRefreshBox
import com.harmber2.suadat.ui.component.IconButton
import com.harmber2.suadat.ui.component.SpotifyTrackListItem
import com.harmber2.suadat.ui.theme.PlayerColorExtractor
import com.harmber2.suadat.ui.utils.backToMain
import com.harmber2.suadat.ui.utils.resize
import com.harmber2.suadat.utils.makeTimeString
import com.harmber2.suadat.utils.rememberPreference
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotifyPlaylistScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
    viewModel: SpotifyPlaylistViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val playerConnection = LocalPlayerConnection.current
    val coroutineScope = rememberCoroutineScope()
    val isPlaying by playerConnection?.isPlaying?.collectAsStateWithLifecycle()
        ?: remember { mutableStateOf(false) }
    val mediaMetadata by playerConnection?.mediaMetadata?.collectAsStateWithLifecycle()
        ?: remember { mutableStateOf<MediaMetadata?>(null) }
    val playlist = state.playlist
    val tracks = state.tracks
    val lazyListState = rememberLazyListState()
    val systemBarsTopPadding = WindowInsets.systemBars.asPaddingValues().calculateTopPadding()
    val snackbarHostState = remember { SnackbarHostState() }

    val showTopBarTitle by remember {
        derivedStateOf { lazyListState.firstVisibleItemIndex > 0 }
    }

    var isSearching by rememberSaveable { mutableStateOf(false) }
    var resolvingTrackId by remember { mutableStateOf<String?>(null) }
    var query by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue()) }
    val focusRequester = remember { FocusRequester() }

    // Import flow state
    var showImportDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var importPlaylistName by remember { mutableStateOf("") }
    var editPlaylistName by remember { mutableStateOf("") }

    val filteredTracks =
        remember(tracks, query.text) {
            if (query.text.isBlank()) {
                tracks
            } else {
                tracks.filter { wrapper ->
                    val track = wrapper.track ?: return@filter false
                    track.name.contains(query.text, ignoreCase = true) ||
                        track.artists.any { artist -> artist.name.contains(query.text, ignoreCase = true) } ||
                        track.album?.name?.contains(query.text, ignoreCase = true) == true
                }
            }
        }

    val loadedDurationMs =
        remember(tracks) {
            tracks.sumOf { wrapper -> wrapper.track?.durationMs?.toLong() ?: 0L }
        }

    val (disableBlur) = rememberPreference(DisableBlurKey, true)
    var gradientColors by remember { mutableStateOf<List<Color>>(emptyList()) }
    val fallbackColor = MaterialTheme.colorScheme.surface.toArgb()
    val surfaceColor = MaterialTheme.colorScheme.surface

    val thumbnailUrl =
        remember(playlist) {
            playlist?.let { SpotifyMapper.getPlaylistThumbnail(it)?.resize(544, 544) }
        }

    LaunchedEffect(thumbnailUrl) {
        if (thumbnailUrl != null) {
            val request =
                ImageRequest
                    .Builder(context)
                    .data(thumbnailUrl)
                    .size(PlayerColorExtractor.Config.IMAGE_SIZE, PlayerColorExtractor.Config.IMAGE_SIZE)
                    .allowHardware(false)
                    .build()

            val result =
                runCatching {
                    context.imageLoader.execute(request)
                }.getOrNull()

            if (result != null) {
                val bitmap = result.image?.toBitmap()
                if (bitmap != null) {
                    val palette =
                        withContext(Dispatchers.Default) {
                            Palette
                                .from(bitmap)
                                .maximumColorCount(PlayerColorExtractor.Config.MAX_COLOR_COUNT)
                                .resizeBitmapArea(PlayerColorExtractor.Config.BITMAP_AREA)
                                .generate()
                        }

                    gradientColors =
                        PlayerColorExtractor.extractGradientColors(
                            palette = palette,
                            fallbackColor = fallbackColor,
                        )
                }
            }
        } else {
            gradientColors = emptyList()
        }
    }

    val gradientAlpha by remember {
        derivedStateOf {
            if (lazyListState.firstVisibleItemIndex == 0) {
                val offset = lazyListState.firstVisibleItemScrollOffset
                (1f - (offset / 600f)).coerceIn(0f, 1f)
            } else {
                0f
            }
        }
    }

    val transparentAppBar by remember {
        derivedStateOf { !disableBlur && !showTopBarTitle && !isSearching }
    }

    val topAppBarColors =
        if (transparentAppBar) {
            TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent,
                navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                titleContentColor = MaterialTheme.colorScheme.onBackground,
                actionIconContentColor = MaterialTheme.colorScheme.onBackground,
            )
        } else {
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                scrolledContainerColor = MaterialTheme.colorScheme.surface,
            )
        }

    LaunchedEffect(isSearching) {
        if (isSearching) focusRequester.requestFocus()
    }

    if (isSearching) {
        BackHandler {
            isSearching = false
            query = TextFieldValue()
        }
    }

    fun playPlaylist(
        startIndex: Int = 0,
        shuffled: Boolean = false,
    ) {
        val currentPlaylist = playlist ?: return
        val rawTracks = tracks.mapNotNull { it.track }
        val queueTracks = if (shuffled) rawTracks.shuffled() else rawTracks
        if (queueTracks.isEmpty()) return
        val boundedStartIndex = startIndex.coerceIn(queueTracks.indices)
        val preloadTrack = queueTracks[boundedStartIndex]
        if (resolvingTrackId != null) return

        coroutineScope.launch {
            resolvingTrackId = preloadTrack.id
            try {
                val preloadItem = SpotifyPlaybackResolver.resolveToMetadata(preloadTrack)
                playerConnection?.playQueue(
                    SpotifyPlaylistQueue(
                        playlistId = currentPlaylist.id,
                        title = currentPlaylist.name,
                        initialTracks = queueTracks,
                        startIndex = boundedStartIndex,
                        preloadItem = preloadItem,
                    ),
                )
            } finally {
                resolvingTrackId = null
            }
        }
    }

    // Handle import state side-effects
    val importSuccessText = stringResource(R.string.spotify_import_success)
    LaunchedEffect(state.importState) {
        when (val s = state.importState) {
            is ImportState.Success -> {
                snackbarHostState.showSnackbar(importSuccessText)
                viewModel.clearImportState()
            }
            is ImportState.Error -> {
                snackbarHostState.showSnackbar(s.message)
                viewModel.clearImportState()
            }
            else -> {}
        }
    }

    // Import name dialog
    if (showImportDialog) {
        LaunchedEffect(playlist) {
            importPlaylistName = playlist?.name ?: ""
        }
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text(stringResource(R.string.spotify_import_playlist)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.spotify_import_playlist_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OutlinedTextField(
                        value = importPlaylistName,
                        onValueChange = { importPlaylistName = it },
                        label = { Text(stringResource(R.string.spotify_import_name_hint)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showImportDialog = false
                        viewModel.importPlaylist(importPlaylistName)
                    },
                    enabled = importPlaylistName.isNotBlank(),
                ) {
                    Text(stringResource(R.string.spotify_import_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text(stringResource(R.string.cancel_button))
                }
            },
        )
    }

    // Edit name dialog
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text(stringResource(R.string.edit_playlist)) },
            text = {
                OutlinedTextField(
                    value = editPlaylistName,
                    onValueChange = { editPlaylistName = it },
                    label = { Text(stringResource(R.string.playlist_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showEditDialog = false
                        viewModel.editPlaylist(editPlaylistName)
                    },
                    enabled = editPlaylistName.isNotBlank(),
                ) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text(stringResource(R.string.cancel_button))
                }
            },
        )
    }

    ExpressivePullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = viewModel::reload,
        modifier =
            Modifier
                .fillMaxSize()
                .background(surfaceColor),
    ) {
        if (!disableBlur && gradientColors.isNotEmpty() && gradientAlpha > 0f) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .fillMaxSize(0.55f)
                        .align(Alignment.TopCenter)
                        .zIndex(-1f)
                        .drawBehind {
                            val width = size.width
                            val height = size.height

                            if (gradientColors.size >= 3) {
                                val c0 = gradientColors[0]
                                val c1 = gradientColors[1]
                                val c2 = gradientColors[2]
                                val c3 = gradientColors.getOrElse(3) { c0 }
                                val c4 = gradientColors.getOrElse(4) { c1 }
                                drawRect(
                                    brush =
                                        Brush.radialGradient(
                                            colors =
                                                listOf(
                                                    c0.copy(alpha = gradientAlpha * 0.75f),
                                                    c0.copy(alpha = gradientAlpha * 0.4f),
                                                    Color.Transparent,
                                                ),
                                            center = Offset(width * 0.5f, height * 0.15f),
                                            radius = width * 0.8f,
                                        ),
                                )
                                drawRect(
                                    brush =
                                        Brush.radialGradient(
                                            colors =
                                                listOf(
                                                    c1.copy(alpha = gradientAlpha * 0.55f),
                                                    c1.copy(alpha = gradientAlpha * 0.3f),
                                                    Color.Transparent,
                                                ),
                                            center = Offset(width * 0.1f, height * 0.4f),
                                            radius = width * 0.6f,
                                        ),
                                )
                                drawRect(
                                    brush =
                                        Brush.radialGradient(
                                            colors =
                                                listOf(
                                                    c2.copy(alpha = gradientAlpha * 0.5f),
                                                    c2.copy(alpha = gradientAlpha * 0.25f),
                                                    Color.Transparent,
                                                ),
                                            center = Offset(width * 0.9f, height * 0.35f),
                                            radius = width * 0.55f,
                                        ),
                                )
                                drawRect(
                                    brush =
                                        Brush.radialGradient(
                                            colors =
                                                listOf(
                                                    c3.copy(alpha = gradientAlpha * 0.35f),
                                                    c3.copy(alpha = gradientAlpha * 0.18f),
                                                    Color.Transparent,
                                                ),
                                            center = Offset(width * 0.25f, height * 0.65f),
                                            radius = width * 0.75f,
                                        ),
                                )
                                drawRect(
                                    brush =
                                        Brush.radialGradient(
                                            colors =
                                                listOf(
                                                    c4.copy(alpha = gradientAlpha * 0.3f),
                                                    c4.copy(alpha = gradientAlpha * 0.15f),
                                                    Color.Transparent,
                                                ),
                                            center = Offset(width * 0.55f, height * 0.85f),
                                            radius = width * 0.9f,
                                        ),
                                )
                            } else if (gradientColors.isNotEmpty()) {
                                drawRect(
                                    brush =
                                        Brush.radialGradient(
                                            colors =
                                                listOf(
                                                    gradientColors[0].copy(alpha = gradientAlpha * 0.7f),
                                                    gradientColors[0].copy(alpha = gradientAlpha * 0.35f),
                                                    Color.Transparent,
                                                ),
                                            center = Offset(width * 0.5f, height * 0.25f),
                                            radius = width * 0.85f,
                                        ),
                                )
                            }
                            drawRect(
                                brush =
                                    Brush.verticalGradient(
                                        colors =
                                            listOf(
                                                Color.Transparent,
                                                Color.Transparent,
                                                surfaceColor.copy(alpha = gradientAlpha * 0.22f),
                                                surfaceColor.copy(alpha = gradientAlpha * 0.55f),
                                                surfaceColor,
                                            ),
                                        startY = height * 0.4f,
                                        endY = height,
                                    ),
                            )
                        },
            )
        }

        LazyColumn(
            state = lazyListState,
            contentPadding = LocalPlayerAwareWindowInsets.current.union(WindowInsets.ime).asPaddingValues(),
            modifier = Modifier.fillMaxSize(),
        ) {
            if (!isSearching) {
                playlist?.let { currentPlaylist ->
                    item(key = "header") {
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(top = systemBarsTopPadding + AppBarHeight),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Box(
                                modifier =
                                    Modifier
                                        .padding(top = 8.dp, bottom = 20.dp),
                            ) {
                                Surface(
                                    modifier =
                                        Modifier
                                            .size(240.dp)
                                            .shadow(
                                                elevation = 24.dp,
                                                shape = RoundedCornerShape(16.dp),
                                                spotColor =
                                                    gradientColors.getOrNull(0)?.copy(alpha = 0.5f)
                                                        ?: MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                            ),
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                ) {
                                    if (thumbnailUrl != null) {
                                        AsyncImage(
                                            model = thumbnailUrl,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize(),
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.queue_music),
                                                contentDescription = null,
                                                modifier = Modifier.size(80.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }
                                }
                            }

                            Text(
                                text = currentPlaylist.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 32.dp),
                            )

                            currentPlaylist.owner?.displayName?.takeIf(String::isNotBlank)?.let { owner ->
                                Text(
                                    text = owner,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier =
                                        Modifier
                                            .padding(top = 8.dp)
                                            .padding(horizontal = 32.dp),
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 48.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                val trackCount = currentPlaylist.tracks?.total ?: tracks.size
                                MetadataChip(
                                    icon = R.drawable.music_note,
                                    text = pluralStringResource(R.plurals.n_song, trackCount, trackCount),
                                )

                                if (loadedDurationMs > 0L) {
                                    MetadataChip(
                                        icon = R.drawable.timer,
                                        text = makeTimeString(loadedDurationMs),
                                    )
                                }
                            }

                            currentPlaylist.description?.takeIf(String::isNotBlank)?.let { description ->
                                Text(
                                    text = description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier =
                                        Modifier
                                            .padding(top = 16.dp)
                                            .padding(horizontal = 32.dp),
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                ToggleButton(
                                    checked = false,
                                    onCheckedChange = { viewModel.reload() },
                                    modifier = Modifier.size(48.dp),
                                    shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(),
                                    colors =
                                        ToggleButtonDefaults.toggleButtonColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            checkedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                            checkedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        ),
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.sync),
                                        contentDescription = stringResource(R.string.spotify_reload_playlist),
                                        modifier = Modifier.size(24.dp),
                                    )
                                }

                                ToggleButton(
                                    checked = false,
                                    onCheckedChange = { playPlaylist() },
                                    enabled = tracks.isNotEmpty(),
                                    modifier =
                                        Modifier
                                            .weight(1f)
                                            .height(48.dp),
                                    shapes = ButtonGroupDefaults.connectedMiddleButtonShapes(),
                                    colors =
                                        ToggleButtonDefaults.toggleButtonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary,
                                            checkedContainerColor = MaterialTheme.colorScheme.primary,
                                            checkedContentColor = MaterialTheme.colorScheme.onPrimary,
                                        ),
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.play),
                                        contentDescription = stringResource(R.string.play),
                                        modifier = Modifier.size(24.dp),
                                    )
                                }

                                ToggleButton(
                                    checked = false,
                                    onCheckedChange = { playPlaylist(shuffled = true) },
                                    enabled = tracks.isNotEmpty(),
                                    modifier =
                                        Modifier
                                            .weight(1f)
                                            .height(48.dp),
                                    shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
                                    colors =
                                        ToggleButtonDefaults.toggleButtonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary,
                                            checkedContainerColor = MaterialTheme.colorScheme.primary,
                                            checkedContentColor = MaterialTheme.colorScheme.onPrimary,
                                        ),
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.shuffle),
                                        contentDescription = stringResource(R.string.shuffle),
                                        modifier = Modifier.size(24.dp),
                                    )
                                }
                            }

                            val isImporting = state.importState is ImportState.Loading
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Button(
                                    onClick = { isSearching = true },
                                    enabled = !isImporting,
                                    modifier =
                                        Modifier
                                            .weight(1f)
                                            .height(48.dp),
                                    shapes = ButtonDefaults.shapes(),
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.search),
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                    )
                                    Spacer(modifier = Modifier.size(6.dp))
                                    Text(
                                        text = stringResource(R.string.search),
                                        maxLines = 1,
                                    )
                                }

                                FilledTonalButton(
                                    onClick = {
                                        if (!isImporting) {
                                            importPlaylistName = playlist?.name ?: ""
                                            showImportDialog = true
                                        }
                                    },
                                    enabled = tracks.isNotEmpty() && !isImporting,
                                    modifier =
                                        Modifier
                                            .weight(1f)
                                            .height(48.dp),
                                    shapes = ButtonDefaults.shapes(),
                                ) {
                                    if (isImporting) {
                                        val loadingState = state.importState as ImportState.Loading
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp,
                                        )
                                        Spacer(modifier = Modifier.size(6.dp))
                                        Text(
                                            text = stringResource(
                                                R.string.spotify_importing,
                                                loadingState.progress,
                                                loadingState.total,
                                            ),
                                            maxLines = 1,
                                        )
                                    } else {
                                        Icon(
                                            painter = painterResource(R.drawable.playlist_import),
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                        )
                                        Spacer(modifier = Modifier.size(6.dp))
                                        Text(
                                            text = stringResource(R.string.spotify_import_playlist),
                                            maxLines = 1,
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }

            if (state.isLoading && tracks.isEmpty()) {
                item(key = "loading") {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularWavyProgressIndicator()
                    }
                }
            }

            state.errorMessage?.let { error ->
                item(key = "error") {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    )
                }
            }

            if (!state.isLoading && state.errorMessage == null && filteredTracks.isEmpty()) {
                item(key = "empty") {
                    EmptyPlaceholder(
                        icon = R.drawable.music_note,
                        text =
                            stringResource(
                                if (query.text.isBlank()) {
                                    R.string.spotify_no_tracks
                                } else {
                                    R.string.ai_model_no_results
                                },
                            ),
                    )
                }
            }

            itemsIndexed(
                items = filteredTracks,
                key = { index, track -> "spotify_track_${track.uid ?: track.track?.id}_$index" },
                contentType = { _, _ -> "spotify_track" },
            ) { index, trackWrapper ->
                val track = trackWrapper.track ?: return@itemsIndexed
                val trackIsActive =
                    remember(track, mediaMetadata) {
                        track.isResolvedAs(mediaMetadata)
                    }
                val trackIsResolving = resolvingTrackId == track.id

                SpotifyTrackListItem(
                    track = track,
                    isActive = trackIsActive || trackIsResolving,
                    isPlaying = isPlaying && !trackIsResolving,
                    trailingContent = {
                        if (trackIsResolving) {
                            CircularWavyProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            IconButton(
                                onClick = { viewModel.removeTrack(trackWrapper) },
                                onLongClick = {}
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.delete),
                                    contentDescription = stringResource(R.string.delete),
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                )
                            }
                        }
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable(enabled = resolvingTrackId == null || trackIsActive) {
                                if (trackIsActive) {
                                    playerConnection?.player?.togglePlayPause()
                                } else {
                                    val startIndex =
                                        tracks
                                            .indexOfFirst { item -> item.track?.id == track.id }
                                            .takeIf { itemIndex -> itemIndex >= 0 }
                                            ?: index
                                    playPlaylist(startIndex = startIndex)
                                }
                            },
                )
            }
        }

        DraggableScrollbar(
            modifier =
                Modifier
                    .padding(
                        LocalPlayerAwareWindowInsets.current.union(WindowInsets.ime).asPaddingValues(),
                    ).align(Alignment.CenterEnd),
            scrollState = lazyListState,
            headerItems = if (!isSearching && playlist != null) 1 else 0,
        )

        TopAppBar(
            colors = topAppBarColors,
            title = {
                if (isSearching) {
                    TextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = {
                            Text(
                                text = stringResource(R.string.search),
                                style = MaterialTheme.typography.titleLarge,
                            )
                        },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.titleLarge,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        colors =
                            TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                            ),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                    )
                } else if (showTopBarTitle) {
                    Text(
                        text = playlist?.name ?: stringResource(R.string.spotify_playlists),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            },
            navigationIcon = {
                IconButton(
                    onClick = {
                        if (isSearching) {
                            isSearching = false
                            query = TextFieldValue()
                        } else {
                            navController.navigateUp()
                        }
                    },
                    onLongClick = {
                        if (!isSearching) navController.backToMain()
                    },
                ) {
                    Icon(
                        painter =
                            painterResource(
                                if (isSearching) R.drawable.close else R.drawable.arrow_back,
                            ),
                        contentDescription = null,
                    )
                }
            },
            actions = {
                if (!isSearching) {
                    var showMenu by remember { mutableStateOf(false) }
                    IconButton(
                        onClick = { isSearching = true },
                        onLongClick = {},
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.search),
                            contentDescription = null,
                        )
                    }
                    
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            onLongClick = {}
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.more_vert),
                                contentDescription = null
                            )
                        }
                        
                        androidx.compose.material3.DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text(stringResource(R.string.spotify_refresh)) },
                                onClick = { 
                                    showMenu = false
                                    viewModel.reload() 
                                },
                                leadingIcon = { Icon(painterResource(R.drawable.sync), null) }
                            )
                            
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text(stringResource(R.string.edit)) },
                                onClick = { 
                                    showMenu = false
                                    editPlaylistName = playlist?.name ?: ""
                                    showEditDialog = true 
                                },
                                leadingIcon = { Icon(painterResource(R.drawable.edit), null) }
                            )

                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text(stringResource(R.string.spotify_add_songs)) },
                                onClick = {
                                    showMenu = false
                                    navController.navigate("search") // Go to search, from there they can add
                                },
                                leadingIcon = { Icon(painterResource(R.drawable.add), null) }
                            )

                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text(stringResource(R.string.spotify_import_playlist)) },
                                onClick = { 
                                    showMenu = false
                                    showImportDialog = true 
                                },
                                leadingIcon = { Icon(painterResource(R.drawable.playlist_import), null) }
                            )
                        }
                    }
                }
            },
            scrollBehavior = scrollBehavior,
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(LocalPlayerAwareWindowInsets.current.asPaddingValues()),
        )
    }
}

private fun SpotifyTrack.isResolvedAs(mediaMetadata: MediaMetadata?): Boolean {
    if (mediaMetadata == null) return false

    mediaMetadata.spotifyTrackId?.let { spotifyTrackId ->
        return id.isNotBlank() && spotifyTrackId == id
    }

    val titleMatches = name.equals(mediaMetadata.title, ignoreCase = true)
    val durationMatches =
        durationMs <= 0 ||
            mediaMetadata.duration <= 0 ||
            abs(durationMs.toLong() - mediaMetadata.duration * 1000L) <= 1_000L
    val albumMatches =
        album?.let { spotifyAlbum ->
            val currentAlbum = mediaMetadata.album ?: return false
            spotifyAlbum.id.isNotBlank() && spotifyAlbum.id == currentAlbum.id ||
                spotifyAlbum.name.equals(currentAlbum.title, ignoreCase = true)
        } ?: true
    val artistMatches =
        artists.isEmpty() ||
            mediaMetadata.artists.isEmpty() ||
            artists.any { spotifyArtist ->
                mediaMetadata.artists.any { artist ->
                    spotifyArtist.name.equals(artist.name, ignoreCase = true)
                }
            }
    val thumbnailMatches =
        SpotifyMapper.getTrackThumbnail(this)?.let { thumbnail ->
            thumbnail == mediaMetadata.thumbnailUrl
        } ?: true

    return titleMatches && durationMatches && albumMatches && artistMatches && thumbnailMatches
}

@Composable
private fun MetadataChip(
    icon: Int,
    text: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}
