package com.example.sonicflow.presentation.player

import android.content.ContentUris
import android.net.Uri
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import androidx.compose.ui.graphics.vector.ImageVector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullPlayerScreen(
    onBackClick: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    var showWaveform by remember { mutableStateOf(false) }
    val state by viewModel.state.collectAsState()
    val currentTrack = state.currentTrack ?: return

    val albumArtUri = ContentUris.withAppendedId(
        Uri.parse("content://media/external/audio/albumart"),
        currentTrack.albumId
    )

    // Dégradé dynamique
    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0a0a0a),
            Color(0xFF1a0f00),
            Color(0xFF2d1600),
            Color(0xC6FF6600)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header avec bouton retour
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.1f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }


                Spacer(modifier = Modifier.size(38.dp))
            }

            Spacer(modifier = Modifier.height(35.dp))

            // Album Art
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .aspectRatio(1f)
            ) {
                AsyncImage(
                    model = albumArtUri,
                    contentDescription = "Album art",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(24.dp)),
                    contentScale = ContentScale.Crop,
                    error = rememberVectorPainter(Icons.Default.MusicNote)
                )
            }

            Spacer(modifier = Modifier.weight(0.2f))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MarqueeText(
                    text = currentTrack.title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = currentTrack.artist,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp
                    ),
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            //Seekbar de progression
            if (showWaveform) {
                WaveformView(
                    progress = if (state.duration > 0) state.currentPosition.toFloat() / state.duration else 0f,
                    isPlaying = state.isPlaying,
                    onSeek = { progress ->
                        viewModel.seekTo((progress * state.duration).toLong())
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    var sliderPosition by remember { mutableFloatStateOf(0f) }
                    var isSeeking by remember { mutableStateOf(false) }
                    var isHovering by remember { mutableStateOf(false) }

                    LaunchedEffect(state.currentPosition, state.duration) {
                        if (!isSeeking && state.duration > 0) {
                            sliderPosition = state.currentPosition.toFloat() / state.duration
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Slider(
                            value = sliderPosition,
                            onValueChange = {
                                isSeeking = true
                                isHovering = true
                                sliderPosition = it
                            },
                            onValueChangeFinished = {
                                isSeeking = false
                                isHovering = false
                                viewModel.seekTo((sliderPosition * state.duration).toLong())
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = SliderDefaults.colors(
                                thumbColor = if (isHovering || isSeeking) Color.White else Color.Transparent,
                                activeTrackColor = Color(0xFFFF6600),
                                inactiveTrackColor = Color.White.copy(alpha = 0.25f)
                            ),
                            thumb = {
                                if (isHovering || isSeeking) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .shadow(6.dp, CircleShape)
                                            .background(Color.White, CircleShape)
                                    )
                                }
                            },
                            track = { sliderState ->
                                SliderDefaults.Track(
                                    sliderState = sliderState,
                                    modifier = Modifier.height(if (isHovering || isSeeking) 6.dp else 3.dp),
                                    thumbTrackGapSize = 0.dp,
                                    drawStopIndicator = null
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatTime(state.currentPosition),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 8.sp
                        )
                        Text(
                            text = formatTime(state.duration),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 8.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            //Controles de lecture
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(
                    onClick = { viewModel.playPrevious() },
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.1f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }

                // Play/Pause
                IconButton(
                    onClick = { viewModel.togglePlayPause() },
                    modifier = Modifier
                        .size(70.dp)
                        .shadow(
                            elevation = 12.dp,
                            shape = CircleShape,
                            spotColor = Color(0xFFFF6600)
                        )
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFFF6600),
                                    Color(0xFFFF8833)
                                )
                            ),
                            shape = CircleShape
                        )
                ) {
                    if (state.isPlaying) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = "Pause",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }


                IconButton(
                    onClick = { viewModel.playNext() },
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.1f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            //Contrôle de volume
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                var volume by remember { mutableFloatStateOf(0.7f) }
                var isVolumeHovering by remember { mutableStateOf(false) }


                Icon(
                    imageVector = Icons.Rounded.VolumeDown,
                    contentDescription = "Volume Down",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(24.dp)
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Slider(
                        value = volume,
                        onValueChange = {
                            volume = it
                            isVolumeHovering = true
                        },
                        onValueChangeFinished = {
                            isVolumeHovering = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = if (isVolumeHovering) Color.White else Color.Transparent,
                            activeTrackColor = Color(0xFFFF6600),
                            inactiveTrackColor = Color.White.copy(alpha = 0.25f)
                        ),
                        thumb = {
                            if (isVolumeHovering) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .shadow(4.dp, CircleShape)
                                        .background(Color.White, CircleShape)
                                )
                            }
                        },
                        track = { sliderState ->
                            SliderDefaults.Track(
                                sliderState = sliderState,
                                modifier = Modifier.height(if (isVolumeHovering) 5.dp else 3.dp),
                                thumbTrackGapSize = 0.dp,
                                drawStopIndicator = null
                            )
                        }
                    )
                }


                Icon(
                    imageVector = Icons.Rounded.VolumeUp,
                    contentDescription = "Volume Up",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Waveform, Paroles, Playlist
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Waveform
                IconButton(
                    onClick = { showWaveform = !showWaveform },
                ) {
                    Icon(
                        imageVector = Icons.Rounded.GraphicEq,
                        contentDescription = "Waveform",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Paroles
                IconButton(
                    onClick = { /* TODO: Afficher paroles */ },
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Message,
                        contentDescription = "Paroles",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Playlist
                IconButton(
                    onClick = { /* TODO: Afficher playlist */ }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.QueueMusic,
                        contentDescription = "Liste de lecture",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

@Composable
fun MarqueeText(
    text: String,
    style: androidx.compose.ui.text.TextStyle,
    color: Color,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = style,
        color = color,
        maxLines = 1,
        modifier = modifier.basicMarquee(
            animationMode = MarqueeAnimationMode.Immediately,
            initialDelayMillis = 1000,
            velocity = 30.dp
        )
    )
}

private fun formatTime(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / 1000) / 60
    return String.format("%d:%02d", minutes, seconds)
}