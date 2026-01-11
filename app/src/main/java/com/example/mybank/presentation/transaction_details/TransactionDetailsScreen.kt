package com.example.mybank.presentation.transaction_details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mybank.data.model.Transaction
import com.example.mybank.data.model.TransactionType
import com.example.mybank.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TransactionDetailsScreen(
    transactionId: String,
    viewModel: TransactionDetailsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onDisputeTransaction: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(transactionId) {
        viewModel.loadTransaction(transactionId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = PrimaryBlue
            )
        } else if (uiState.transaction != null) {
            val transaction = uiState.transaction!!
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                item {
                    TopBar(onNavigateBack = onNavigateBack)
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    TransactionHeader(transaction = transaction)
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    TransactionDetailsCard(transaction = transaction)
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    ActionButtons(
                        transaction = transaction,
                        onDisputeClick = onDisputeTransaction,
                        onDownloadReceipt = { viewModel.downloadReceipt() },
                        onShareClick = { viewModel.shareTransaction() }
                    )
                }

                if (transaction.status != "COMPLETED") {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    item {
                        StatusAlert(status = transaction.status)
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = ErrorRed,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Transaction not found",
                        color = TextWhite,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun TopBar(onNavigateBack: () -> Unit) {
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
            text = "Transaction Details",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextWhite,
            fontSize = 18.sp
        )

        Box(modifier = Modifier.size(40.dp))
    }
}

@Composable
private fun TransactionHeader(transaction: Transaction) {
    val isCredit = transaction.type == TransactionType.CREDIT
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    if (isCredit) SuccessGreen.copy(alpha = 0.2f)
                    else ErrorRed.copy(alpha = 0.2f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isCredit) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                contentDescription = null,
                tint = if (isCredit) SuccessGreen else ErrorRed,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = transaction.description,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextWhite,
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${if (isCredit) "+" else "-"}${NumberFormat.getCurrencyInstance(Locale.US).format(transaction.amount)}",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = if (isCredit) SuccessGreen else TextWhite,
            fontSize = 36.sp
        )
    }
}

@Composable
private fun TransactionDetailsCard(transaction: Transaction) {
    val dateFormatter = SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm a", Locale.US)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .padding(20.dp)
    ) {
        Column {
            Text(
                text = "Transaction Information",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )

            Spacer(modifier = Modifier.height(20.dp))

            DetailRow(label = "Transaction ID", value = transaction.id.take(16) + "...")
            Spacer(modifier = Modifier.height(12.dp))
            
            DetailRow(label = "Date & Time", value = dateFormatter.format(Date(transaction.timestamp)))
            Spacer(modifier = Modifier.height(12.dp))
            
            DetailRow(label = "Type", value = transaction.type.name)
            Spacer(modifier = Modifier.height(12.dp))
            
            DetailRow(label = "Category", value = transaction.category.name.lowercase().capitalize())
            Spacer(modifier = Modifier.height(12.dp))
            
            DetailRow(label = "Status", value = transaction.status)
            
            if (transaction.recipientName != null) {
                Spacer(modifier = Modifier.height(12.dp))
                DetailRow(label = "Recipient", value = transaction.recipientName)
            }
            
            if (transaction.recipientAccount != null) {
                Spacer(modifier = Modifier.height(12.dp))
                DetailRow(label = "Recipient Account", value = transaction.recipientAccount)
            }
            
            if (transaction.balanceAfter != null) {
                Spacer(modifier = Modifier.height(12.dp))
                DetailRow(
                    label = "Balance After",
                    value = NumberFormat.getCurrencyInstance(Locale.US).format(transaction.balanceAfter)
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = TextGray500
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = TextWhite,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ActionButtons(
    transaction: Transaction,
    onDisputeClick: () -> Unit,
    onDownloadReceipt: () -> Unit,
    onShareClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDownloadReceipt,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = PrimaryBlue
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Receipt", fontSize = 14.sp)
            }

            OutlinedButton(
                onClick = onShareClick,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = PrimaryBlue
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share", fontSize = 14.sp)
            }
        }

        if (transaction.status == "COMPLETED" && transaction.type == TransactionType.DEBIT) {
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedButton(
                onClick = onDisputeClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = ErrorRed
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Report,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Dispute Transaction", fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun StatusAlert(status: String) {
    val (color, icon, message) = when (status) {
        "PENDING" -> Triple(WarningOrange, Icons.Default.Schedule, "This transaction is pending")
        "PROCESSING" -> Triple(PrimaryBlue, Icons.Default.Sync, "This transaction is being processed")
        "FAILED" -> Triple(ErrorRed, Icons.Default.Error, "This transaction failed")
        "DISPUTED" -> Triple(WarningOrange, Icons.Default.Report, "This transaction is under dispute")
        else -> Triple(TextGray500, Icons.Default.Info, "Status: $status")
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                fontSize = 14.sp,
                color = color,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
