package com.example.mybank.presentation.internal_transfer

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mybank.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun InternalTransferSuccessScreen(
    transactionId: String,
    onDone: () -> Unit,
    onTransferAgain: () -> Unit = {}
) {
    var showConfetti by remember { mutableStateOf(true) }
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        delay(3000)
        showConfetti = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        if (showConfetti) {
            ConfettiAnimation()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                SuccessGreen.copy(alpha = 0.3f),
                                SuccessGreen.copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        )
                    )
                    .border(
                        width = 3.dp,
                        color = SuccessGreen,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = SuccessGreen,
                    modifier = Modifier.size(80.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Transfer Complete!",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                fontSize = 28.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Your funds have been transferred instantly between your MyBank accounts.",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Normal,
                color = TextGray400,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Transaction ID: ${transactionId.takeLast(8).uppercase()}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = TextGray600,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = onDone,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SuccessGreen,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Done",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onTransferAgain,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = PrimaryBlue
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    width = 2.dp,
                    brush = androidx.compose.ui.graphics.SolidColor(PrimaryBlue)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Repeat,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Transfer Again",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ConfettiAnimation() {
    val confettiPieces = remember {
        List(50) {
            ConfettiPiece(
                x = Random.nextFloat(),
                startY = Random.nextFloat() * -0.3f,
                color = listOf(
                    PrimaryBlue,
                    SuccessGreen,
                    WarningYellow,
                    Color(0xFFEC4899),
                    Color(0xFF8B5CF6)
                ).random(),
                size = Random.nextInt(8, 16).dp,
                rotation = Random.nextFloat() * 360f,
                duration = Random.nextInt(2000, 4000)
            )
        }
    }

    confettiPieces.forEach { piece ->
        ConfettiPieceAnimation(piece)
    }
}

data class ConfettiPiece(
    val x: Float,
    val startY: Float,
    val color: Color,
    val size: androidx.compose.ui.unit.Dp,
    val rotation: Float,
    val duration: Int
)

@Composable
private fun ConfettiPieceAnimation(piece: ConfettiPiece) {
    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    
    val yOffset by infiniteTransition.animateFloat(
        initialValue = piece.startY,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(piece.duration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "y"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = piece.rotation,
        targetValue = piece.rotation + 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(piece.duration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .offset(
                    x = (piece.x * 300).dp,
                    y = (yOffset * 800).dp
                )
                .size(piece.size)
                .rotate(rotation)
                .clip(RoundedCornerShape(2.dp))
                .background(piece.color)
        )
    }
}
