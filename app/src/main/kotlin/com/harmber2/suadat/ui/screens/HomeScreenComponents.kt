/*
 * harmber (2026)
 * © Rukamori — github.com/suadatbiniqbal
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.harmber2.suadat.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalCenteredHeroCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import kotlinx.coroutines.CoroutineScope
import com.harmber2.suadat.R
import com.harmber2.suadat.constants.GridThumbnailHeight
import com.harmber2.suadat.constants.ListItemHeight
import com.harmber2.suadat.constants.ListThumbnailSize
import com.harmber2.suadat.constants.QuickPicksDisplayMode
import com.harmber2.suadat.constants.ThumbnailCornerRadius
import com.harmber2.suadat.db.entities.Album
import com.harmber2.suadat.db.entities.Artist
import com.harmber2.suadat.db.entities.LocalItem
import com.harmber2.suadat.db.entities.Playlist
import com.harmber2.suadat.db.entities.Song
import com.harmber2.suadat.extensions.toMediaItem
import com.harmber2.suadat.extensions.togglePlayPause
import com.harmber2.suadat.innertube.models.AlbumItem
import com.harmber2.suadat.innertube.models.ArtistItem
import com.harmber2.suadat.innertube.models.PlaylistItem
import com.harmber2.suadat.innertube.models.SongItem
import com.harmber2.suadat.innertube.models.WatchEndpoint
import com.harmber2.suadat.innertube.models.YTItem
import com.harmber2.suadat.innertube.pages.HomePage
import com.harmber2.suadat.models.BannerAd
import com.harmber2.suadat.models.MediaMetadata
import com.harmber2.suadat.models.SimilarRecommendation
import com.harmber2.suadat.models.toMediaMetadata
import com.harmber2.suadat.playback.PlayerConnection
import com.harmber2.suadat.playback.queues.ListQueue
import com.harmber2.suadat.playback.queues.YouTubeQueue
import com.harmber2.suadat.spotify.SpotifyRecommendationsQueue
import com.harmber2.suadat.spotify.models.SpotifyTrack
import com.harmber2.suadat.ui.component.AlbumGridItem
import com.harmber2.suadat.ui.component.ArtistGridItem
import com.harmber2.suadat.ui.component.LocalMenuState
import com.harmber2.suadat.ui.component.MenuState
import com.harmber2.suadat.ui.component.NavigationTitle
import com.harmber2.suadat.ui.component.SongGridItem
import com.harmber2.suadat.ui.component.SongListItem
import com.harmber2.suadat.ui.component.SpeedDialGridItem
import com.harmber2.suadat.ui.component.YouTubeGridItem
import com.harmber2.suadat.ui.component.shimmer.GridItemPlaceHolder
import com.harmber2.suadat.ui.component.shimmer.ShimmerHost
import com.harmber2.suadat.ui.component.shimmer.TextPlaceholder
import com.harmber2.suadat.ui.menu.AlbumMenu
import com.harmber2.suadat.ui.menu.ArtistMenu
import com.harmber2.suadat.ui.menu.PlaylistMenu
import com.harmber2.suadat.ui.menu.SongMenu
import com.harmber2.suadat.ui.menu.YouTubeAlbumMenu
import com.harmber2.suadat.ui.menu.YouTubeArtistMenu
import com.harmber2.suadat.ui.menu.YouTubePlaylistMenu
import com.harmber2.suadat.ui.menu.YouTubeSongMenu
import com.harmber2.suadat.ui.utils.rememberArtworkCardColor
import com.harmber2.suadat.ui.utils.rememberArtworkGradient
import com.harmber2.suadat.utils.rememberPreference
import com.harmber2.suadat.viewmodels.HomeViewModel
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.absoluteValue
import kotlin.random.Random
import com.harmber2.suadat.ui.utils.SnapLayoutInfoProvider as buildSnapLayoutInfoProvider

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BannerAdSection(
    ads: List<BannerAd>,
    modifier: Modifier = Modifier,
) {
    val bannerAds = remember(ads) { ads.filter { it.adFormat == "banner" } }
    if (bannerAds.isEmpty()) return

    if (bannerAds.size == 1) {
        BannerAdCard(
            ad = bannerAds[0],
            modifier = modifier.padding(horizontal = 24.dp).padding(top = 16.dp, bottom = 8.dp)
        )
    } else {
        val pagerState = rememberPagerState(pageCount = { bannerAds.size })
        val adConfig by com.harmber2.suadat.models.AdManager.config.collectAsStateWithLifecycle()

        // Auto-swipe logic
        if (adConfig.autoSwipeEnabled) {
            LaunchedEffect(pagerState.currentPage, bannerAds.size) {
                while (true) {
                    kotlinx.coroutines.delay(adConfig.swipeIntervalMs)
                    val nextPage = (pagerState.currentPage + 1) % bannerAds.size
                    pagerState.animateScrollToPage(nextPage)
                }
            }
        }
        
        Column(modifier = modifier.fillMaxWidth().padding(top = 16.dp)) {
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 24.dp),
                pageSpacing = 16.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) { page ->
                val ad = bannerAds[page]
                BannerAdCard(
                    ad = ad,
                    modifier = Modifier.graphicsLayer {
                        val pageOffset = (
                            (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                        ).absoluteValue
                        
                        lerp(
                            start = 0.85f,
                            stop = 1f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        ).also { scale ->
                            scaleX = scale
                            scaleY = scale
                        }
                        alpha = lerp(
                            start = 0.5f,
                            stop = 1f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        )
                    }
                )
            }

            // Carousel Indicators
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(bannerAds.size) { iteration ->
                    val color = if (pagerState.currentPage == iteration) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    
                    val width by animateDpAsState(
                        targetValue = if (pagerState.currentPage == iteration) 24.dp else 8.dp,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label = "indicator_width"
                    )

                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(color)
                            .width(width)
                            .height(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BannerAdCard(
    ad: BannerAd,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var isMuted by remember { mutableStateOf(true) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clip(RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(Color.Black)
            ) {
                if (ad.mediaType == "video") {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = ad.mediaUrl, // Thumbnail
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().alpha(0.6f)
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.play),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                        
                        // Sound Toggle
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                                .clickable { isMuted = !isMuted },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = if (isMuted) R.drawable.volume_off else R.drawable.volume_up),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                } else {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(ad.mediaUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Advertisement Tag
                if (ad.showAdTag) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .padding(10.dp)
                            .align(Alignment.TopStart)
                    ) {
                        Text(
                            text = "Advertisement",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            ),
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }
            
            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (ad.title.isNotEmpty()) {
                        Text(
                            text = ad.title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (ad.subtitle.isNotEmpty()) {
                        Text(
                            text = ad.subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                Spacer(Modifier.width(8.dp))
                
                Button(
                    onClick = {
                        if (ad.actionUrl.isNotEmpty()) {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(ad.actionUrl))
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        }
                    },
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(
                        text = ad.buttonText.ifEmpty { "Listen" },
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
fun BannerAdOverlay(
    ads: List<BannerAd>,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val overlayAds = remember(ads) { ads.filter { it.adFormat == "overlay" } }
    if (overlayAds.isEmpty()) return

    val currentAd = remember(overlayAds) { overlayAds.first() }
    
    // Limits tracking
    val (lastShownDate, setLastShownDate) = rememberPreference(com.harmber2.suadat.constants.LastAdShownDateKey, "")
    val (showCount, setShowCount) = rememberPreference(com.harmber2.suadat.constants.OverlayAdShowCountKey, 0)
    
    val today = remember { java.time.LocalDate.now().toString() }
    val currentHour = remember { java.time.LocalTime.now().hour }
    
    var isDismissed by rememberSaveable { mutableStateOf(false) }
    
    val shouldShow = remember(lastShownDate, showCount, isDismissed, currentAd) {
        if (isDismissed) return@remember false
        
        // Time slot check
        if (currentHour < currentAd.startHour || currentHour > currentAd.endHour) return@remember false
        
        // Daily limit check
        if (lastShownDate == today) {
            showCount < currentAd.dailyLimit
        } else {
            true
        }
    }
    
    LaunchedEffect(shouldShow) {
        if (shouldShow) {
            if (lastShownDate != today) {
                setLastShownDate(today)
                setShowCount(1)
            } else {
                setShowCount(showCount + 1)
            }
        }
    }

    if (!shouldShow) return

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(28.dp))
            .background(Color.Black)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(currentAd.mediaUrl)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        
        // Close button
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .size(32.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable { isDismissed = true },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.close),
                contentDescription = "Close Ad",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
        
        // Advertisement tag
        Surface(
            color = Color.Black.copy(alpha = 0.5f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .padding(12.dp)
                .align(Alignment.TopStart)
        ) {
            Text(
                text = "Sponsored",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = Color.White,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        
        // Bottom Action Button
        Button(
            onClick = {
                if (currentAd.actionUrl.isNotEmpty()) {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentAd.actionUrl))
                        context.startActivity(intent)
                    } catch (_: Exception) {}
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth(0.8f),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(
                text = currentAd.buttonText.ifEmpty { "Learn More" },
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun AlbumRecommendationsSection(
    albums: List<Album>,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth(),
    ) {
        items(
            items = albums,
            key = { it.id },
        ) { item ->
            val album = item.album
            val interactionSource = remember { MutableInteractionSource() }
            val isHovered by interactionSource.collectIsHoveredAsState()
            val isPressed by interactionSource.collectIsPressedAsState()

            val animatedScale by animateFloatAsState(
                targetValue = if (isPressed) 0.94f else if (isHovered) 1.05f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium),
                label = "album_card_scale"
            )

            val animatedElevation by animateDpAsState(
                targetValue = if (isPressed) 2.dp else if (isHovered) 16.dp else 4.dp,
                animationSpec = tween(300),
                label = "album_card_elevation"
            )

            val animatedColor by animateColorAsState(
                targetValue = if (isHovered) MaterialTheme.colorScheme.surfaceContainerHighest
                else MaterialTheme.colorScheme.surfaceContainer,
                animationSpec = tween(400),
                label = "album_card_color"
            )

            Column(
                modifier = Modifier
                    .width(200.dp)
                    .graphicsLayer {
                        scaleX = animatedScale
                        scaleY = animatedScale
                    }
                    .shadow(animatedElevation, shape = RoundedCornerShape(28.dp))
                    .clip(RoundedCornerShape(28.dp))
                    .background(animatedColor)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        navController.navigate("album/${item.id}")
                    },
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(album.thumbnailUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.35f)),
                                    startY = 140f
                                )
                            )
                    )
                }
                
                Column(
                    modifier = Modifier
                        .padding(horizontal = 14.dp, vertical = 14.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = album.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = item.artists.joinToString(", ") { it.name },
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    )
                }
            }
        }
    }
}

@Composable
fun MostPlayedAlbumsSection(
    albums: List<Album>,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth(),
    ) {
        items(
            items = albums,
            key = { it.id },
        ) { item ->
            val album = item.album
            val interactionSource = remember { MutableInteractionSource() }
            val isHovered by interactionSource.collectIsHoveredAsState()
            val isPressed by interactionSource.collectIsPressedAsState()

            val animatedScale by animateFloatAsState(
                targetValue = if (isPressed) 0.94f else if (isHovered) 1.05f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium),
                label = "most_played_album_card_scale"
            )

            Column(
                modifier = Modifier
                    .width(110.dp)
                    .graphicsLayer {
                        scaleX = animatedScale
                        scaleY = animatedScale
                    }
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        navController.navigate("album/${item.id}")
                    },
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(RoundedCornerShape(28.dp)),
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(album.thumbnailUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = album.title,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = item.artists.joinToString(", ") { it.name },
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                )
            }
        }
    }
}

@Composable
fun MusicRecommendationsSection(
    songs: List<Song>,
    playerConnection: PlayerConnection,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        items(
            items = songs,
            key = { it.id },
        ) { song ->
            Column(
                modifier = Modifier
                    .width(110.dp)
                    .clickable {
                        playerConnection.playQueue(YouTubeQueue.radio(song.toMediaMetadata()))
                    },
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(RoundedCornerShape(28.dp)),
                ) {
                    AsyncImage(
                        model = song.song.thumbnailUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                    // Play Overlay button
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.play),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = song.song.title,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = song.artists.joinToString(", ") { it.name },
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                )
            }
        }
    }
}

@Composable
fun MostPlayedArtistsSection(
    artists: List<Artist>,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth(),
    ) {
        items(
            items = artists,
            key = { it.id },
        ) { item ->
            val artist = item.artist
            Column(
                modifier = Modifier
                    .width(72.dp)
                    .clickable {
                        navController.navigate("artist/${artist.id}")
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(artist.thumbnailUrl)
                        .crossfade(true)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.artist),
                    error = painterResource(R.drawable.artist),
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = artist.name,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
    }
}

@Composable
fun RandomAlbumsSection(
    albums: List<Album>,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth(),
    ) {
        items(
            items = albums,
            key = { it.id },
        ) { item ->
            val album = item.album
            val interactionSource = remember { MutableInteractionSource() }
            val isHovered by interactionSource.collectIsHoveredAsState()
            val isPressed by interactionSource.collectIsPressedAsState()

            val animatedScale by animateFloatAsState(
                targetValue = if (isPressed) 0.94f else if (isHovered) 1.05f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium),
                label = "random_album_card_scale"
            )

            Column(
                modifier = Modifier
                    .width(150.dp)
                    .graphicsLayer {
                        scaleX = animatedScale
                        scaleY = animatedScale
                    }
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        navController.navigate("album/${item.id}")
                    },
            ) {
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(RoundedCornerShape(32.dp)),
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(album.thumbnailUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = album.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = item.artists.joinToString(", ") { it.name },
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                )
            }
        }
    }
}

@Composable
fun KeepListeningSection(
    keepListening: List<LocalItem>,
    mediaMetadata: MediaMetadata?,
    isPlaying: Boolean,
    navController: NavController,
    playerConnection: PlayerConnection,
    menuState: MenuState,
    haptic: HapticFeedback,
    scope: CoroutineScope,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        items(
            items = keepListening,
            key = { it.id },
        ) { item ->
            when (item) {
                is Song -> {
                    val isActive = item.id == mediaMetadata?.id
                    SongGridItem(
                        song = item,
                        isActive = isActive,
                        isPlaying = isPlaying && isActive,
                        modifier = Modifier.combinedClickable(
                            onClick = {
                                if (isActive) {
                                    playerConnection.player.togglePlayPause()
                                } else {
                                    playerConnection.playQueue(ListQueue(items = listOf(item.toMediaItem())))
                                }
                            },
                            onLongClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                menuState.show {
                                    SongMenu(
                                        originalSong = item,
                                        navController = navController,
                                        onDismiss = menuState::dismiss,
                                    )
                                }
                            }
                        )
                    )
                }
                is Album -> {
                    AlbumGridItem(
                        album = item,
                        coroutineScope = scope,
                        modifier = Modifier.clickable {
                            navController.navigate("album/${item.id}")
                        }
                    )
                }
                is Artist -> {
                    ArtistGridItem(
                        artist = item,
                        modifier = Modifier.clickable {
                            navController.navigate("artist/${item.id}")
                        }
                    )
                }
                is Playlist -> {
                    YouTubeGridItem(
                        item = PlaylistItem(
                            id = item.id,
                            title = item.playlist.name,
                            author = null,
                            songCountText = null,
                            thumbnail = item.playlist.thumbnailUrl,
                            playEndpoint = null,
                            shuffleEndpoint = null,
                            radioEndpoint = null,
                            isEditable = true
                        ),
                        isActive = false,
                        isPlaying = false,
                        coroutineScope = scope,
                        modifier = Modifier.clickable {
                            navController.navigate("local_playlist/${item.id}")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SpeedDialSection(
    speedDialItems: List<LocalItem>,
    mediaMetadata: MediaMetadata?,
    isPlaying: Boolean,
    navController: NavController,
    playerConnection: PlayerConnection,
    menuState: MenuState,
    haptic: HapticFeedback,
    scope: CoroutineScope,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        items(
            items = speedDialItems,
            key = { it.id },
        ) { item ->
            val ytItem = remember(item) {
                when (item) {
                    is Song -> SongItem(id = item.id, title = item.song.title, artists = item.artists.map { com.harmber2.suadat.innertube.models.Artist(name = it.name, id = it.id) }, thumbnail = item.song.thumbnailUrl.orEmpty())
                    is Album -> AlbumItem(browseId = item.id, playlistId = "", id = item.id, title = item.album.title, artists = item.artists.map { com.harmber2.suadat.innertube.models.Artist(name = it.name, id = it.id) }, thumbnail = item.album.thumbnailUrl.orEmpty(), year = null)
                    is Artist -> ArtistItem(id = item.id, title = item.artist.name, thumbnail = item.artist.thumbnailUrl, shuffleEndpoint = null, radioEndpoint = null)
                    is Playlist -> PlaylistItem(id = item.id, title = item.playlist.name, author = null, songCountText = null, thumbnail = item.playlist.thumbnailUrl, playEndpoint = null, shuffleEndpoint = null, radioEndpoint = null)
                }
            }
            SpeedDialGridItem(
                item = ytItem,
                isPinned = true,
                modifier = Modifier.clickable {
                    when (item) {
                        is Song -> navController.navigate("album/${item.song.albumId}")
                        is Album -> navController.navigate("album/${item.id}")
                        is Artist -> navController.navigate("artist/${item.id}")
                        is Playlist -> navController.navigate("local_playlist/${item.id}")
                    }
                }
            )
        }
    }
}

@Composable
fun ForgottenFavoritesSection(
    forgottenFavorites: List<Song>,
    mediaMetadata: MediaMetadata?,
    isPlaying: Boolean,
    horizontalLazyGridItemWidth: Dp,
    lazyGridState: LazyGridState,
    snapLayoutInfoProvider: SnapLayoutInfoProvider,
    navController: NavController,
    playerConnection: PlayerConnection,
    menuState: MenuState,
    haptic: HapticFeedback,
    modifier: Modifier = Modifier,
) {
    LazyHorizontalGrid(
        state = lazyGridState,
        rows = GridCells.Fixed(1),
        flingBehavior = rememberSnapFlingBehavior(snapLayoutInfoProvider),
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxWidth().height(GridThumbnailHeight + 64.dp),
    ) {
        items(
            items = forgottenFavorites,
            key = { it.id },
        ) { song ->
            val isActive = song.id == mediaMetadata?.id
            SongGridItem(
                song = song,
                isActive = isActive,
                isPlaying = isPlaying && isActive,
                modifier = Modifier
                    .width(horizontalLazyGridItemWidth)
                    .combinedClickable(
                        onClick = {
                            if (isActive) {
                                playerConnection.player.togglePlayPause()
                            } else {
                                playerConnection.playQueue(YouTubeQueue.radio(song.toMediaMetadata()))
                            }
                        },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            menuState.show {
                                SongMenu(
                                    originalSong = song,
                                    navController = navController,
                                    onDismiss = menuState::dismiss,
                                )
                            }
                        }
                    )
            )
        }
    }
}

@Composable
fun HomePageSectionTitle(
    section: HomePage.Section,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    NavigationTitle(
        title = section.title ?: "",
        onClick = section.endpoint?.browseId?.let { browseId ->
            {
                if (browseId == "FEmusic_moods_and_genres") {
                    navController.navigate("moods_and_genres")
                } else {
                    navController.navigate("browse/$browseId")
                }
            }
        },
        modifier = modifier,
    )
}

@Composable
fun HomePageSectionContent(
    section: HomePage.Section,
    mediaMetadata: MediaMetadata?,
    isPlaying: Boolean,
    navController: NavController,
    playerConnection: PlayerConnection,
    menuState: MenuState,
    haptic: HapticFeedback,
    scope: CoroutineScope,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        items(
            items = section.items,
            key = { it.id },
        ) { item ->
            YouTubeGridItem(
                item = item,
                isActive = item.id == mediaMetadata?.id,
                isPlaying = isPlaying,
                coroutineScope = scope,
                modifier = Modifier.combinedClickable(
                    onClick = {
                        if (item is SongItem) {
                            playerConnection.playQueue(YouTubeQueue.radio(item.toMediaMetadata()))
                        } else if (item is AlbumItem) {
                            navController.navigate("album/${item.id}")
                        } else if (item is PlaylistItem) {
                            navController.navigate("online_playlist/${item.id}")
                        } else if (item is ArtistItem) {
                            navController.navigate("artist/${item.id}")
                        }
                    },
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        // Add menu if needed
                    }
                )
            )
        }
    }
}

@Composable
fun HomeLoadingShimmer(modifier: Modifier = Modifier) {
    ShimmerHost(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            repeat(3) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextPlaceholder(modifier = Modifier.padding(horizontal = 24.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        userScrollEnabled = false,
                    ) {
                        items(5) {
                            GridItemPlaceHolder()
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.spotifyPlaylistsContainer(
    viewModel: HomeViewModel,
    navController: NavController,
    haptic: HapticFeedback,
    scope: CoroutineScope,
) {
    item {
        val spotifyPlaylists by viewModel.spotifyPlaylists.collectAsStateWithLifecycle()
        
        Column {
            NavigationTitle(
                title = stringResource(R.string.spotify_playlists),
                onClick = { navController.navigate("settings/backup_restore") },
                modifier = Modifier.animateItem(),
            )
            
            if (spotifyPlaylists.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth().animateItem(),
                ) {
                    items(
                        items = spotifyPlaylists,
                        key = { it.id },
                    ) { item ->
                        SpotifyHomePlaylistCard(
                            playlist = item,
                            onClick = { navController.navigate("spotify_playlist/${item.id}") },
                            onLongClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        )
                    }
                }
            } else {
                // Show "Login with Spotify" card if empty
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .clickable {
                            navController.navigate("settings/backup_restore")
                        }
                        .padding(24.dp)
                        .animateItem(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.spotify_icon),
                            contentDescription = null,
                            tint = Color(0xFF1DB954),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.spotify_connect),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SpotifyHomePlaylistCard(
    playlist: com.harmber2.suadat.spotify.models.SpotifyPlaylist,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val thumbnailUrl = remember(playlist) { com.harmber2.suadat.spotify.SpotifyMapper.getPlaylistThumbnail(playlist) }
    val cardBgColor = rememberArtworkCardColor(
        thumbnailUrl = thumbnailUrl,
        fallbackColor = MaterialTheme.colorScheme.surfaceVariant,
    )
    
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "SpotifyCardScale"
    )
    
    val elevation by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 12.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "SpotifyCardElevation"
    )

    Column(
        modifier = modifier
            .width(180.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(elevation, shape = RoundedCornerShape(32.dp))
            .clip(RoundedCornerShape(32.dp))
            .background(cardBgColor)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(32.dp)),
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(thumbnailUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            
            // Spotify watermark / badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(14.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.4f))
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.spotify_icon),
                    contentDescription = null,
                    tint = Color(0xFF1DB954),
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            // Play overlay with nice glass effect or just solid primary
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(14.dp)
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.play),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 12.dp, bottom = 16.dp)
        ) {
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Text(
                text = "${playlist.tracks?.total ?: 0} tracks",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.SpotifyRecommendationsContainer(
    viewModel: HomeViewModel,
    mediaMetadata: MediaMetadata?,
    isPlaying: Boolean,
    navController: NavController,
    playerConnection: PlayerConnection,
    menuState: MenuState,
    haptic: HapticFeedback,
    scope: CoroutineScope,
) {
    item {
        val spotifyRecommendations by viewModel.spotifyRecommendations.collectAsStateWithLifecycle()

        if (spotifyRecommendations.isNotEmpty()) {
            Column {
                NavigationTitle(
                    title = stringResource(R.string.spotify_recommendations),
                    onClick = { /* Could go to a dedicated recs screen */ },
                    modifier = Modifier,
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(
                        items = spotifyRecommendations,
                        key = { it.id },
                    ) { item ->
                        YouTubeGridItem(
                            item = SongItem(
                                id = item.id,
                                title = item.name,
                                artists = item.artists.map { com.harmber2.suadat.innertube.models.Artist(name = it.name, id = it.id.orEmpty()) },
                                thumbnail = item.album?.images?.firstOrNull()?.url.orEmpty(),
                                explicit = item.explicit
                            ),
                            isActive = item.id == mediaMetadata?.spotifyTrackId,
                            isPlaying = isPlaying,
                            coroutineScope = scope,
                            modifier = Modifier
                                .combinedClickable(
                                    onClick = {
                                        val spotifyRecommendationsList = spotifyRecommendations
                                        val index = spotifyRecommendationsList.indexOf(item)
                                        playerConnection.playQueue(
                                            SpotifyRecommendationsQueue(
                                                seedTracks = spotifyRecommendationsList,
                                                startIndex = index.coerceAtLeast(0),
                                                preloadItem = item.toMediaMetadata()
                                            )
                                        )
                                    },
                                    onLongClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                )
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.SimilarRecommendationsContainer(
    viewModel: HomeViewModel,
    mediaMetadata: MediaMetadata?,
    isPlaying: Boolean,
    navController: NavController,
    playerConnection: PlayerConnection,
    menuState: MenuState,
    haptic: HapticFeedback,
    scope: CoroutineScope,
) {
    item {
        val similarRecommendations by viewModel.similarRecommendations.collectAsStateWithLifecycle()

        Column {
            similarRecommendations?.forEach { recommendation ->
                SimilarRecommendationsTitle(
                    recommendation = recommendation,
                    navController = navController,
                    modifier = Modifier,
                )
                SimilarRecommendationsSection(
                    recommendation = recommendation,
                    mediaMetadata = mediaMetadata,
                    isPlaying = isPlaying,
                    navController = navController,
                    playerConnection = playerConnection,
                    menuState = menuState,
                    haptic = haptic,
                    scope = scope,
                    modifier = Modifier,
                )
            }
        }
    }
}

@Composable
fun SimilarRecommendationsTitle(
    recommendation: SimilarRecommendation,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    NavigationTitle(
        title = recommendation.title.title,
        modifier = modifier,
    )
}

@Composable
fun SimilarRecommendationsSection(
    recommendation: SimilarRecommendation,
    mediaMetadata: MediaMetadata?,
    isPlaying: Boolean,
    navController: NavController,
    playerConnection: PlayerConnection,
    menuState: MenuState,
    haptic: HapticFeedback,
    scope: CoroutineScope,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        items(
            items = recommendation.items,
            key = { it.id },
        ) { item ->
            YouTubeGridItem(
                item = item,
                isActive = item.id == mediaMetadata?.id,
                isPlaying = isPlaying,
                coroutineScope = scope,
                modifier = Modifier.combinedClickable(
                    onClick = {
                        if (item is SongItem) {
                            playerConnection.playQueue(YouTubeQueue.radio(item.toMediaMetadata()))
                        }
                    },
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                )
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.AccountPlaylistsContainer(
    viewModel: HomeViewModel,
    accountName: String?,
    accountImageUrl: String?,
    mediaMetadata: MediaMetadata?,
    isPlaying: Boolean,
    navController: NavController,
    playerConnection: PlayerConnection,
    menuState: MenuState,
    haptic: HapticFeedback,
    scope: CoroutineScope,
) {
    item {
        val accountPlaylists by viewModel.accountPlaylists.collectAsStateWithLifecycle()

        val currentPlaylists = accountPlaylists
        if (!currentPlaylists.isNullOrEmpty()) {
            Column {
                AccountPlaylistsTitle(
                    accountName = accountName ?: "",
                    accountImageUrl = accountImageUrl,
                    onClick = { navController.navigate("account") },
                    modifier = Modifier,
                )
                AccountPlaylistsSection(
                    accountPlaylists = currentPlaylists,
                    accountName = accountName ?: "",
                    accountImageUrl = accountImageUrl,
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
    }
}

@Composable
fun AccountPlaylistsTitle(
    accountName: String,
    accountImageUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(accountImageUrl)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape),
        )
        Text(
            text = accountName,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Icon(
            painter = painterResource(R.drawable.navigate_next),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun AccountPlaylistsSection(
    accountPlaylists: List<PlaylistItem>,
    accountName: String,
    accountImageUrl: String?,
    mediaMetadata: MediaMetadata?,
    isPlaying: Boolean,
    navController: NavController,
    playerConnection: PlayerConnection,
    menuState: MenuState,
    haptic: HapticFeedback,
    scope: CoroutineScope,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        items(
            items = accountPlaylists,
            key = { it.id },
        ) { item ->
            YouTubeGridItem(
                item = item,
                isActive = false,
                isPlaying = false,
                coroutineScope = scope,
                modifier = Modifier.combinedClickable(
                    onClick = { navController.navigate("online_playlist/${item.id}") },
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                )
            )
        }
    }
}
