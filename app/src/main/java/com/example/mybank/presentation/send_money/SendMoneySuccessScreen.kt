package com.example.mybank.presentation.send_money

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mybank.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SendMoneySuccessScreen(
    transactionId: String,
    viewModel: SendMoneyViewModel = hiltViewModel(),
    onDone: () -> Unit,
    onShareReceipt: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val transactionDetails = uiState.lastTransaction

    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
        viewModel.loadTransactionDetails(transactionId)
    }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.5f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "success_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(onClose = onDone)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 120.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    SuccessAnimation(scale = scale)
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Money Sent!",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        fontSize = 32.sp,
                        letterSpacing = (-0.5).sp
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    TransactionSummaryText(
                        amount = transactionDetails?.amount ?: 0.0,
                        recipientName = transactionDetails?.recipientName ?: "recipient"
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    ArrivalBadge()
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    TransactionDetailsCard(
                        transactionId = transactionId,
                        paymentMethod = "MyBank Card •• 4242",
                        date = transactionDetails?.timestamp ?: System.currentTimeMillis()
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    RecipientAvatar(
                        recipientName = transactionDetails?.recipientName ?: "Recipient"
                    )
                }
            }
        }

        ActionButtons(
            onDone = onDone,
            onShareReceipt = onShareReceipt,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun TopBar(onClose: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundDark.copy(alpha = 0.9f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onClose,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = TextWhite,
                modifier = Modifier.size(24.dp)
            )
        }

        Text(
            text = "Success",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextWhite,
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.size(40.dp))
    }
}

@Composable
private fun SuccessAnimation(scale: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(180.dp)
                .scale(scale)
                .blur(60.dp)
                .background(Color(0xFF13EC5B).copy(alpha = 0.3f), CircleShape)
        )

        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(Color(0xFF13EC5B).copy(alpha = 0.1f))
                .border(2.dp, Color(0xFF13EC5B).copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Success",
                tint = Color(0xFF13EC5B),
                modifier = Modifier.size(72.dp)
            )
        }
    }
}

@Composable
private fun TransactionSummaryText(
    amount: Double,
    recipientName: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "You sent ",
                style = MaterialTheme.typography.titleLarge,
                color = TextWhite.copy(alpha = 0.9f),
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = "$${String.format("%,.2f", amount)}",
                style = MaterialTheme.typography.titleLarge,
                color = TextWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = " to",
                style = MaterialTheme.typography.titleLarge,
                color = TextWhite.copy(alpha = 0.9f),
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        Text(
            text = recipientName,
            style = MaterialTheme.typography.titleLarge,
            color = TextWhite,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ArrivalBadge() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF13EC5B).copy(alpha = 0.1f))
            .border(1.dp, Color(0xFF13EC5B).copy(alpha = 0.2f), RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(Color(0xFF13EC5B).copy(alpha = alpha))
        )

        Text(
            text = "Arrives: Instantly",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF13EC5B),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun TransactionDetailsCard(
    transactionId: String,
    paymentMethod: String,
    date: Long
) {
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()) }
    val formattedDate = remember(date) { dateFormatter.format(Date(date)) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, BorderDarkSubtle, RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DetailRow(
                label = "Transaction ID",
                value = transactionId
            )

            Divider(color = BorderDarkSubtle, thickness = 1.dp)

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Payment Method",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGray500,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 24.dp, height = 16.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color(0xFF1152D4))
                        )
                        Text(
                            text = paymentMethod,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextWhite.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Divider(color = BorderDarkSubtle, thickness = 1.dp)

            DetailRow(
                label = "Date",
                value = formattedDate
            )
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextGray500,
            fontSize = 12.sp
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = TextWhite.copy(alpha = 0.8f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        )
    }
}

@Composable
private fun RecipientAvatar(recipientName: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(PrimaryBlue.copy(alpha = 0.2f))
                .border(2.dp, Color(0xFF13EC5B).copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = recipientName.take(2).uppercase(),
                color = TextWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ActionButtons(
    onDone: () -> Unit,
    onShareReceipt: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        BackgroundDark.copy(alpha = 0.95f),
                        BackgroundDark
                    )
                )
            )
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onDone,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF13EC5B)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Done",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BackgroundDark,
                    fontSize = 16.sp
                )
            }

            OutlinedButton(
                onClick = onShareReceipt,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = TextWhite
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Color.White.copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = TextWhite,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Share Receipt",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextWhite,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
