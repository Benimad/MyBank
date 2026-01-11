package com.example.mybank.presentation.send_money

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mybank.ui.theme.*

@Composable
fun SendMoneyConfirmScreen(
    recipientId: String,
    amount: String,
    viewModel: SendMoneyViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToSuccess: (transactionId: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var note by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(recipientId) {
        viewModel.loadRecipientDetails(recipientId)
    }

    val recipient = uiState.selectedRecipient
    val amountDouble = amount.toDoubleOrNull() ?: 0.0

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp)
        ) {
            item {
                TopBar(onNavigateBack = onNavigateBack)
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                RecipientSection(
                    recipientName = recipient?.name ?: "Loading...",
                    accountNumber = recipient?.accountNumber ?: "••••",
                    isRevolutUser = recipient?.isRevolutUser ?: false
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                AmountDisplaySection(amount = amountDouble)
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                TransactionDetailsSection(
                    fromAccount = "Personal Checking (...1234)",
                    fee = 0.0,
                    totalAmount = amountDouble
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                NoteSection(
                    note = note,
                    onNoteChange = { note = it }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                SecurityBadge()
            }
        }

        SendButton(
            amount = amountDouble,
            isLoading = isLoading,
            onSend = {
                isLoading = true
                viewModel.sendMoney(
                    recipientId = recipientId,
                    amount = amountDouble,
                    note = note.ifEmpty { null },
                    onSuccess = { transactionId ->
                        isLoading = false
                        onNavigateToSuccess(transactionId)
                    },
                    onError = {
                        isLoading = false
                    }
                )
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun TopBar(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundDark.copy(alpha = 0.9f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = TextWhite,
                modifier = Modifier.size(24.dp)
            )
        }

        Text(
            text = "Confirm Transfer",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextWhite,
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.size(40.dp))
    }
}

@Composable
private fun RecipientSection(
    recipientName: String,
    accountNumber: String,
    isRevolutUser: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(PrimaryBlue.copy(alpha = 0.2f))
                .border(4.dp, PrimaryBlue.copy(alpha = 0.2f), CircleShape)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = recipientName.take(2).uppercase(),
                    color = TextWhite,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (isRevolutUser) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(PrimaryBlue)
                        .border(2.dp, BackgroundDark, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verified",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = recipientName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextWhite,
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Account •••• $accountNumber",
            style = MaterialTheme.typography.bodyMedium,
            color = TextGray400,
            fontSize = 14.sp
        )

        if (isRevolutUser) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(PrimaryBlue.copy(alpha = 0.1f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Revolut User",
                    style = MaterialTheme.typography.bodySmall,
                    color = PrimaryBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun AmountDisplaySection(amount: Double) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "amount_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark.copy(alpha = 0.5f))
            .border(1.dp, BorderDark, RoundedCornerShape(16.dp))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Total Amount",
                style = MaterialTheme.typography.bodySmall,
                color = TextGray400,
                fontSize = 14.sp
            )

            Text(
                text = "$${String.format("%,.2f", amount)}",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                fontSize = 40.sp,
                letterSpacing = (-0.5).sp,
                modifier = Modifier.scale(scale)
            )
        }
    }
}

@Composable
private fun TransactionDetailsSection(
    fromAccount: String,
    fee: Double,
    totalAmount: Double
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "TRANSACTION DETAILS",
            style = MaterialTheme.typography.bodySmall,
            color = TextGray500,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .border(1.dp, BorderDarkSubtle, RoundedCornerShape(16.dp))
        ) {
            Column {
                DetailRow(
                    icon = Icons.Default.AccountBalanceWallet,
                    iconTint = TextGray400,
                    iconBg = SurfaceDark,
                    label = "From Account",
                    value = fromAccount,
                    showDivider = true
                )

                DetailRow(
                    icon = Icons.Default.Payments,
                    iconTint = SuccessGreen,
                    iconBg = SuccessGreen.copy(alpha = 0.1f),
                    label = "Transfer Fee",
                    value = "Free ($${String.format("%.2f", fee)})",
                    valueColor = SuccessGreen,
                    showDivider = true
                )

                DetailRow(
                    icon = Icons.Default.Schedule,
                    iconTint = TextGray400,
                    iconBg = SurfaceDark,
                    label = "Estimated Arrival",
                    value = "Instant (within seconds)",
                    showDivider = false
                )
            }
        }
    }
}

@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    iconBg: Color,
    label: String,
    value: String,
    valueColor: Color = TextWhite,
    showDivider: Boolean
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray500,
                    fontSize = 12.sp
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = valueColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (showDivider) {
            Divider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = BorderDarkSubtle,
                thickness = 1.dp
            )
        }
    }
}

@Composable
private fun NoteSection(
    note: String,
    onNoteChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "MESSAGE (OPTIONAL)",
            style = MaterialTheme.typography.bodySmall,
            color = TextGray500,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .border(1.dp, BorderDarkSubtle, RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.EditNote,
                contentDescription = "Note",
                tint = TextGray400,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            BasicTextField(
                value = note,
                onValueChange = onNoteChange,
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = TextWhite,
                    fontWeight = FontWeight.Normal
                ),
                cursorBrush = SolidColor(PrimaryBlue),
                decorationBox = { innerTextField ->
                    if (note.isEmpty()) {
                        Text(
                            text = "Add a note for recipient...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextGray500,
                            fontSize = 16.sp
                        )
                    }
                    innerTextField()
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SecurityBadge() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Secure",
            tint = TextGray500,
            modifier = Modifier.size(18.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "Secure Encrypted Transaction",
            style = MaterialTheme.typography.bodySmall,
            color = TextGray500,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SendButton(
    amount: Double,
    isLoading: Boolean,
    onSend: () -> Unit,
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
        Column {
            Button(
                onClick = onSend,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Send $${String.format("%.2f", amount)} Now",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )

                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "By tapping Send, you authorize this transfer and agree to our Terms of Service.",
                style = MaterialTheme.typography.bodySmall,
                color = TextGray600,
                fontSize = 10.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
