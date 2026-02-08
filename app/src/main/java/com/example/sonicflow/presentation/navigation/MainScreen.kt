package com.example.sonicflow.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.sonicflow.presentation.home.HomeScreen
import com.example.sonicflow.presentation.playlist.PlaylistsScreen
import com.example.sonicflow.presentation.player.PlayerViewModel
import com.example.sonicflow.presentation.player.MiniPlayer

sealed class BottomNavScreen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomNavScreen(
        route = "home_tab",
        title = "Accueil",
        icon = Icons.Default.Home
    )
    object Playlists : BottomNavScreen(
        route = "playlists_tab",
        title = "Playlists",
        icon = Icons.Default.List
    )
}

@Composable
fun MainScreen(
    mainNavController: NavHostController,
    playerViewModel: PlayerViewModel = hiltViewModel()
) {
    val bottomNavController = rememberNavController()
    val playerState by playerViewModel.state.collectAsState()

    Scaffold(
        bottomBar = {
            // Column pour empiler MiniPlayer + BottomNav
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                // MiniPlayer (si une piste joue)
                if (playerState.currentTrack != null) {
                    MiniPlayer(
                        currentTrack = playerState.currentTrack,
                        isPlaying = playerState.isPlaying,
                        onPlayPauseClick = { playerViewModel.togglePlayPause() },
                        onNextClick = { playerViewModel.playNext() },
                        onPlayerClick = {
                            mainNavController.navigate(Screen.Player.route)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Bottom Navigation Bar
                BottomNavigationBar(navController = bottomNavController)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = bottomNavController,
            startDestination = BottomNavScreen.Home.route,
            modifier = Modifier
                .padding(paddingValues)
        ) {
            composable(BottomNavScreen.Home.route) {
                HomeScreen(navController = mainNavController)
            }
            composable(BottomNavScreen.Playlists.route) {
                PlaylistsScreen()
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        BottomNavScreen.Home,
        BottomNavScreen.Playlists
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Même dégradé que le MiniPlayer
    val gradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF2D1302).copy(alpha = 0.95f),
            Color.Black.copy(alpha = 0.95f)
        )
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        tonalElevation = 0.dp,
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any {
                        it.route == screen.route
                    } == true

                    BottomNavItem(
                        screen = screen,
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavItem(
    screen: BottomNavScreen,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = screen.icon,
            contentDescription = screen.title,
            tint = if (selected) Color(0xFFFF9800) else Color.White.copy(alpha = 0.5f),
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = screen.title,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) Color(0xFFFF9800) else Color.White.copy(alpha = 0.5f)
        )

        // Indicateur sous l'icône sélectionnée
        if (selected) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .width(32.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(Color(0xFFFF9800))
            )
        }
    }
}