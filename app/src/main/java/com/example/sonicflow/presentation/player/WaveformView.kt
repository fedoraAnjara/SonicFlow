package com.example.sonicflow.presentation.player

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun WaveformView(
    progress: Float,
    isPlaying: Boolean,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val barCount = 100

    // Générer une waveform statique réaliste (une seule fois)
    val staticHeights = remember {
        List(barCount) { i ->
            // Créer une forme de vague réaliste
            val position = i.toFloat() / barCount
            val base = (sin(position * 12) * 0.3f + 0.5f).toFloat()
            val variation = Random.nextFloat() * 0.2f
            (base + variation).coerceIn(0.2f, 1f)
        }
    }

    // Animation subtile seulement si ça joue
    var animationOffset by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (true) {
                kotlinx.coroutines.delay(50)
                animationOffset += 0.1f
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp), // ← Réduit à 120dp au lieu de 200dp
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

            staticHeights.forEachIndexed { index, baseHeight ->
                val x = index * barWidth + barWidth / 2

                // Animation subtile uniquement autour de la position de lecture
                val distanceFromProgress = abs(index.toFloat() / barCount - progress)
                val animationFactor = if (isPlaying && distanceFromProgress < 0.1f) {
                    1f + sin(animationOffset + index * 0.5f) * 0.15f
                } else {
                    1f
                }

                val barHeight = baseHeight * animationFactor
                val barMaxHeight = height * 0.7f
                val actualBarHeight = barHeight * barMaxHeight / 2

                val color = if (index.toFloat() / barCount <= progress) {
                    Color(0xFFFF6600) // Orange pour la partie jouée
                } else {
                    Color.White.copy(alpha = 0.25f) // Gris clair pour non jouée
                }

                // Barre unique (pas de miroir pour gagner de la place)
                drawLine(
                    color = color,
                    start = Offset(x, centerY - actualBarHeight),
                    end = Offset(x, centerY + actualBarHeight),
                    strokeWidth = barWidth * 0.7f,
                    cap = StrokeCap.Round
                )
            }

            // Indicateur de progression (ligne fine)
            val progressX = progress * width
            drawCircle(
                color = Color.White,
                radius = 6f,
                center = Offset(progressX, centerY)
            )
        }
    }
}