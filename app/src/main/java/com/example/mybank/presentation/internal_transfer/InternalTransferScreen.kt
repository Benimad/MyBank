package com.example.mybank.presentation.internal_transfer

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mybank.data.model.Account
import com.example.mybank.data.model.AccountType
import com.example.mybank.ui.theme.*

@Composable
fun InternalTransferScreen(
    viewModel: InternalTransferViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToSuccess: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var amount by remember { mutableStateOf("0.00") }
    var showAccountSelector by remember { mutableStateOf(false) }
    var selectingFromAccount by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TopAppBar(onNavigateBack = onNavigateBack)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                FromAccountSection(
                    account = uiState.fromAccount,
                    onClick = {
                        selectingFromAccount = true
                        showAccountSelector = true
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                SwapButton(
                    onSwap = {
                        viewModel.swapAccounts()
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                ToAccountSection(
                    account = uiState.toAccount,
                    onClick = {
                        selectingFromAccount = false
                        showAccountSelector = true
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                AmountSection(
                    amount = amount,
                    onAmountChange = { newAmount ->
                        amount = newAmount
                        viewModel.updateAmount(newAmount.toDoubleOrNull() ?: 0.0)
                    },
                    onQuickAmountClick = { quickAmount ->
                        when (quickAmount) {
                            "Max" -> {
                                amount = String.format("%.2f", uiState.fromAccount?.balance ?: 0.0)
                                viewModel.updateAmount(uiState.fromAccount?.balance ?: 0.0)
                            }
                            else -> {
                                val value = quickAmount.removePrefix("$").toDouble()
                                amount = String.format("%.2f", value)
                                viewModel.updateAmount(value)
                            }
                        }
                    }
                )
            }

            BottomActionBar(
                isEnabled = uiState.canTransfer,
                onTransferClick = {
                    viewModel.initiateTransfer {
                        onNavigateToSuccess(it)
                    }
                }
            )
        }

        if (showAccountSelector) {
            AccountSelectorBottomSheet(
                accounts = uiState.accounts,
                selectedAccountId = if (selectingFromAccount) uiState.fromAccount?.id else uiState.toAccount?.id,
                onAccountSelected = { account ->
                    if (selectingFromAccount) {
                        viewModel.selectFromAccount(account)
                    } else {
                        viewModel.selectToAccount(account)
                    }
                    showAccountSelector = false
                },
                onDismiss = { showAccountSelector = false }
            )
        }
    }
}

@Composable
private fun TopAppBar(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundDark.copy(alpha = 0.8f))
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(SurfaceDark)
                .clickable(onClick = onNavigateBack),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = TextWhite,
                modifier = Modifier.size(24.dp)
            )
        }

        Text(
            text = "Internal Transfer",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextWhite,
            fontSize = 18.sp
        )

        Box(modifier = Modifier.size(40.dp))
    }
}

@Composable
private fun FromAccountSection(
    account: Account?,
    onClick: () -> Unit
) {
    Column {
        Text(
            text = "FROM",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = TextGray500,
            fontSize = 12.sp,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        AccountCard(
            account = account,
            labelColor = PrimaryBlue,
            label = account?.accountType?.name ?: "CHECKING ACCOUNT",
            onClick = onClick
        )
    }
}

@Composable
private fun ToAccountSection(
    account: Account?,
    onClick: () -> Unit
) {
    Column {
        Text(
            text = "TO",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = TextGray500,
            fontSize = 12.sp,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        AccountCard(
            account = account,
            labelColor = SuccessGreen,
            label = account?.accountType?.name ?: "SAVINGS ACCOUNT",
            onClick = onClick
        )
    }
}

@Composable
private fun AccountCard(
    account: Account?,
    labelColor: Color,
    label: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceDark)
            .border(
                width = 1.dp,
                color = BorderDark,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = labelColor,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = account?.accountName ?: "Select Account",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    fontSize = 18.sp
                )
                Text(
                    text = "•••• ${account?.accountNumber?.takeLast(4) ?: "----"}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Normal,
                    color = TextGray500,
                    fontSize = 14.sp
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (account?.accountType == AccountType.CHECKING) "AVAILABLE" else "BALANCE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextGray400,
                    fontSize = 10.sp,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "$${String.format("%,.2f", account?.balance ?: 0.0)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    fontSize = 16.sp
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(4.dp)
                .align(Alignment.CenterEnd)
                .background(PrimaryBlue.copy(alpha = 0f))
        )
    }
}

@Composable
private fun SwapButton(onSwap: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    ambientColor = PrimaryBlue.copy(alpha = glowAlpha),
                    spotColor = PrimaryBlue.copy(alpha = glowAlpha)
                )
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            PrimaryBlue,
                            PrimaryBlueDark
                        )
                    )
                )
                .border(
                    width = 4.dp,
                    color = BackgroundDark,
                    shape = CircleShape
                )
                .clickable(onClick = onSwap),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = "Swap accounts",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun AmountSection(
    amount: String,
    onAmountChange: (String) -> Unit,
    onQuickAmountClick: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "ENTER AMOUNT",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = TextGray500,
            fontSize = 12.sp,
            letterSpacing = 2.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Light,
                color = TextGray400,
                fontSize = 48.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            BasicTextField(
                value = amount,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                        onAmountChange(newValue.ifEmpty { "0.00" })
                    }
                },
                textStyle = TextStyle(
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    textAlign = TextAlign.Center
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                cursorBrush = SolidColor(PrimaryBlue),
                singleLine = true,
                modifier = Modifier.width(250.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
        ) {
            QuickAmountChip(label = "$50", onClick = { onQuickAmountClick("$50") })
            QuickAmountChip(label = "$100", onClick = { onQuickAmountClick("$100") })
            QuickAmountChip(label = "$500", onClick = { onQuickAmountClick("$500") })
            QuickAmountChip(label = "Max", onClick = { onQuickAmountClick("Max") })
        }
    }
}

@Composable
private fun QuickAmountChip(label: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF374151))
            .border(
                width = 1.dp,
                color = Color.Transparent,
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = TextWhite,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun BottomActionBar(
    isEnabled: Boolean,
    onTransferClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundDark)
            .border(
                width = 1.dp,
                color = BorderDarkSubtle,
                shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Secure",
                tint = TextGray500,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Instant transfer between your MyBank accounts.",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Normal,
                color = TextGray500,
                fontSize = 12.sp
            )
        }

        Button(
            onClick = onTransferClick,
            enabled = isEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(
                    elevation = if (isEnabled) 16.dp else 0.dp,
                    shape = RoundedCornerShape(12.dp),
                    ambientColor = if (isEnabled) PrimaryBlue.copy(alpha = 0.3f) else Color.Transparent
                ),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryBlue,
                contentColor = Color.White,
                disabledContainerColor = SurfaceDark,
                disabledContentColor = TextGray500
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Transfer Funds",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
