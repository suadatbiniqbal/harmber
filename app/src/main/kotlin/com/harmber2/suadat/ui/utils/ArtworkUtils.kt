/*
 * harmber (2026)
 * © Rukamori — github.com/suadatbiniqbal
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.harmber2.suadat.ui.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import coil3.imageLoader
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.request.crossfade
import coil3.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.harmber2.suadat.constants.PureBlackKey
import com.harmber2.suadat.ui.theme.PlayerColorExtractor
import com.harmber2.suadat.utils.rememberPreference

@Composable
fun rememberArtworkGradient(
    thumbnailUrl: String?,
    fallbackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
): List<Color> {
    val context = LocalContext.current
    var colors by remember(thumbnailUrl) { mutableStateOf(listOf(fallbackColor)) }

    LaunchedEffect(thumbnailUrl) {
        if (thumbnailUrl == null) {
            colors = listOf(fallbackColor)
            return@LaunchedEffect
        }

        val request =
            ImageRequest
                .Builder(context)
                .data(thumbnailUrl)
                .size(128, 128)
                .allowHardware(false)
                .crossfade(true)
                .build()

        val result = context.imageLoader.execute(request)
        if (result is SuccessResult) {
            val bitmap = result.image.toBitmap()
            val palette =
                withContext(Dispatchers.Default) {
                    Palette
                        .from(bitmap)
                        .maximumColorCount(PlayerColorExtractor.Config.MAX_COLOR_COUNT)
                        .resizeBitmapArea(PlayerColorExtractor.Config.BITMAP_AREA)
                        .generate()
                }

            val extracted =
                PlayerColorExtractor.extractGradientColors(
                    palette = palette,
                    fallbackColor = fallbackColor.toArgb(),
                )

            if (extracted.isNotEmpty()) {
                colors = extracted
            }
        } else if (result is ErrorResult) {
            colors = listOf(fallbackColor)
        }
    }
    return colors
}

@Composable
fun rememberArtworkCardColor(
    thumbnailUrl: String?,
    fallbackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
): Color {
    val gradientColors =
        rememberArtworkGradient(
            thumbnailUrl = thumbnailUrl,
            fallbackColor = fallbackColor,
        )
    val surfaceColor = MaterialTheme.colorScheme.surface
    val useDarkTheme = remember(surfaceColor) { ColorUtils.calculateLuminance(surfaceColor.toArgb()) < 0.5 }
    val (pureBlack) = rememberPreference(PureBlackKey, defaultValue = false)

    return remember(gradientColors, useDarkTheme, pureBlack) {
        val baseColor = gradientColors.firstOrNull() ?: fallbackColor
        val baseArgb = baseColor.toArgb()
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(baseArgb, hsv)
        val hue = hsv[0]

        if (useDarkTheme) {
            val s = (hsv[1] * 0.45f).coerceIn(0.06f, 0.20f)
            val v = if (pureBlack) 0.18f else 0.12f
            Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, s, v)))
        } else {
            val s = (hsv[1] * 0.30f).coerceIn(0.03f, 0.12f)
            val v = 0.95f
            Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, s, v)))
        }
    }
}
