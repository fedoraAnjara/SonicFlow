package com.example.sonicflow.presentation.splash

import androidx.compose.runtime.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.example.sonicflow.R
import com.example.sonicflow.ui.AudiowideFont

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
){
    var startAnimation by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.4f,
        animationSpec = tween(
            durationMillis = 2500,
            easing = FastOutSlowInEasing
        )
    )

    val textSplash by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 2000,
            delayMillis = 1000,
            easing = LinearEasing
        )
    )

    val textBlur by animateFloatAsState(
        targetValue = if (startAnimation) 0f else 10f,
        animationSpec = tween(
            durationMillis = 2000,
            delayMillis = 1000,
            easing = LinearEasing
        )
    )

    val infiniteTransition = rememberInfiniteTransition(label = "gradient")
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient_offset"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(4000)
        onSplashFinished()
    }

    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF000000),
            Color(0xFF2a1500).copy(alpha = 0.3f + gradientOffset * 0.7f),
            Color(0xFFFF6600).copy(alpha = 0.6f + gradientOffset * 0.4f)
        ),
        startY = 0f,
        endY = 1000f*(1f + gradientOffset * 0.5f)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ){
        Image(
            painter = painterResource(id = R.drawable.logo1),
            contentDescription = "SonicFlow Logo",
            modifier = Modifier
                .size(280.dp)
                .scale(scale)
        )
        Spacer(modifier = Modifier.height(10.dp))

        Text(
               text = "SonicFloW",
               //fontFamily = AudiowideFont,
               fontStyle = FontStyle.Italic,
               fontSize = 42.sp,
               fontWeight = FontWeight.ExtraBold,
               color = Color.White,
               modifier = Modifier
                   .alpha(textSplash)
                   .blur(textBlur.dp)
            )
    }}
}