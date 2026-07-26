/*
 * harmber (2026)
 *  — github.com/suadatbiniqbal
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.harmber2.suadat.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil3.compose.rememberAsyncImagePainter
import com.harmber2.suadat.models.AdManager
import kotlin.random.Random

@Composable
fun SeasonalEffectOverlay() {
    val config by AdManager.config.collectAsState()
    
    val effectVisible = remember(config.seasonalEffect) { mutableStateOf(config.seasonalEffect != "none") }
    val alphaAnim = remember { Animatable(1f) }

    LaunchedEffect(config.seasonalEffect, config.seasonalEffectDurationSeconds) {
        if (config.seasonalEffect != "none") {
            alphaAnim.snapTo(1f)
            effectVisible.value = true
            kotlinx.coroutines.delay(config.seasonalEffectDurationSeconds * 1000L)
            alphaAnim.animateTo(0f, animationSpec = tween(1500))
            effectVisible.value = false
        }
    }

    if (effectVisible.value || alphaAnim.value > 0f) {
        Box(modifier = Modifier.fillMaxSize().graphicsLayer { alpha = alphaAnim.value }.zIndex(100f)) {
            when (config.seasonalEffect) {
                "snow" -> SnowEffect()
                "rain" -> RainEffect()
                "hearts" -> HeartEffect()
                else -> {
                    if (config.fallingEffectUrl.isNotEmpty()) {
                        CustomFallingEffect(config.fallingEffectUrl)
                    }
                }
            }
        }
    }
}

@Composable
private fun SnowEffect() {
    val infiniteTransition = rememberInfiniteTransition(label = "snow")
    val snowflakes = remember { List(200) { Snowflake() } }
    
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "snowTime"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        snowflakes.forEach { flake ->
            val progress = (time / 1000f + flake.offset) % 1f
            val y = progress * size.height
            val x = flake.x * size.width + kotlin.math.sin(progress * 10f + flake.drift) * 50f
            
            val alpha = flake.alpha * (if (progress < 0.1f) progress * 10f else 1f)
            
            // Outer soft glow
            drawCircle(
                color = Color.White.copy(alpha = alpha * 0.2f),
                radius = flake.radius * 3f,
                center = Offset(x, y)
            )
            // Mid glow
            drawCircle(
                color = Color.White.copy(alpha = alpha * 0.5f),
                radius = flake.radius * 1.5f,
                center = Offset(x, y)
            )
            // Core
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = flake.radius,
                center = Offset(x, y)
            )

            // Draw "deposited" snow at the bottom with a growing mound
            if (progress > 0.90f) {
                val moundAlpha = (progress - 0.90f) * 10f * flake.alpha
                drawCircle(
                    color = Color.White.copy(alpha = moundAlpha),
                    radius = flake.radius * 5f,
                    center = Offset(x, size.height - (3.dp.toPx() * (1f - progress) * 100f).coerceAtLeast(0f))
                )
            }
        }
        
        // Dynamic bottom accumulation
        drawRect(
            color = Color.White.copy(alpha = 0.25f),
            topLeft = Offset(0f, size.height - 8.dp.toPx()),
            size = androidx.compose.ui.geometry.Size(size.width, 8.dp.toPx())
        )
    }
}

@Composable
private fun HeartEffect() {
    val infiniteTransition = rememberInfiniteTransition(label = "hearts")
    val hearts = remember { List(40) { Heart() } }
    
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "heartTime"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        hearts.forEach { heart ->
            val progress = (time / 1000f + heart.offset) % 1f
            val y = progress * size.height
            val x = heart.x * size.width + kotlin.math.sin(progress * 8f) * 25f
            
            val scale = (0.6f + kotlin.math.sin(progress * 15f + heart.offset) * 0.4f) * heart.scale
            val alpha = if (progress < 0.1f) progress * 10f else if (progress > 0.9f) (1f - progress) * 10f else 1f

            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint().apply {
                    this.color = heart.color.toArgb()
                    this.alpha = (alpha * 255).toInt()
                    this.textSize = 24.dp.toPx() * scale
                    this.textAlign = android.graphics.Paint.Align.CENTER
                    if (heart.isNeon) {
                        this.setShadowLayer(15f, 0f, 0f, heart.color.toArgb())
                    }
                }
                canvas.nativeCanvas.drawText("❤️", x, y, paint)
            }
        }
    }
}

@Composable
private fun CustomFallingEffect(imageUrl: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "customFalling")
    val items = remember { List(30) { Snowflake() } }
    val painter = rememberAsyncImagePainter(imageUrl)
    
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "customTime"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        items.forEach { item ->
            val progress = (time / 1000f + item.offset) % 1f
            val y = progress * size.height
            val x = item.x * size.width + kotlin.math.sin(progress * 10f) * 30f
            
            translate(x, y) {
                with(painter) {
                    draw(
                        size = androidx.compose.ui.geometry.Size(24.dp.toPx(), 24.dp.toPx()),
                        alpha = item.alpha,
                        colorFilter = null
                    )
                }
            }
        }
    }
}

@Composable
private fun RainEffect() {
    val infiniteTransition = rememberInfiniteTransition(label = "rain")
    val raindrops = remember { List(100) { Raindrop() } }
    
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "rainTime"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        raindrops.forEach { drop ->
            val progress = (time / 1000f + drop.offset) % 1f
            val y = progress * size.height
            val x = drop.x * size.width
            
            drawLine(
                color = Color(0xFF88CCFF).copy(alpha = drop.alpha),
                start = Offset(x, y),
                end = Offset(x - 2f, y + 15f),
                strokeWidth = drop.width
            )
        }
    }
}

@Composable
fun Modifier.snowable(shape: Shape = RoundedCornerShape(0.dp)): Modifier {
    val config by AdManager.config.collectAsState()
    if (config.seasonalEffect != "snow") return this

    return this.drawBehind {
        // Draw a subtle white "cap" on the top of the component
        drawRect(
            color = Color.White.copy(alpha = 0.25f),
            topLeft = Offset(0f, 0f),
            size = androidx.compose.ui.geometry.Size(size.width, 2.dp.toPx())
        )
    }
}

private class Snowflake {
    val x = Random.nextFloat()
    val offset = Random.nextFloat()
    val radius = 2f + Random.nextFloat() * 4f
    val alpha = 0.3f + Random.nextFloat() * 0.5f
    val drift = Random.nextFloat() * 5f
}

private class Raindrop {
    val x = Random.nextFloat()
    val offset = Random.nextFloat()
    val width = 1f + Random.nextFloat() * 1.5f
    val alpha = 0.2f + Random.nextFloat() * 0.4f
}

private class Heart {
    val x = Random.nextFloat()
    val offset = Random.nextFloat()
    val scale = 0.5f + Random.nextFloat() * 1.5f
    val color = listOf(
        Color(0xFFFF1493), // Deep Pink
        Color(0xFFFF69B4), // Hot Pink
        Color(0xFFFF0000), // Red
        Color(0xFFFF00FF), // Magenta
    ).random()
    val isNeon = Random.nextBoolean()
}
