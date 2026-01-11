package com.example.mybank.presentation.transfers

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

private val PurplePrimary = Color(0xFF8B7FFF)
private val TextPrimary = Color(0xFF1A1A2E)
private val TextSecondary = Color(0xFF9CA3AF)
private val CardBgWhite = Color(0xFFFAFAFC)
private val SuccessGreen = Color(0xFF00C566)
private val ErrorRed = Color(0xFFDC2626)

private val BackgroundGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFE8E0FF),
        Color(0xFFF0ECFF),
        Color(0xFFF8F6FF),
        Color(0xFFFFFEFF)
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun P2PTransferScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: P2PTransferViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    var recipientInput by remember { mutableStateOf("") }
    var amountInput by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var transactionId by remember { mutableStateOf("") }
    
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "P2P Transfer",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundGradient)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (uiState.senderAccounts.isNotEmpty()) {
                    Text(
                        "From Account",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardBgWhite),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                uiState.selectedAccount?.accountName ?: "Select Account",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextPrimary
                            )
                            Text(
                                "Balance: $${String.format("%.2f", uiState.selectedAccount?.balance ?: 0.0)}",
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
                
                Text(
                    "Recipient",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                
                OutlinedTextField(
                    value = recipientInput,
                    onValueChange = { 
                        recipientInput = it
                        viewModel.updateRecipientIdentifier(it)
                    },
                    label = { Text("Email or Phone") },
                    placeholder = { Text("Enter recipient email or phone") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PurplePrimary,
                        unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f)
                    ),
                    trailingIcon = {
                        if (uiState.searchingRecipient) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            IconButton(onClick = { viewModel.searchRecipient() }) {
                                Icon(Icons.Default.Search, "Search")
                            }
                        }
                    }
                )
                
                if (uiState.recipientUser != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    uiState.recipientUser?.name ?: "",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    uiState.recipientUser?.email ?: "",
                                    fontSize = 14.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
                
                Text(
                    "Amount",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { 
                        amountInput = it
                        it.toDoubleOrNull()?.let { amount -> viewModel.updateAmount(amount) }
                    },
                    label = { Text("Amount") },
                    placeholder = { Text("0.00") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    leadingIcon = { Text("$", fontSize = 18.sp, color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PurplePrimary,
                        unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f)
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        viewModel.initiateP2PTransfer { txId ->
                            transactionId = txId
                            showSuccessDialog = true
                            recipientInput = ""
                            amountInput = ""
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = uiState.canTransfer && !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PurplePrimary,
                        disabledContainerColor = TextSecondary.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "Send Money",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
    
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { 
                showSuccessDialog = false
                onNavigateBack()
            },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    "Transfer Successful!",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Transaction ID: $transactionId")
            },
            confirmButton = {
                Button(
                    onClick = { 
                        showSuccessDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                ) {
                    Text("Done")
                }
            }
        )
    }
}
