package com.example.sonicflow.presentation.home

import android.Manifest
import android.content.ContentUris
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.isGranted
import androidx.compose.ui.res.stringResource
import androidx.core.util.TimeUtils.formatDuration
import com.example.sonicflow.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import androidx.compose.material.icons.filled.Add
import com.example.sonicflow.domain.model.AudioTrack
import com.example.sonicflow.domain.repository.MusicPlayerRepository
import com.example.sonicflow.presentation.player.MiniPlayer
import com.example.sonicflow.presentation.player.PlayerViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel()
) {
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val permissionState = rememberPermissionState(permission)

    val state by viewModel.state.collectAsState()
    val playerState by playerViewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        if (!permissionState.status.isGranted) {
            permissionState.launchPermissionRequest()
        }
    }

    LaunchedEffect(permissionState.status.isGranted) {
        if (permissionState.status.isGranted) {
            viewModel.loadAudioTracks()
        }
    }

    Scaffold(
        bottomBar = {
            MiniPlayer(
                currentTrack = playerState.currentTrack,
                isPlaying = playerState.isPlaying,
                onPlayPauseClick = { playerViewModel.togglePlayPause() },
                onPlayerClick = { /* TODO: Ouvrir le lecteur complet */ },
                modifier = Modifier.navigationBarsPadding()
            )
        }
    ) { paddingValues ->
        AudioListContent(
            state = state,
            onRetry = { viewModel.loadAudioTracks() },
            hasPermission = permissionState.status.isGranted,
            onRequestPermission = { permissionState.launchPermissionRequest() },
            onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
            onSortTypeChange = { viewModel.onSortTypeChange(it) },
            onPlayTrack = { track -> playerViewModel.playTrack(track) },
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
fun AudioListContent(
    state: HomeState,
    onRetry: () -> Unit,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSortTypeChange: (SortType) -> Unit,
    onPlayTrack: (AudioTrack) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSortMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Text(
            text = stringResource(R.string.my_library),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp, 52.dp, 16.dp, 8.dp)
        )

        if (hasPermission && state.audioTracks.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    placeholder = {
                        Text(
                        text = stringResource(R.string.search_music),
                        style = MaterialTheme.typography.bodyMedium
                    ) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(R.string.search_music) ,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (state.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SortChip(
                        text = stringResource(R.string.sort_title_asc),
                        isSelected = state.sortType == SortType.TITLE_ASC,
                        onClick = { onSortTypeChange(SortType.TITLE_ASC) }
                    )

                    SortChip(
                        text = stringResource(R.string.sort_artist),
                        isSelected = state.sortType == SortType.ARTIST_ASC,
                        onClick = { onSortTypeChange(SortType.ARTIST_ASC) }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        when {
            !hasPermission -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.permission_required),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.permission_message),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = onRequestPermission) {
                        Text(stringResource(R.string.allow_access))
                    }
                }
            }
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            state.error != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(stringResource(R.string.error_label, state.error))
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onRetry) {
                        Text(stringResource(R.string.retry))
                    }
                }
            }
            state.filteredTracks.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (state.searchQuery.isEmpty()) {
                            stringResource(R.string.no_music_found)
                        } else {
                            stringResource(R.string.no_results)
                        }
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(state.filteredTracks) { track ->
                        AudioTrackItem(
                            track = track,
                            onPlayTrack = onPlayTrack
                            )
                    }
                }
            }
        }
    }
}

@Composable
fun AudioTrackItem(
    track: com.example.sonicflow.domain.model.AudioTrack,
    onPlayTrack: (AudioTrack) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    val albumArtUri = ContentUris.withAppendedId(
        Uri.parse("content://media/external/audio/albumart"),
        track.albumId
    )

    Column{
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable{onPlayTrack(track)},
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp,vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ){

            AsyncImage(
                model = albumArtUri,
                contentDescription = null,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop,
                error = painterResource(R.drawable.msc)
            )

            Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                maxLines = 1
            )
        }
        Box {
            IconButton(
                onClick = {showMenu = true}
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(R.string.more_option),
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }

            ) {
                DropdownMenuItem(
                    text = {Text(stringResource(R.string.add_to_playlist))},
                    onClick = {
                        showMenu = false
                        //TODO: ajouter a la playlist
                    }
                )
                DropdownMenuItem(
                    text = {Text(stringResource(R.string.share))},
                    onClick = {
                        showMenu = false
                        //TODO: partager
                    }
                )
                DropdownMenuItem(
                    text = {Text(stringResource(R.string.details))},
                    onClick = {
                        showMenu = false
                        //TODO: details
                    }
                )
                DropdownMenuItem(
                    text = {Text(stringResource(R.string.delete))},
                    onClick = {
                        showMenu = false
                        //TODO: supprimer
                    }
                )
            }
        }
    }}
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
    )
    }
}

fun formatDuration(milliseconds: Long): String {
    val totalSeconds = milliseconds / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}

@Composable
fun SortChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium
            )
        },
        leadingIcon = if (isSelected) {
            {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        } else null,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@Composable
fun PermissionDeniedContent(
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.permission_required),
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.permission_message),
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRequestPermission) {
            Text(stringResource(R.string.allow_access))
        }
    }
}