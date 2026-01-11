package com.example.mybank.presentation.savings_goals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mybank.data.model.Account
import com.example.mybank.ui.theme.InterFontFamily
import com.example.mybank.ui.theme.PrimaryBlue

@Composable
fun AutomateSavingsScreen(
    goalAccountId: String,
    onNavigateBack: () -> Unit = {},
    viewModel: SavingsGoalsViewModel = hiltViewModel()
) {
    var amount by remember { mutableStateOf("") }
    var selectedSourceAccount by remember { mutableStateOf<Account?>(null) }
    var showSourceAccountMenu by remember { mutableStateOf(false) }
    var selectedFrequency by remember { mutableStateOf(AutomationFrequency.WEEKLY) }
    var showFrequencyMenu by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()
    val goalAccount = remember(uiState.goalAccounts, goalAccountId) {
        uiState.goalAccounts.find { it.id == goalAccountId }
    }

    LaunchedEffect(goalAccount) {
        goalAccount?.let {
            viewModel.selectGoalAccount(it)
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            viewModel.clearSuccessMessage()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Automate Savings",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = InterFontFamily
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                if (goalAccount != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1E293B)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(
                                        PrimaryBlue.copy(alpha = 0.2f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = goalAccount.accountName,
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = InterFontFamily
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Current: $${String.format("%.2f", goalAccount.balance)}",
                                    color = Color(0xFF64748B),
                                    fontSize = 14.sp,
                                    fontFamily = InterFontFamily
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                Text(
                    text = "Transfer Amount",
                    color = Color(0xFF94A3B8),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = InterFontFamily
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "0.00",
                            color = Color(0xFF64748B),
                            fontFamily = InterFontFamily
                        )
                    },
                    leadingIcon = {
                        Text(
                            "$",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = InterFontFamily
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedContainerColor = Color(0xFF1E293B),
                        unfocusedContainerColor = Color(0xFF1E293B)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "From Account",
                    color = Color(0xFF94A3B8),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = InterFontFamily
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E293B))
                        .border(
                            width = 1.dp,
                            color = Color(0xFF334155),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { showSourceAccountMenu = true }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedSourceAccount?.accountName ?: "Select source account",
                            color = if (selectedSourceAccount != null) Color.White else Color(0xFF64748B),
                            fontSize = 16.sp,
                            fontFamily = InterFontFamily
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Dropdown",
                            tint = Color(0xFF64748B)
                        )
                    }
                }

                if (showSourceAccountMenu && uiState.sourceAccounts.isNotEmpty()) {
                    DropdownMenu(
                        expanded = showSourceAccountMenu,
                        onDismissRequest = { showSourceAccountMenu = false },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .background(Color(0xFF1E293B))
                    ) {
                        uiState.sourceAccounts.forEach { account ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            account.accountName,
                                            color = Color.White,
                                            fontFamily = InterFontFamily
                                        )
                                        Text(
                                            "Balance: $${String.format("%.2f", account.balance)}",
                                            color = Color(0xFF64748B),
                                            fontSize = 12.sp,
                                            fontFamily = InterFontFamily
                                        )
                                    }
                                },
                                onClick = {
                                    selectedSourceAccount = account
                                    showSourceAccountMenu = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Frequency",
                    color = Color(0xFF94A3B8),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = InterFontFamily
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E293B))
                        .border(
                            width = 1.dp,
                            color = Color(0xFF334155),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { showFrequencyMenu = true }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedFrequency.name.replace("_", " "),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontFamily = InterFontFamily
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Dropdown",
                            tint = Color(0xFF64748B)
                        )
                    }
                }

                if (showFrequencyMenu) {
                    DropdownMenu(
                        expanded = showFrequencyMenu,
                        onDismissRequest = { showFrequencyMenu = false },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .background(Color(0xFF1E293B))
                    ) {
                        AutomationFrequency.values().forEach { frequency ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        frequency.name.replace("_", " "),
                                        color = Color.White,
                                        fontFamily = InterFontFamily
                                    )
                                },
                                onClick = {
                                    selectedFrequency = frequency
                                    showFrequencyMenu = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = PrimaryBlue.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Automation Summary",
                            color = PrimaryBlue,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = InterFontFamily
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        if (selectedSourceAccount != null && amount.toDoubleOrNull() != null) {
                            Text(
                                text = "Transfer $${amount} from ${selectedSourceAccount!!.accountName} to ${goalAccount?.accountName ?: "goal"} every ${selectedFrequency.name.lowercase().replace("_", " ")}",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontFamily = InterFontFamily
                            )
                        } else {
                            Text(
                                text = "Fill in all fields to see automation summary",
                                color = Color(0xFF64748B),
                                fontSize = 13.sp,
                                fontFamily = InterFontFamily
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        val amountValue = amount.toDoubleOrNull() ?: 0.0
                        if (amountValue > 0 && selectedSourceAccount != null) {
                            viewModel.setAutomationAmount(amountValue)
                            viewModel.selectSourceAccount(selectedSourceAccount!!)
                            viewModel.setAutomationFrequency(selectedFrequency)
                            viewModel.toggleAutomation(true)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        disabledContainerColor = PrimaryBlue.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !uiState.isLoading && 
                            (amount.toDoubleOrNull() ?: 0.0) > 0 && selectedSourceAccount != null
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = "Enable Automation",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = InterFontFamily
                        )
                    }
                }

                if (uiState.isAutomationEnabled) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            viewModel.toggleAutomation(false)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEF4444)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Disable Automation",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = InterFontFamily
                        )
                    }
                }
            }
        }

        uiState.error?.let { error ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                containerColor = Color(0xFFEF4444)
            ) {
                Text(error, color = Color.White)
            }
        }

        uiState.successMessage?.let { message ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                containerColor = Color(0xFF10B981)
            ) {
                Text(message, color = Color.White)
            }
        }
    }
}
