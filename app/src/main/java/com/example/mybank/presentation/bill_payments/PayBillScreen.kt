package com.example.mybank.presentation.bill_payments

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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mybank.data.model.Account
import com.example.mybank.data.model.RecurrenceFrequency
import com.example.mybank.presentation.accounts.AccountsViewModel
import com.example.mybank.ui.theme.InterFontFamily
import com.example.mybank.ui.theme.PrimaryBlue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun PayBillScreen(
    billerId: String,
    onNavigateBack: () -> Unit = {},
    viewModel: BillPaymentViewModel = hiltViewModel(),
    accountsViewModel: AccountsViewModel = viewModel()
) {
    var amount by remember { mutableStateOf("") }
    var selectedAccount by remember { mutableStateOf<Account?>(null) }
    var showAccountMenu by remember { mutableStateOf(false) }
    var isRecurring by remember { mutableStateOf(false) }
    var selectedFrequency by remember { mutableStateOf(RecurrenceFrequency.MONTHLY) }
    var showFrequencyMenu by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()
    val accountsState by accountsViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(billerId) {
        scope.launch {
            val biller = viewModel.uiState.value.billers.find { it.id == billerId }
            if (biller != null) {
                viewModel.selectBiller(biller)
            }
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
                        text = "Pay Bill",
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
                if (uiState.selectedBiller != null) {
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
                                        getCategoryColor(uiState.selectedBiller!!.category).copy(
                                            alpha = 0.2f
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = uiState.selectedBiller!!.billerName.take(1).uppercase(),
                                    color = getCategoryColor(uiState.selectedBiller!!.category),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = InterFontFamily
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = uiState.selectedBiller!!.billerName,
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = InterFontFamily
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = uiState.selectedBiller!!.category.name.replace("_", " "),
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
                    text = "Amount",
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
                        .clickable { showAccountMenu = true }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedAccount?.accountName ?: "Select account",
                            color = if (selectedAccount != null) Color.White else Color(0xFF64748B),
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

                if (showAccountMenu && accountsState.accounts.isNotEmpty()) {
                    DropdownMenu(
                        expanded = showAccountMenu,
                        onDismissRequest = { showAccountMenu = false },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .background(Color(0xFF1E293B))
                    ) {
                        accountsState.accounts.forEach { account ->
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
                                    selectedAccount = account
                                    showAccountMenu = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Set as Recurring",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = InterFontFamily
                    )
                    Switch(
                        checked = isRecurring,
                        onCheckedChange = { isRecurring = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PrimaryBlue,
                            uncheckedThumbColor = Color(0xFF64748B),
                            uncheckedTrackColor = Color(0xFF334155)
                        )
                    )
                }

                if (isRecurring) {
                    Spacer(modifier = Modifier.height(16.dp))
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
                            RecurrenceFrequency.values()
                                .filter { it != RecurrenceFrequency.NONE }
                                .forEach { frequency ->
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
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        val amountValue = amount.toDoubleOrNull() ?: 0.0
                        if (amountValue > 0 && selectedAccount != null) {
                            viewModel.setAmount(amountValue)
                            viewModel.setAccountId(selectedAccount!!.id)
                            viewModel.setIsRecurring(isRecurring)
                            if (isRecurring) {
                                viewModel.setRecurrenceFrequency(selectedFrequency)
                            }
                            viewModel.payBill { onNavigateBack() }
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
                            (amount.toDoubleOrNull() ?: 0.0) > 0 && selectedAccount != null
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = "Pay Bill",
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
