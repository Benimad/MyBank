package com.example.mybank.presentation.add_money

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.VerifiedUser
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mybank.ui.theme.*
import kotlin.random.Random

@Composable
private fun ConfettiParticle(
    modifier: Modifier = Modifier,
    delay: Int = 0
) {
    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000 + delay, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "confetti_y"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500 + delay, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "confetti_alpha"
    )

    Box(
        modifier = modifier
            .offset(y = offsetY.dp)
            .background(
                PrimaryGreen.copy(alpha = alpha),
                RoundedCornerShape(2.dp)
            )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMoneySuccessScreen(
    transactionId: String,
    amount: String,
    onDone: () -> Unit,
    onAddMore: () -> Unit,
    viewModel: AddMoneyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val displayAmount = amount.toDoubleOrNull()?.let { String.format("%.2f", it) } ?: "0.00"
    val newBalance = uiState.currentBalance + (amount.toDoubleOrNull() ?: 0.0)

    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "success_scale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    LaunchedEffect(Unit) {
        viewModel.loadUserData()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                IconButton(
                    onClick = onDone,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
                
                Text(
                    text = "MyBank",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .padding(top = 48.dp, bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(0.5f))

                Box(
                    modifier = Modifier.scale(scale),
                    contentAlignment = Alignment.Center
                ) {
                    ConfettiParticle(
                        modifier = Modifier
                            .size(8.dp, 8.dp)
                            .offset(x = (-20).dp, y = 0.dp)
                            .rotate(45f),
                        delay = 0
                    )
                    
                    ConfettiParticle(
                        modifier = Modifier
                            .size(12.dp, 4.dp)
                            .offset(x = 30.dp, y = 40.dp)
                            .rotate(12f),
                        delay = 300
                    )
                    
                    ConfettiParticle(
                        modifier = Modifier
                            .size(8.dp, 8.dp)
                            .offset(x = (-40).dp, y = 80.dp)
                            .rotate(90f),
                        delay = 600
                    )

                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .clip(CircleShape)
                            .background(PrimaryGreen.copy(alpha = glowAlpha * 0.5f))
                    )

                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(PrimaryGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = BackgroundDark,
                            modifier = Modifier.size(72.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Money Added!",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = buildString {
                        append("You successfully added ")
                        append("$")
                        append(displayAmount)
                        append(" to your account.")
                    },
                    fontSize = 16.sp,
                    color = TextGray400,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = 280.dp),
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(40.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = SurfaceDark
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Color.White.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "NEW BALANCE",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextGray400,
                                letterSpacing = 1.2.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$${String.format("%,.2f", newBalance)}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                lineHeight = 32.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(64.dp)
                                .height(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            PrimaryGreen.copy(alpha = 0.3f),
                                            PrimaryGreen.copy(alpha = 0.1f)
                                        )
                                    )
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = onDone,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGreen
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Done",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = BackgroundDark,
                        letterSpacing = 0.15.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onAddMore,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PrimaryGreen
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        2.dp,
                        PrimaryGreen.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Add More Money",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.15.sp
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.VerifiedUser,
                        contentDescription = null,
                        tint = TextGray500,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Transaction secure and encrypted",
                        fontSize = 12.sp,
                        color = TextGray500,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .align(Alignment.BottomCenter)
                .background(BackgroundDark),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .width(128.dp)
                    .height(4.dp)
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.2f))
            )
        }
    }
}
