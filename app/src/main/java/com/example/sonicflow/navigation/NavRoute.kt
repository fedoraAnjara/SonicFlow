package com.example.sonicflow.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.sonicflow.presentation.home.HomeScreen
import com.example.sonicflow.presentation.signin.SignInScreen
import com.example.sonicflow.presentation.signup.SignUpScreen
import com.example.sonicflow.presentation.splash.SplashScreen

@Composable
fun NavRoute(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route,
){
    NavHost(
        navController = navController,
        startDestination = startDestination
    ){
        composable(route = Screen.Splash.route){
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        composable(route = Screen.SignIn.route){
            SignInScreen(
                onNavigateToSignUp = {
                    navController.navigate(Screen.SignUp.route) },
                onSignInSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.SignIn.route) {inclusive = true}
                }}
            )
        }
        composable(route = Screen.SignUp.route){
            SignUpScreen(
                onNavigateToSignIn = {
                    navController.popBackStack()
                },
                onSignUpSuccess = {
                    navController.navigate(Screen.Home.route){
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                    }
                }
            )
        }
        composable(route = Screen.Home.route){
            HomeScreen()
        }
    }

}