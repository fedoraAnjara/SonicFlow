package com.example.sonicflow.presentation.player

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.abs

@Composable
fun WaveformView(
    progress: Float,
    isPlaying: Boolean,
    waveformData: List<Float>,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val barCount = 100

    val heights = if (waveformData.isNotEmpty()) {
        waveformData
    } else {
        List(barCount) { 0.5f }
    }

    // Animation légère uniquement pour l'indicateur de progression
    val infiniteTransition = rememberInfiniteTransition(label = "progress_indicator")

    val progressGlow by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val clickProgress = offset.x / size.width
                        onSeek(clickProgress.coerceIn(0f, 1f))
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            val barWidth = width / barCount
            val centerY = height / 2

            heights.forEachIndexed { index, barHeight ->
                val x = index * barWidth + barWidth / 2
                val normalizedIndex = index.toFloat() / barCount

                val barMaxHeight = height * 0.75f
                val actualBarHeight = barHeight * barMaxHeight / 2

                val distanceFromProgress = abs(normalizedIndex - progress)

                val baseColor = if (normalizedIndex <= progress) {
                    Color(0xFFFF6600)
                } else {
                    Color.White.copy(alpha = 0.35f)
                }

                val focusIntensity = if (isPlaying && distanceFromProgress < 0.1f) {
                    1f + (1f - distanceFromProgress / 0.1f) * 0.2f
                } else {
                    1f
                }

                val finalHeight = actualBarHeight * focusIntensity

                val alpha = if (normalizedIndex <= progress) {
                    if (distanceFromProgress < 0.05f && isPlaying) {
                        1f
                    } else {
                        0.85f
                    }
                } else {
                    0.35f
                }

                val color = baseColor.copy(alpha = alpha)

                drawLine(
                    color = color,
                    start = Offset(x, centerY - finalHeight),
                    end = Offset(x, centerY + finalHeight),
                    strokeWidth = barWidth * 0.8f,
                    cap = StrokeCap.Round
                )

                if (isPlaying && distanceFromProgress < 0.04f) {
                    val glowAlpha = (1f - distanceFromProgress / 0.04f) * 0.3f
                    drawLine(
                        color = Color(0xFFFF6600).copy(alpha = glowAlpha),
                        start = Offset(x, centerY - finalHeight * 1.1f),
                        end = Offset(x, centerY + finalHeight * 1.1f),
                        strokeWidth = barWidth * 1.4f,
                        cap = StrokeCap.Round
                    )
                }
            }

            // Indicateur de progression
            val progressX = progress * width

            if (isPlaying) {
                drawCircle(
                    color = Color(0xFFFF6600).copy(alpha = 0.4f * progressGlow),
                    radius = 14f,
                    center = Offset(progressX, centerY)
                )
            }

            drawCircle(
                color = Color.White,
                radius = 7f,
                center = Offset(progressX, centerY)
            )

            drawCircle(
                color = Color(0xFFFF6600),
                radius = 3.5f,
                center = Offset(progressX, centerY)
            )
        }
    }
}