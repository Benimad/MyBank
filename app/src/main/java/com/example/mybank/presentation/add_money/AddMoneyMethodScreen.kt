package com.example.mybank.presentation.add_money

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mybank.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMoneyMethodScreen(
    amount: String,
    onNavigateBack: () -> Unit,
    onNavigateToCardEntry: () -> Unit,
    onNavigateToConfirm: (String) -> Unit,
    viewModel: AddMoneyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val defaultMethods = listOf(
        PaymentMethod(
            id = "visa_4242",
            type = PaymentMethodType.DEBIT_CARD,
            name = "Visa ending in 4242",
            lastFour = "4242",
            fee = 0.0,
            arrivalTime = "Instant"
        ),
        PaymentMethod(
            id = "chase_9801",
            type = PaymentMethodType.BANK_ACCOUNT,
            name = "Chase Bank (ACH)",
            lastFour = "9801",
            fee = 0.0,
            arrivalTime = "1-3 business days"
        ),
        PaymentMethod(
            id = "apple_pay",
            type = PaymentMethodType.APPLE_PAY,
            name = "Apple Pay",
            lastFour = "",
            fee = 0.0,
            arrivalTime = "Instant"
        )
    )

    val displayAmount = amount.toDoubleOrNull()?.let { String.format("%.2f", it) } ?: "0.00"

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
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Help,
                            contentDescription = "Help",
                            tint = Color.White
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Amount to be added",
                        fontSize = 14.sp,
                        color = TextGray400,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$$displayAmount",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(SuccessGreen.copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Secure Transaction",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SuccessGreen
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "SELECT PAYMENT METHOD",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextGray400,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            items(defaultMethods) { method ->
                PaymentMethodCard(
                    method = method,
                    onClick = {
                        viewModel.selectPaymentMethod(method)
                        onNavigateToConfirm(method.id)
                    }
                )
            }

            item {
                AddNewMethodCard(onClick = onNavigateToCardEntry)
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = TextGray500,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Secure, encrypted transactions by MyBank",
                        fontSize = 12.sp,
                        color = TextGray500
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun PaymentMethodCard(
    method: PaymentMethod,
    onClick: () -> Unit
) {
    val icon = when (method.type) {
        PaymentMethodType.DEBIT_CARD, PaymentMethodType.CREDIT_CARD -> Icons.Default.CreditCard
        PaymentMethodType.BANK_ACCOUNT -> Icons.Default.AccountBalance
        PaymentMethodType.APPLE_PAY -> Icons.Default.AccountBalanceWallet
        PaymentMethodType.GOOGLE_PAY -> Icons.Default.AccountBalanceWallet
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(BorderDark),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = method.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (method.type) {
                                PaymentMethodType.DEBIT_CARD -> "Debit Card"
                                PaymentMethodType.CREDIT_CARD -> "Credit Card"
                                PaymentMethodType.BANK_ACCOUNT -> "Bank Transfer •••• ${method.lastFour}"
                                PaymentMethodType.APPLE_PAY -> "Fast and secure checkout"
                                PaymentMethodType.GOOGLE_PAY -> "Fast and secure checkout"
                            },
                            fontSize = 14.sp,
                            color = TextGray400
                        )
                    }
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextGray400
            )
        }
    }
}

@Composable
fun AddNewMethodCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 2.dp,
            color = BorderDark
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.AddCircle,
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Add new payment method",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryBlue
            )
        }
    }
}
