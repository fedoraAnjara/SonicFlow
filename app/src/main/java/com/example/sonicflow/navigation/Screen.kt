package com.example.sonicflow.navigation
sealed class Screen(val route: String){
    object Splash : Screen("splash")
    object SignIn : Screen("sign_in")
    object SignUp : Screen("sign_up")
    object Home : Screen("home")
    object Player : Screen("player")
}