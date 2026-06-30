/*
 * harmber (2026)
 * © Rukamori — github.com/suadatbiniqbal
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.harmber2.suadat.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlinx.coroutines.launch
import com.harmber2.suadat.LocalPlayerAwareWindowInsets
import com.harmber2.suadat.LocalPlayerConnection
import com.harmber2.suadat.R
import com.harmber2.suadat.constants.DisableBlurKey
import com.harmber2.suadat.constants.InnerTubeCookieKey
import com.harmber2.suadat.constants.QuickPicksDisplayMode
import com.harmber2.suadat.constants.QuickPicksDisplayModeKey
import com.harmber2.suadat.constants.ShowHomeCategoryChipsKey
import com.harmber2.suadat.innertube.utils.hasYouTubeLoginCookie
import com.harmber2.suadat.ui.component.ChipsRow
import com.harmber2.suadat.ui.component.ExpressivePullToRefreshBox
import com.harmber2.suadat.ui.component.LocalBottomSheetPageState
import com.harmber2.suadat.ui.component.LocalMenuState
import com.harmber2.suadat.ui.component.NavigationTitle
import com.harmber2.suadat.ui.utils.SnapLayoutInfoProvider
import com.harmber2.suadat.utils.rememberEnumPreference
import com.harmber2.suadat.utils.rememberPreference
import com.harmber2.suadat.viewmodels.HomeViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val menuState = LocalMenuState.current
    val bottomSheetPageState = LocalBottomSheetPageState.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val haptic = LocalHapticFeedback.current

    val isPlaying by playerConnection.isPlaying.collectAsStateWithLifecycle()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsStateWithLifecycle()

    val quickPicks by viewModel.quickPicks.collectAsStateWithLifecycle()
    val speedDialItems by viewModel.speedDialItems.collectAsStateWithLifecycle()
    val forgottenFavorites by viewModel.forgottenFavorites.collectAsStateWithLifecycle()
    val keepListening by viewModel.keepListening.collectAsStateWithLifecycle()
    val bannerAds by viewModel.bannerAds.collectAsStateWithLifecycle()
    val albumRecommendations by viewModel.albumRecommendations.collectAsStateWithLifecycle()
    val mostPlayedArtists by viewModel.mostPlayedArtists.collectAsStateWithLifecycle()
    val mostPlayedAlbums by viewModel.mostPlayedAlbums.collectAsStateWithLifecycle()
    val randomAlbums by viewModel.randomAlbums.collectAsStateWithLifecycle()
    val homePage by viewModel.homePage.collectAsStateWithLifecycle()

    val selectedChip by viewModel.selectedChip.collectAsStateWithLifecycle()

    val isLoading: Boolean by viewModel.isLoading.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    val forgottenFavoritesLazyGridState = rememberLazyGridState()

    val accountName by viewModel.accountName.collectAsStateWithLifecycle()
    val accountImageUrl by viewModel.accountImageUrl.collectAsStateWithLifecycle()
    val innerTubeCookie by rememberPreference(InnerTubeCookieKey, "")
    val (disableBlur) = rememberPreference(DisableBlurKey, true)
    val (showHomeCategoryChips) = rememberPreference(ShowHomeCategoryChipsKey, true)
    val (quickPicksDisplayMode) = rememberEnumPreference(QuickPicksDisplayModeKey, QuickPicksDisplayMode.LIST)
    val (spotifyRecommendationsEnabled) = rememberPreference(com.harmber2.suadat.constants.SpotifyRecommendationsEnabledKey, false)
    val isLoggedIn =
        remember(innerTubeCookie) {
            hasYouTubeLoginCookie(innerTubeCookie)
        }
    val url = if (isLoggedIn) accountImageUrl else null

    val scope = rememberCoroutineScope()
    
    val infiniteTransition = rememberInfiniteTransition(label = "mesh")
    val meshOffset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )
    val meshOffset2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    val lazylistState = rememberLazyListState()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val scrollToTop =
        backStackEntry?.savedStateHandle?.getStateFlow("scrollToTop", false)?.collectAsStateWithLifecycle()

    LaunchedEffect(scrollToTop?.value) {
        if (scrollToTop?.value == true) {
            lazylistState.animateScrollToItem(0)
            backStackEntry?.savedStateHandle?.set("scrollToTop", false)
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow {
            lazylistState.layoutInfo.visibleItemsInfo
                .lastOrNull()
                ?.index
        }.collect { lastVisibleIndex ->
            val len = lazylistState.layoutInfo.totalItemsCount
            if (lastVisibleIndex != null && lastVisibleIndex >= len - 3) {
                viewModel.loadMoreYouTubeItems(homePage?.continuation)
            }
        }
    }

    if (selectedChip != null) {
        BackHandler {
            // if a chip is selected, go back to the normal homepage first
            viewModel.toggleChip(selectedChip)
        }
    }

    LaunchedEffect(showHomeCategoryChips, selectedChip) {
        if (!showHomeCategoryChips && selectedChip != null) {
            viewModel.toggleChip(selectedChip)
        }
    }

    LaunchedEffect(forgottenFavorites) {
        forgottenFavoritesLazyGridState.scrollToItem(0)
    }

    LaunchedEffect(mediaMetadata, spotifyRecommendationsEnabled) {
        if (spotifyRecommendationsEnabled) {
            viewModel.loadSpotifyRecommendations(
                mediaId = mediaMetadata?.id,
                title = mediaMetadata?.title,
                artist = mediaMetadata?.artists?.firstOrNull()?.name,
                duration = mediaMetadata?.duration
            )
        }
    }

    // Capture M3 Expressive colors from theme outside drawBehind
    val color1 = MaterialTheme.colorScheme.primary
    val color2 = MaterialTheme.colorScheme.secondary
    val color3 = MaterialTheme.colorScheme.tertiary
    val color4 = MaterialTheme.colorScheme.primaryContainer
    val color5 = MaterialTheme.colorScheme.secondaryContainer
    val surfaceColor = MaterialTheme.colorScheme.surface

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        // M3E Mesh gradient background layer at the top
        if (!disableBlur) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .fillMaxSize(0.7f) // Cover top 70% of screen
                        .align(Alignment.TopCenter)
                        .zIndex(-1f) // Place behind all content
                        .drawWithCache {
                            val width = this.size.width
                            val height = this.size.height

                            // Create mesh gradient with 5 color blobs for more variation
                            // First color blob - top left
                            val brush1 =
                                Brush.radialGradient(
                                    colors =
                                        listOf(
                                            color1.copy(alpha = 0.38f),
                                            color1.copy(alpha = 0.24f),
                                            color1.copy(alpha = 0.14f),
                                            color1.copy(alpha = 0.06f),
                                            Color.Transparent,
                                        ),
                                    center = Offset(width * (0.1f + 0.1f * meshOffset1), height * (0.05f + 0.1f * meshOffset2)),
                                    radius = width * 0.55f,
                                )

                            // Second color blob - top right
                            val brush2 =
                                Brush.radialGradient(
                                    colors =
                                        listOf(
                                            color2.copy(alpha = 0.34f),
                                            color2.copy(alpha = 0.2f),
                                            color2.copy(alpha = 0.11f),
                                            color2.copy(alpha = 0.05f),
                                            Color.Transparent,
                                        ),
                                    center = Offset(width * (0.9f - 0.1f * meshOffset2), height * (0.15f + 0.1f * meshOffset1)),
                                    radius = width * 0.65f,
                                )

                            // Third color blob - middle left
                            val brush3 =
                                Brush.radialGradient(
                                    colors =
                                        listOf(
                                            color3.copy(alpha = 0.3f),
                                            color3.copy(alpha = 0.17f),
                                            color3.copy(alpha = 0.09f),
                                            color3.copy(alpha = 0.04f),
                                            Color.Transparent,
                                        ),
                                    center = Offset(width * (0.25f + 0.1f * meshOffset2), height * (0.4f + 0.1f * meshOffset1)),
                                    radius = width * 0.6f,
                                )

                            // Fourth color blob - middle right
                            val brush4 =
                                Brush.radialGradient(
                                    colors =
                                        listOf(
                                            color4.copy(alpha = 0.26f),
                                            color4.copy(alpha = 0.14f),
                                            color4.copy(alpha = 0.08f),
                                            color4.copy(alpha = 0.03f),
                                            Color.Transparent,
                                        ),
                                    center = Offset(width * 0.7f, height * 0.5f),
                                    radius = width * 0.7f,
                                )

                            // Fifth color blob - bottom center (helps with smooth fade)
                            val brush5 =
                                Brush.radialGradient(
                                    colors =
                                        listOf(
                                            color5.copy(alpha = 0.22f),
                                            color5.copy(alpha = 0.12f),
                                            color5.copy(alpha = 0.06f),
                                            color5.copy(alpha = 0.02f),
                                            Color.Transparent,
                                        ),
                                    center = Offset(width * 0.5f, height * 0.75f),
                                    radius = width * 0.8f,
                                )

                            // Add a final vertical gradient overlay to ensure smooth bottom fade
                            val overlayBrush =
                                Brush.verticalGradient(
                                    colors =
                                        listOf(
                                            Color.Transparent,
                                            Color.Transparent,
                                            surfaceColor.copy(alpha = 0.22f),
                                            surfaceColor.copy(alpha = 0.55f),
                                            surfaceColor,
                                        ),
                                    startY = height * 0.4f,
                                    endY = height,
                                )

                            onDrawBehind {
                                drawRect(brush = brush1)
                                drawRect(brush = brush2)
                                drawRect(brush = brush3)
                                drawRect(brush = brush4)
                                drawRect(brush = brush5)
                                drawRect(brush = overlayBrush)
                            }
                        },
            ) {}
        }

        ExpressivePullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.fillMaxSize(),
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val horizontalLazyGridItemWidthFactor = if (maxWidth * 0.475f >= 320.dp) 0.475f else 0.9f
                val horizontalLazyGridItemWidth = maxWidth * horizontalLazyGridItemWidthFactor
                val forgottenFavoritesSnapLayoutInfoProvider =
                    remember(forgottenFavoritesLazyGridState) {
                        SnapLayoutInfoProvider(
                            lazyGridState = forgottenFavoritesLazyGridState,
                            positionInLayout = { layoutSize, itemSize ->
                                (layoutSize * horizontalLazyGridItemWidthFactor / 2f - itemSize / 2f)
                            },
                        )
                    }

                LazyColumn(
                    state = lazylistState,
                    contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues(),
                ) {
                    if (showHomeCategoryChips) {
                        item {
                            ChipsRow(
                                chips = homePage?.chips.orEmpty().map { it to it.title },
                                currentValue = selectedChip,
                                onValueUpdate = {
                                    viewModel.toggleChip(it)
                                },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }

                    // 1. Most Played Artists
                    if (mostPlayedArtists.isNotEmpty()) {
                        item {
                            NavigationTitle(
                                title = stringResource(R.string.your_artists),
                                modifier = Modifier.animateItem(),
                            )
                        }

                        item {
                            MostPlayedArtistsSection(
                                artists = mostPlayedArtists,
                                navController = navController,
                                modifier = Modifier.animateItem(),
                            )
                            Spacer(Modifier.height(16.dp))
                        }
                    }

                    // Advertisement / Banner Ads Section
                    if (bannerAds.isNotEmpty()) {
                        item(key = "banner_ads", contentType = "advertisement") {
                            BannerAdSection(
                                ads = bannerAds,
                                modifier = Modifier.animateItem()
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }

                    // 2. Quick Picks
                    quickPicks?.takeIf { it.isNotEmpty() }?.let { picks ->
                        item {
                            NavigationTitle(
                                title = stringResource(R.string.quick_picks),
                                modifier = Modifier.animateItem()
                            )
                        }

                        item(
                            key = "home_quick_picks_small",
                            contentType = "quick_picks",
                        ) {
                            MusicRecommendationsSection(
                                songs = picks,
                                playerConnection = playerConnection,
                                modifier = Modifier.animateItem(),
                            )
                        }
                    }

                    // 3. Spotify
                    spotifyPlaylistsContainer(
                        viewModel = viewModel,
                        navController = navController,
                        haptic = haptic,
                        scope = scope,
                    )

                    SpotifyRecommendationsContainer(
                        viewModel = viewModel,
                        mediaMetadata = mediaMetadata,
                        isPlaying = isPlaying,
                        navController = navController,
                        playerConnection = playerConnection,
                        menuState = menuState,
                        haptic = haptic,
                        scope = scope,
                    )

                    // 4. Wide Albums
                    if (albumRecommendations.isNotEmpty()) {
                        item {
                            NavigationTitle(
                                title = stringResource(R.string.albums),
                                modifier = Modifier.animateItem(),
                            )
                        }

                        item {
                            AlbumRecommendationsSection(
                                albums = albumRecommendations,
                                navController = navController,
                                modifier = Modifier.animateItem(),
                            )
                        }
                    }

                    // 5. Random Albums
                    if (randomAlbums.isNotEmpty()) {
                        item {
                            NavigationTitle(
                                title = stringResource(R.string.suggested_albums),
                                modifier = Modifier.animateItem(),
                            )
                        }

                        item {
                            RandomAlbumsSection(
                                albums = randomAlbums,
                                navController = navController,
                                modifier = Modifier.animateItem(),
                            )
                        }
                    }

                    // 6. Your Albums
                    if (mostPlayedAlbums.isNotEmpty()) {
                        item {
                            NavigationTitle(
                                title = stringResource(R.string.your_albums),
                                modifier = Modifier.animateItem(),
                            )
                        }

                        item {
                            MostPlayedAlbumsSection(
                                albums = mostPlayedAlbums,
                                navController = navController,
                                modifier = Modifier.animateItem(),
                            )
                        }
                    }

                    // 7. Keep Listening
                    keepListening?.takeIf { it.isNotEmpty() }?.let { items ->
                        item {
                            NavigationTitle(
                                title = stringResource(R.string.keep_listening),
                                modifier = Modifier.animateItem(),
                            )
                        }

                        item {
                            KeepListeningSection(
                                keepListening = items,
                                mediaMetadata = mediaMetadata,
                                isPlaying = isPlaying,
                                navController = navController,
                                playerConnection = playerConnection,
                                menuState = menuState,
                                haptic = haptic,
                                scope = scope,
                            )
                        }
                    }

                    SimilarRecommendationsContainer(
                        viewModel = viewModel,
                        mediaMetadata = mediaMetadata,
                        isPlaying = isPlaying,
                        navController = navController,
                        playerConnection = playerConnection,
                        menuState = menuState,
                        haptic = haptic,
                        scope = scope,
                    )

                    speedDialItems.takeIf { it.isNotEmpty() }?.let { items ->
                        item {
                            NavigationTitle(
                                title = stringResource(R.string.speed_dial),
                                modifier = Modifier.animateItem(),
                            )
                        }

                        item {
                            SpeedDialSection(
                                speedDialItems = items,
                                mediaMetadata = mediaMetadata,
                                isPlaying = isPlaying,
                                navController = navController,
                                playerConnection = playerConnection,
                                menuState = menuState,
                                haptic = haptic,
                                scope = scope,
                            )
                        }
                    }

                    AccountPlaylistsContainer(
                        viewModel = viewModel,
                        accountName = accountName,
                        accountImageUrl = accountImageUrl,
                        mediaMetadata = mediaMetadata,
                        isPlaying = isPlaying,
                        navController = navController,
                        playerConnection = playerConnection,
                        menuState = menuState,
                        haptic = haptic,
                        scope = scope,
                    )

                    forgottenFavorites?.takeIf { it.isNotEmpty() }?.let { favorites ->
                        item {
                            NavigationTitle(
                                title = stringResource(R.string.forgotten_favorites),
                                modifier = Modifier.animateItem(),
                            )
                        }

                        item {
                            ForgottenFavoritesSection(
                                forgottenFavorites = favorites,
                                mediaMetadata = mediaMetadata,
                                isPlaying = isPlaying,
                                horizontalLazyGridItemWidth = horizontalLazyGridItemWidth,
                                lazyGridState = forgottenFavoritesLazyGridState,
                                snapLayoutInfoProvider = forgottenFavoritesSnapLayoutInfoProvider,
                                navController = navController,
                                playerConnection = playerConnection,
                                menuState = menuState,
                                haptic = haptic,
                            )
                        }
                    }

                    homePage?.sections?.forEach { section ->
                        item {
                            HomePageSectionTitle(
                                section = section,
                                navController = navController,
                                modifier = Modifier.animateItem(),
                            )
                        }

                        item {
                            HomePageSectionContent(
                                section = section,
                                mediaMetadata = mediaMetadata,
                                isPlaying = isPlaying,
                                navController = navController,
                                playerConnection = playerConnection,
                                menuState = menuState,
                                haptic = haptic,
                                scope = scope,
                            )
                        }
                    }

                    if (isLoading || homePage?.continuation != null && homePage?.sections?.isNotEmpty() == true) {
                        item {
                            HomeLoadingShimmer(modifier = Modifier.animateItem())
                        }
                    }
                }
            }
        }
    }
}
