package com.example.sonicflow.presentation.player

import android.content.ContentUris
import android.net.Uri
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import com.example.sonicflow.domain.model.AudioTrack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullPlayerScreen(
    onBackClick: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    var showWaveform by remember { mutableStateOf(false) }
    val state by viewModel.state.collectAsState()
    val currentTrack = state.currentTrack ?: return
    var showQueueSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

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
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .navigationBarsPadding(),
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

            Spacer(modifier = Modifier.height(20.dp))

            // Album Art
            Box(
                modifier = Modifier
                    .fillMaxWidth(if (showWaveform) 0.75f else 0.85f)
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

            Spacer(modifier = Modifier.weight(if (showWaveform) 0.05f else 0.15f))

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

            Spacer(modifier = Modifier.height(if (showWaveform) 12.dp else 22.dp))

            //Seekbar de progression
            if (showWaveform) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                ) {
                    WaveformView(
                        progress = if (state.duration > 0) state.currentPosition.toFloat() / state.duration else 0f,
                        isPlaying = state.isPlaying,
                        waveformData = state.waveformData,
                        onSeek = { progress ->
                            viewModel.seekTo((progress * state.duration).toLong())
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

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

            Spacer(modifier = Modifier.height(if (showWaveform) 20.dp else 20.dp))

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

            Spacer(modifier = Modifier.height(if (showWaveform) 35.dp else 20.dp))

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
                        value = state.volume,
                        onValueChange = {
                            isVolumeHovering = true
                            viewModel.setVolume(it)
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

            Spacer(modifier = Modifier.height(if (showWaveform) 12.dp else 20.dp))

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
                        tint = if (showWaveform) Color(0xFFFF6600) else Color.White.copy(alpha = 0.8f),
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
                    onClick = { showQueueSheet = true }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.QueueMusic,
                        contentDescription = "Liste de lecture",
                        tint = if (showQueueSheet) Color(0xFFFF6600) else Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    if (showQueueSheet) {
        ModalBottomSheet(
            onDismissRequest = { showQueueSheet = false },
            sheetState = sheetState,
            containerColor = Color(0xFF1a1a1a),
            contentColor = Color.White,
            dragHandle = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .width(40.dp)
                            .height(4.dp)
                            .background(
                                Color.White.copy(alpha = 0.3f),
                                RoundedCornerShape(2.dp)
                            )
                    )
                }
            }
        ) {
            QueueContent(
                queue = state.queue,
                currentTrack = currentTrack,
                onTrackClick = { track ->
                    viewModel.playTrack(track)
                    showQueueSheet = false
                },
                onRemoveTrack = { track ->
                    viewModel.removeFromQueue(track)
                },
                onClearQueue = {
                    viewModel.clearQueue()
                    showQueueSheet = false
                },
                repeatMode = state.repeatMode,
                shuffleMode = state.shuffleMode,
                onToggleRepeat = { viewModel.toggleRepeatMode() },
                onToggleShuffle = { viewModel.toggleShuffleMode() }
            )
        }
    }
}

@Composable
fun QueueContent(
    queue: List<AudioTrack>,
    currentTrack: AudioTrack,
    onTrackClick: (AudioTrack) -> Unit,
    onRemoveTrack: (AudioTrack) -> Unit,
    onClearQueue: () -> Unit,
    repeatMode: RepeatMode,
    shuffleMode: ShuffleMode,
    onToggleRepeat: () -> Unit,
    onToggleShuffle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "File d'attente",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${queue.size} chanson${if (queue.size > 1) "s" else ""}",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            if (queue.isNotEmpty()) {
                TextButton(
                    onClick = onClearQueue,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFFFF6600)
                    )
                ) {
                    Text("Effacer tout")
                }
            }
        }

        // Contrôles de lecture (Repeat & Shuffle)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bouton Shuffle
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .clickable { onToggleShuffle() },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (shuffleMode == ShuffleMode.ON)
                        Color(0xFFFF6600).copy(alpha = 0.2f)
                    else
                        Color.White.copy(alpha = 0.05f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (shuffleMode == ShuffleMode.ON)
                            Color(0xFFFF6600)
                        else
                            Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Aléatoire",
                        fontSize = 15.sp,
                        fontWeight = if (shuffleMode == ShuffleMode.ON)
                            FontWeight.SemiBold
                        else
                            FontWeight.Normal,
                        color = if (shuffleMode == ShuffleMode.ON)
                            Color(0xFFFF6600)
                        else
                            Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            // Bouton Repeat
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .clickable { onToggleRepeat() },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (repeatMode != RepeatMode.OFF)
                        Color(0xFFFF6600).copy(alpha = 0.2f)
                    else
                        Color.White.copy(alpha = 0.05f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (repeatMode) {
                            RepeatMode.ONE -> Icons.Rounded.RepeatOne
                            else -> Icons.Rounded.Repeat
                        },
                        contentDescription = "Repeat",
                        tint = if (repeatMode != RepeatMode.OFF)
                            Color(0xFFFF6600)
                        else
                            Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (repeatMode) {
                            RepeatMode.OFF -> "Répéter"
                            RepeatMode.ALL -> "Tout"
                            RepeatMode.ONE -> "Un"
                        },
                        fontSize = 15.sp,
                        fontWeight = if (repeatMode != RepeatMode.OFF)
                            FontWeight.SemiBold
                        else
                            FontWeight.Normal,
                        color = if (repeatMode != RepeatMode.OFF)
                            Color(0xFFFF6600)
                        else
                            Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Divider(
            color = Color.White.copy(alpha = 0.1f),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )

        // Liste des pistes
        if (queue.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Rounded.QueueMusic,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.White.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "File d'attente vide",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(queue, key = { it.id }) { track ->
                    QueueTrackItem(
                        track = track,
                        isCurrentTrack = track.id == currentTrack.id,
                        onClick = { onTrackClick(track) },
                        onRemove = { onRemoveTrack(track) }
                    )
                }
            }
        }
    }
}

@Composable
fun QueueTrackItem(
    track: AudioTrack,
    isCurrentTrack: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    val albumArtUri = ContentUris.withAppendedId(
        Uri.parse("content://media/external/audio/albumart"),
        track.albumId
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentTrack)
                Color(0xFFFF6600).copy(alpha = 0.2f)
            else
                Color.White.copy(alpha = 0.05f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicateur de lecture en cours
            if (isCurrentTrack) {
                Icon(
                    imageVector = Icons.Rounded.GraphicEq,
                    contentDescription = "En lecture",
                    tint = Color(0xFFFF6600),
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 8.dp)
                )
            }

            // Album art
            AsyncImage(
                model = albumArtUri,
                contentDescription = "Album art",
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(6.dp)),
                contentScale = ContentScale.Crop,
                error = rememberVectorPainter(Icons.Default.MusicNote)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Infos de la piste
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    fontSize = 15.sp,
                    fontWeight = if (isCurrentTrack) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isCurrentTrack) Color(0xFFFF6600) else Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = track.artist,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Durée
            Text(
                text = formatTime(track.duration),
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Menu
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(Color(0xFF2a2a2a))
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Retirer de la file",
                                color = Color.White
                            )
                        },
                        onClick = {
                            showMenu = false
                            onRemove()
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    )
                }
            }
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