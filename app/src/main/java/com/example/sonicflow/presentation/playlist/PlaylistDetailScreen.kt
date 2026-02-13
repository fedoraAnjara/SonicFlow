package com.example.sonicflow.presentation.playlist

import android.content.ContentUris
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.sonicflow.domain.model.AudioTrack
import com.example.sonicflow.presentation.player.PlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlistId: Long,
    onBackClick: () -> Unit,
    viewModel: PlaylistViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel()
) {
    val playlistWithTracks by viewModel.getPlaylistWithTracks(playlistId).collectAsState(initial = null)
    val allTracks by viewModel.allTracks.collectAsState()
    var showAddTracksDialog by remember { mutableStateOf(false) }

    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1a0f00),
            Color.Black
        )
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        playlistWithTracks?.playlist?.name ?: "Playlist",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Retour",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTracksDialog = true },
                containerColor = Color(0xFFFF6600),
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Ajouter des pistes"
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(paddingValues)
        ) {
            playlistWithTracks?.let { data ->
                if (data.tracks.isEmpty()) {
                    // État vide
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.White.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Playlist vide",
                            fontSize = 18.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Appuyez sur + pour ajouter des chansons",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // En-tête avec bouton "Tout lire"
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF2D1302).copy(alpha = 0.6f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${data.tracks.size} chanson${if (data.tracks.size > 1) "s" else ""}",
                                        fontSize = 16.sp,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }

                                Button(
                                    onClick = {
                                        if (data.tracks.isNotEmpty()) {
                                            playerViewModel.setQueueAndPlay(data.tracks, 0)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFFF6600)
                                    ),
                                    shape = RoundedCornerShape(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Tout lire")
                                }
                            }
                        }

                        // Liste des pistes
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(data.tracks, key = { it.id }) { track ->
                                PlaylistTrackItem(
                                    track = track,
                                    onClick = {
                                        val trackIndex = data.tracks.indexOf(track)
                                        playerViewModel.setQueueAndPlay(data.tracks, trackIndex)
                                    },
                                    onRemove = {
                                        viewModel.removeTrackFromPlaylist(playlistId, track.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog pour ajouter des pistes
    if (showAddTracksDialog) {
        AddTracksDialog(
            tracks = allTracks,
            currentPlaylistTracks = playlistWithTracks?.tracks?.map { it.id } ?: emptyList(),
            onDismiss = { showAddTracksDialog = false },
            onAddTrack = { trackId ->
                viewModel.addTrackToPlaylist(playlistId, trackId)
            }
        )
    }
}

@Composable
fun PlaylistTrackItem(
    track: AudioTrack,
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
            containerColor = Color(0xFF1a1a1a).copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album art
            AsyncImage(
                model = albumArtUri,
                contentDescription = "Album art",
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(6.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Infos de la piste
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
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
                text = formatDuration(track.duration),
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Menu
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Retirer de la playlist", color = Color.Red) },
                        onClick = {
                            showMenu = false
                            onRemove()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AddTracksDialog(
    tracks: List<AudioTrack>,
    currentPlaylistTracks: List<Long>,
    onDismiss: () -> Unit,
    onAddTrack: (Long) -> Unit
) {
    // Filtrer les pistes qui ne sont pas déjà dans la playlist
    val availableTracks = tracks.filter { it.id !in currentPlaylistTracks }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajouter des chansons") },
        text = {
            if (availableTracks.isEmpty()) {
                Text("Toutes vos chansons sont déjà dans cette playlist")
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                ) {
                    items(availableTracks) { track ->
                        TrackSelectItem(
                            track = track,
                            onSelect = {
                                onAddTrack(track.id)
                                // Ne pas fermer le dialog pour permettre d'ajouter plusieurs pistes
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer", color = Color(0xFFFF6600))
            }
        }
    )
}

@Composable
fun TrackSelectItem(
    track: AudioTrack,
    onSelect: () -> Unit
) {
    val albumArtUri = ContentUris.withAppendedId(
        Uri.parse("content://media/external/audio/albumart"),
        track.albumId
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = albumArtUri,
            contentDescription = "Album art",
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(4.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = track.artist,
                fontSize = 12.sp,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Ajouter",
            tint = Color(0xFFFF6600)
        )
    }
}

private fun formatDuration(duration: Long): String {
    val seconds = (duration / 1000) % 60
    val minutes = (duration / 1000) / 60
    return String.format("%d:%02d", minutes, seconds)
}