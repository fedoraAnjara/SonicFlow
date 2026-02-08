package com.example.sonicflow.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object SignIn : Screen("signin")
    object SignUp : Screen("signup")
    object Home : Screen("home")
    object Playlists : Screen("playlists")
    object Player : Screen("player")
}