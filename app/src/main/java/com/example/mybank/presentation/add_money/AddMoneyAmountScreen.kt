package com.example.mybank.presentation.add_money

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mybank.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMoneyAmountScreen(
    onNavigateBack: () -> Unit,
    onContinue: (String) -> Unit,
    viewModel: AddMoneyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var amount by remember { mutableStateOf("") }
    var displayAmount by remember { mutableStateOf("0.00") }

    fun updateAmount(value: String) {
        amount = value
        val numericValue = if (value.isEmpty()) 0.0 else value.toDoubleOrNull() ?: 0.0
        displayAmount = String.format("%.2f", numericValue)
        viewModel.setAmount(value)
    }

    fun handleNumberClick(number: String) {
        if (number == "." && amount.contains(".")) return
        if (amount.count { it == '.' } == 1 && amount.substringAfter(".").length >= 2) return
        
        updateAmount(amount + number)
    }

    fun handleBackspace() {
        if (amount.isNotEmpty()) {
            updateAmount(amount.dropLast(1))
        }
    }

    val animatedAmount by animateFloatAsState(
        targetValue = if (amount.isNotEmpty()) 1f else 0.9f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "amount_scale"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Add Money",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BackgroundDark,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = BackgroundDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "How much would you like to add?",
                fontSize = 16.sp,
                color = TextGray400,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                Text(
                    text = "$",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = displayAmount,
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.graphicsLayer(
                        scaleX = animatedAmount,
                        scaleY = animatedAmount
                    )
                )
            }

            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (amount.isNotEmpty()) PrimaryBlue else Color.Transparent)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                QuickAmountChip(
                    amount = "100",
                    onClick = { updateAmount("100") },
                    modifier = Modifier.weight(1f)
                )
                QuickAmountChip(
                    amount = "500",
                    onClick = { updateAmount("500") },
                    modifier = Modifier.weight(1f),
                    isSelected = amount == "500"
                )
                QuickAmountChip(
                    amount = "1,000",
                    onClick = { updateAmount("1000") },
                    modifier = Modifier.weight(1f)
                )
                QuickAmountChip(
                    amount = "2,000",
                    onClick = { updateAmount("2000") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = SurfaceDark
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "TO ACCOUNT",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextGray400,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "MyBank Checking",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Deposit to account ending in 1234",
                            fontSize = 14.sp,
                            color = TextGray400
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PrimaryBlue.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            NumberPad(
                onNumberClick = { handleNumberClick(it) },
                onBackspace = { handleBackspace() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onContinue(amount) },
                enabled = amount.isNotEmpty() && amount.toDoubleOrNull() ?: 0.0 > 0.0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    disabledContainerColor = PrimaryBlue.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Continue",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun QuickAmountChip(
    amount: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) PrimaryBlue.copy(alpha = 0.2f)
                else SurfaceDark
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "+$$amount",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isSelected) PrimaryBlue else Color.White
        )
    }
}

@Composable
fun NumberPad(
    onNumberClick: (String) -> Unit,
    onBackspace: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NumberButton("1", Modifier.weight(1f)) { onNumberClick("1") }
            NumberButton("2", Modifier.weight(1f)) { onNumberClick("2") }
            NumberButton("3", Modifier.weight(1f)) { onNumberClick("3") }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NumberButton("4", Modifier.weight(1f)) { onNumberClick("4") }
            NumberButton("5", Modifier.weight(1f)) { onNumberClick("5") }
            NumberButton("6", Modifier.weight(1f)) { onNumberClick("6") }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NumberButton("7", Modifier.weight(1f)) { onNumberClick("7") }
            NumberButton("8", Modifier.weight(1f)) { onNumberClick("8") }
            NumberButton("9", Modifier.weight(1f)) { onNumberClick("9") }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NumberButton(".", Modifier.weight(1f)) { onNumberClick(".") }
            NumberButton("0", Modifier.weight(1f)) { onNumberClick("0") }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onBackspace),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Backspace,
                    contentDescription = "Backspace",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun NumberButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}
