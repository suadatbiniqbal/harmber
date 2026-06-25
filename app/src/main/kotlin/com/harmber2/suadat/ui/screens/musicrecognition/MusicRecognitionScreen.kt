/*
 * harmber (2026)
 * © Rukamori — github.com/suadatbiniqbal
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.harmber2.suadat.ui.screens.musicrecognition

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.allowHardware
import com.harmber2.suadat.R
import com.harmber2.suadat.musicrecognition.MusicRecognitionAutoStartRequestKey
import com.harmber2.suadat.musicrecognition.MusicRecognitionRoute
import com.harmber2.suadat.shazamkit.Shazam
import com.harmber2.suadat.shazamkit.ShazamSignatureGenerator
import com.harmber2.suadat.shazamkit.models.RecognitionResult
import com.harmber2.suadat.ui.screens.search.onlineSearchResultRoute
import com.harmber2.suadat.ui.utils.appBarScrollBehavior
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicRecognitionScreen(navController: NavHostController) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    var state by remember { mutableStateOf<MusicRecognitionState>(MusicRecognitionState.Ready) }
    var recognitionJob by remember { mutableStateOf<Job?>(null) }
    val backStackEntry = remember(navController) { navController.getBackStackEntry(MusicRecognitionRoute) }
    val autoStartRequestId by backStackEntry.savedStateHandle
        .getStateFlow(MusicRecognitionAutoStartRequestKey, 0L)
        .collectAsStateWithLifecycle()

    val strings =
        remember {
            MusicRecognitionStrings(
                signatureFailed = context.getString(R.string.music_recognition_signature_failed),
                noMatchFallback = context.getString(R.string.music_recognition_no_match),
                recognitionFailedFallback = context.getString(R.string.music_recognition_recognition_failed),
            )
        }

    fun handleRecognitionState(nextState: MusicRecognitionState) {
        state = nextState
    }

    val requestPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                launchRecognition(
                    scope = scope,
                    strings = strings,
                    onState = ::handleRecognitionState,
                    onHaptic = { haptic.performHapticFeedback(HapticFeedbackType.LongPress) },
                    onReplaceJob = {
                        recognitionJob?.cancel()
                        recognitionJob = it
                    },
                )
            } else {
                state = MusicRecognitionState.PermissionRequired
            }
        }

    fun cancelRecognition() {
        recognitionJob?.cancel()
        recognitionJob = null
        state = MusicRecognitionState.Ready
    }

    fun startOrRequestPermission() {
        val permission =
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        if (permission) {
            launchRecognition(
                scope = scope,
                strings = strings,
                onState = ::handleRecognitionState,
                onHaptic = { haptic.performHapticFeedback(HapticFeedbackType.LongPress) },
                onReplaceJob = {
                    recognitionJob?.cancel()
                    recognitionJob = it
                },
            )
        } else {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    DisposableEffect(Unit) {
        onDispose { recognitionJob?.cancel() }
    }

    LaunchedEffect(autoStartRequestId) {
        if (autoStartRequestId == 0L) return@LaunchedEffect
        backStackEntry.savedStateHandle[MusicRecognitionAutoStartRequestKey] = 0L
        startOrRequestPermission()
    }

    val scrollBehavior = appBarScrollBehavior()

    Scaffold(
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(stringResource(R.string.music_recognition)) },
                navigationIcon = {
                    IconButton(onClick = navController::navigateUp) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_back),
                            contentDescription = stringResource(R.string.back_button_desc),
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = MaterialTheme.colorScheme.surface,
                    ),
                scrollBehavior = scrollBehavior,
            )
        },
        containerColor = Color.Transparent,
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .statusBarsPadding(),
        ) {
            AnimatedContent(
                targetState = state,
                transitionSpec = {
                    (fadeIn(tween(220)) + scaleIn(tween(220), initialScale = 0.98f))
                        .togetherWith(fadeOut(tween(160)) + scaleOut(tween(160), targetScale = 1.02f))
                },
                modifier = Modifier.align(Alignment.Center),
                label = "stateContent",
            ) { target ->
                when (target) {
                    is MusicRecognitionState.Success -> {
                        RecognitionResultSimple(
                            result = target.result,
                            onSearch = {
                                val query = "${target.result.title} ${target.result.artist}".trim()
                                navController.navigate(onlineSearchResultRoute(query))
                            },
                            onListenAgain = { startOrRequestPermission() },
                        )
                    }

                    else -> {
                        RecognitionListenPane(
                            state = target,
                            onStart = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                startOrRequestPermission()
                            },
                            onCancel = ::cancelRecognition,
                            onRequestPermission = {
                                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecognitionResultSimple(
    result: RecognitionResult,
    onSearch: () -> Unit,
    onListenAgain: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AsyncImage(
            model = result.coverArtHqUrl ?: result.coverArtUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(240.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = result.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = result.artist,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FilledTonalButton(
                onClick = onListenAgain,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(painterResource(R.drawable.replay), contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.music_recognition_listen_again))
            }
            
            Button(
                onClick = onSearch,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(painterResource(R.drawable.search), contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.search))
            }
        }
    }
}

@Composable
private fun RecognitionListenPane(
    state: MusicRecognitionState,
    onStart: () -> Unit,
    onCancel: () -> Unit,
    onRequestPermission: () -> Unit,
) {
    val isListening = state is MusicRecognitionState.Listening
    val isProcessing = state is MusicRecognitionState.Processing
    val isBusy = isListening || isProcessing

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = if (isBusy) stringResource(R.string.music_recognition_listening) else stringResource(R.string.music_recognition_tap_to_listen),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(48.dp))

        ListeningOrb(
            modifier = Modifier.size(260.dp),
            isActive = isListening,
            isProcessing = isProcessing,
            onClick = onStart,
        )

        Spacer(modifier = Modifier.height(48.dp))

        if (state is MusicRecognitionState.PermissionRequired) {
            Button(onClick = onRequestPermission) {
                Text(stringResource(R.string.allow))
            }
        } else if (state is MusicRecognitionState.NoMatch || state is MusicRecognitionState.Error) {
            val message = if (state is MusicRecognitionState.NoMatch) state.message else (state as MusicRecognitionState.Error).message
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 48.dp)
            )
        }

        AnimatedVisibility(
            visible = isListening,
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(120)),
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.padding(top = 24.dp),
                shape = CircleShape
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    }
}

private sealed interface MusicRecognitionState {
    data object Ready : MusicRecognitionState

    data object Listening : MusicRecognitionState

    data object Processing : MusicRecognitionState

    data object PermissionRequired : MusicRecognitionState

    data class Success(
        val result: RecognitionResult,
    ) : MusicRecognitionState

    data class NoMatch(
        val message: String,
    ) : MusicRecognitionState

    data class Error(
        val message: String,
    ) : MusicRecognitionState
}

@Immutable
private data class MusicRecognitionStrings(
    val signatureFailed: String,
    val noMatchFallback: String,
    val recognitionFailedFallback: String,
)

private fun launchRecognition(
    scope: kotlinx.coroutines.CoroutineScope,
    strings: MusicRecognitionStrings,
    onState: (MusicRecognitionState) -> Unit,
    onHaptic: () -> Unit,
    onReplaceJob: (Job) -> Unit,
) {
    onReplaceJob(
        scope.launch {
            runRecognitionFlow(
                strings = strings,
                onState = onState,
                onHaptic = onHaptic,
            )
        },
    )
}

private suspend fun runRecognitionFlow(
    strings: MusicRecognitionStrings,
    onState: (MusicRecognitionState) -> Unit,
    onHaptic: () -> Unit,
) {
    onHaptic()
    onState(MusicRecognitionState.Listening)

    val samples =
        withContext(Dispatchers.IO) {
            recordMicPcm16Mono(
                sampleRateHz = 16000,
                recordMs = 4200L,
            ).first
        }

    onState(MusicRecognitionState.Processing)

    val signature =
        withContext(Dispatchers.Default) {
            ShazamSignatureGenerator()
                .apply {
                    feedPcm16Mono(samples)
                }.nextSignatureOrNull()
        }

    if (signature == null) {
        onState(MusicRecognitionState.Error(strings.signatureFailed))
        return
    }

    val result =
        withContext(Dispatchers.IO) {
            Shazam.recognize(signature.uri, signature.sampleDurationMs)
        }

    result.fold(
        onSuccess = { onState(MusicRecognitionState.Success(it)) },
        onFailure = { e ->
            val msg = e.message?.trim().orEmpty()
            when {
                msg.contains("no match", ignoreCase = true) || msg.contains("404") -> {
                    onState(MusicRecognitionState.NoMatch(msg.ifEmpty { strings.noMatchFallback }))
                }

                else -> {
                    onState(MusicRecognitionState.Error(msg.ifEmpty { strings.recognitionFailedFallback }))
                }
            }
        },
    )
}

private suspend fun recordMicPcm16Mono(
    sampleRateHz: Int,
    recordMs: Long,
): Pair<ShortArray, Int> =
    withContext(Dispatchers.IO) {
        val channel = AudioFormat.CHANNEL_IN_MONO
        val encoding = AudioFormat.ENCODING_PCM_16BIT
        val minBuffer = AudioRecord.getMinBufferSize(sampleRateHz, channel, encoding).coerceAtLeast(4096)
        val record =
            AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRateHz,
                channel,
                encoding,
                minBuffer,
            )

        val totalSamples = ((recordMs / 1000.0) * sampleRateHz).toInt().coerceAtLeast(sampleRateHz)
        val output = ShortArray(totalSamples)
        val buffer = ShortArray(minBuffer / 2)

        try {
            record.startRecording()

            var written = 0
            while (written < output.size && isActive) {
                val read = record.read(buffer, 0, minOf(buffer.size, output.size - written))
                if (read > 0) {
                    System.arraycopy(buffer, 0, output, written, read)
                    written += read
                }
            }

            if (written <= 0) {
                ShortArray(0) to sampleRateHz
            } else {
                output.copyOf(written) to sampleRateHz
            }
        } finally {
            runCatching { record.stop() }
            runCatching { record.release() }
        }
    }

@Composable
private fun ListeningOrb(
    modifier: Modifier,
    isActive: Boolean,
    isProcessing: Boolean,
    onClick: () -> Unit,
) {
    val ringProgress: Float
    val ringProgress2: Float
    if (isActive) {
        val infinite = rememberInfiniteTransition(label = "orbPulse")
        val animatedRingProgress by infinite.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(animation = tween(1700, easing = LinearEasing)),
            label = "ring1",
        )
        val animatedRingProgress2 by infinite.animateFloat(
            initialValue = 0.25f,
            targetValue = 1.25f,
            animationSpec = infiniteRepeatable(animation = tween(2100, easing = LinearEasing)),
            label = "ring2",
        )
        ringProgress = animatedRingProgress
        ringProgress2 = animatedRingProgress2
    } else {
        ringProgress = 0f
        ringProgress2 = 0f
    }

    val orbScale by animateFloatAsState(
        targetValue = if (isActive) 1.03f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "orbScale",
    )

    val baseColor =
        if (isProcessing) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
    val surfaceContainerHigh = MaterialTheme.colorScheme.surfaceContainerHigh

    val container =
        remember(baseColor, surfaceContainerHigh) {
            Brush.radialGradient(
                colors =
                    listOf(
                        baseColor.copy(alpha = 0.42f),
                        baseColor.copy(alpha = 0.16f),
                        surfaceContainerHigh.copy(alpha = 0.9f),
                    ),
            )
        }
    val density = LocalDensity.current
    val ringStrokeWidth = remember(density) { with(density) { 10.dp.toPx() } }
    val ringStroke = remember(ringStrokeWidth) { Stroke(width = ringStrokeWidth, cap = StrokeCap.Round) }

    Box(
        modifier =
            modifier
                .scale(orbScale)
                .clip(CircleShape)
                .background(container)
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val r = size.minDimension / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            if (isActive) {
                drawRing(center, r, ringProgress, baseColor, ringStroke)
                drawRing(center, r, ringProgress2, baseColor.copy(alpha = 0.85f), ringStroke)
            }

            val glow = baseColor.copy(alpha = if (isActive) 0.22f else 0.12f)
            drawCircle(glow, radius = r * 0.88f, center = center)
            drawCircle(Color.Black.copy(alpha = 0.06f), radius = r * 0.78f, center = center)
        }

        val icon =
            when {
                isProcessing -> R.drawable.cached
                isActive -> R.drawable.listening
                else -> R.drawable.mic
            }

        val iconAlpha by animateFloatAsState(
            targetValue = if (isProcessing) 0.9f else 1f,
            animationSpec = tween(180),
            label = "iconAlpha",
        )

        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(54.dp).alpha(iconAlpha),
            tint = MaterialTheme.colorScheme.onSurface,
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRing(
    center: Offset,
    baseRadius: Float,
    progress: Float,
    color: Color,
    stroke: Stroke,
) {
    val p = progress.coerceIn(0f, 1f)
    val radius = baseRadius * (0.62f + 0.55f * p)
    val alpha = (1f - p) * 0.55f
    drawCircle(
        color = color.copy(alpha = alpha),
        radius = radius,
        center = center,
        style = stroke,
    )
}
