package com.example.mybank.presentation.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mybank.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    isAuthenticated: Boolean,
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
        delay(2500)
        if (isAuthenticated) {
            onNavigateToHome()
        } else {
            onNavigateToLogin()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        PrimaryBlue,
                        PrimaryBlueDark
                    )
                )
            )
    ) {
        // Ambient Background Effects
        AnimatedAmbientBlobs()

        // Main Content
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.weight(1f))
            SplashBrandElement(isVisible)
            Spacer(modifier = Modifier.weight(1f))
            SplashFooter(isVisible)
        }
    }
}

@Composable
private fun AnimatedAmbientBlobs() {
    val infiniteTransition = rememberInfiniteTransition(label = "ambient")

    val blob1Scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob1"
    )

    val blob2Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 50f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob2"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .offset(x = (-128).dp, y = (-128).dp)
                .size(384.dp)
                .scale(blob1Scale)
                .blur(100.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            PrimaryBlue.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = blob2Offset.dp)
                .size(256.dp)
                .blur(80.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 128.dp, y = 128.dp)
                .size(384.dp)
                .blur(80.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            PrimaryBlueDark,
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )
    }
}

@Composable
private fun SplashBrandElement(isVisible: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val logoScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(1000)) + scaleIn(
                initialScale = 0.8f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(96.dp)
                    .scale(logoScale)
                    .background(Color.White, RoundedCornerShape(24.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalance,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(56.dp)
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp)
                        .size(24.dp)
                        .background(PrimaryBlueDark, CircleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(EmeraldGreen, CircleShape)
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(800, delayMillis = 300))
        ) {
            Text(
                text = "MyBank",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = (-0.5).sp
            )
        }

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(800, delayMillis = 500))
        ) {
            Text(
                text = "Banking Reimagined",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun SplashFooter(isVisible: Boolean) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(800, delayMillis = 700)) + slideInVertically { 30 },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 48.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                color = Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(50.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Color.White.copy(alpha = 0.05f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = EmeraldGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Secure Mobile Banking",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Text(
                text = "V2.4.0 • BUILD 892",
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.4f),
                textAlign = TextAlign.Center,
                letterSpacing = 1.5.sp
            )
        }
    }
}