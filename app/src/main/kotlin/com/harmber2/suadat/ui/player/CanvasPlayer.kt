/*
 * harmber (2026)
 * © Rukamori — github.com/suadatbiniqbal
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

@file:OptIn(androidx.media3.common.util.UnstableApi::class)

package com.harmber2.suadat.ui.player

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.*
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.compose.ContentFrame
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW
import okhttp3.OkHttpClient
import java.util.*

@Composable
fun CanvasPlayer(
    videoUrl: String?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    resizeMode: Int = AspectRatioFrameLayout.RESIZE_MODE_ZOOM,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val url = videoUrl?.takeIf { it.isNotBlank() } ?: return
    
    var isVideoReady by remember(url) { mutableStateOf(false) }
    val shouldPlay by rememberUpdatedState(isPlaying)

    val okHttpClient = remember {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                // Skip proxy and special headers for Spotify CDN
                chain.proceed(request)
            }.build()
    }
    
    val mediaSourceFactory = remember(okHttpClient) {
        DefaultMediaSourceFactory(
            DefaultDataSource.Factory(context, OkHttpDataSource.Factory(okHttpClient))
        )
    }
    
    val exoPlayer = remember(url, mediaSourceFactory) {
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
            .apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(C.USAGE_MEDIA)
                        .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                        .build(),
                    false
                )
                volume = 0f
                repeatMode = Player.REPEAT_MODE_ONE
                playWhenReady = isPlaying
            }
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) exoPlayer.play() else exoPlayer.pause()
    }

    DisposableEffect(exoPlayer, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START || event == Lifecycle.Event.ON_RESUME) {
                if (shouldPlay) exoPlayer.play()
            } else if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                exoPlayer.pause()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    DisposableEffect(exoPlayer, url) {
        val listener = object : Player.Listener {
            override fun onRenderedFirstFrame() {
                isVideoReady = true
            }
            override fun onPlayerError(error: PlaybackException) {
                isVideoReady = false
            }
        }
        exoPlayer.addListener(listener)
        
        val mediaItem = MediaItem.Builder()
            .setUri(url)
            .setMimeType(MimeTypes.VIDEO_MP4)
            .build()
            
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    val alpha by animateFloatAsState(
        targetValue = if (isVideoReady) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "canvasAlpha"
    )

    ContentFrame(
        player = exoPlayer,
        surfaceType = SURFACE_TYPE_TEXTURE_VIEW,
        contentScale = if (resizeMode == AspectRatioFrameLayout.RESIZE_MODE_ZOOM) ContentScale.Crop else ContentScale.Fit,
        keepContentOnReset = false,
        shutter = {},
        modifier = modifier.alpha(alpha)
    )
}
