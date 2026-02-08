package com.example.sonicflow.presentation.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Playlists : Screen("playlists")
}