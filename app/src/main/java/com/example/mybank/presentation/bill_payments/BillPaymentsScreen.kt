package com.example.mybank.presentation.bill_payments

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mybank.data.model.Biller
import com.example.mybank.data.model.BillerCategory
import com.example.mybank.ui.theme.InterFontFamily
import com.example.mybank.ui.theme.PrimaryBlue
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BillPaymentsScreen(
    onNavigateBack: () -> Unit = {},
    onAddBiller: () -> Unit = {},
    onPayBill: (String) -> Unit = {},
    viewModel: BillPaymentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
                        text = "Bill Payments",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = InterFontFamily
                    )
                }
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAddBiller() },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF1E293B)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryBlue.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add Biller",
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = "Add New Biller",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = InterFontFamily
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    if (uiState.billers.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No billers added yet",
                                    color = Color(0xFF64748B),
                                    fontSize = 16.sp,
                                    fontFamily = InterFontFamily
                                )
                            }
                        }
                    } else {
                        item {
                            Text(
                                text = "Your Billers",
                                color = Color(0xFF94A3B8),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = InterFontFamily,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }

                        items(uiState.billers) { biller ->
                            BillerItem(
                                biller = biller,
                                onClick = { onPayBill(biller.id) },
                                onDelete = { viewModel.deleteBiller(biller.id) }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
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

@Composable
fun BillerItem(
    biller: Biller,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
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
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(getCategoryColor(biller.category).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = biller.billerName.take(1).uppercase(),
                    color = getCategoryColor(biller.category),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = InterFontFamily
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = biller.billerName,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = InterFontFamily
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = biller.category.name.replace("_", " "),
                    color = Color(0xFF64748B),
                    fontSize = 13.sp,
                    fontFamily = InterFontFamily
                )
            }
            TextButton(onClick = { showDeleteDialog = true }) {
                Text(
                    text = "Delete",
                    color = Color(0xFFEF4444),
                    fontSize = 14.sp,
                    fontFamily = InterFontFamily
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text("Delete Biller", fontFamily = InterFontFamily)
            },
            text = {
                Text(
                    "Are you sure you want to delete ${biller.billerName}?",
                    fontFamily = InterFontFamily
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = Color(0xFFEF4444), fontFamily = InterFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", fontFamily = InterFontFamily)
                }
            }
        )
    }
}

fun getCategoryColor(category: BillerCategory): Color {
    return when (category) {
        BillerCategory.UTILITIES -> Color(0xFFF59E0B)
        BillerCategory.TELECOM -> Color(0xFF8B5CF6)
        BillerCategory.INSURANCE -> Color(0xFF10B981)
        BillerCategory.SUBSCRIPTION -> Color(0xFF3B82F6)
        BillerCategory.LOAN -> Color(0xFFEF4444)
        BillerCategory.CREDIT_CARD -> Color(0xFFEC4899)
        BillerCategory.EDUCATION -> Color(0xFF06B6D4)
        BillerCategory.HEALTHCARE -> Color(0xFF14B8A6)
        BillerCategory.GOVERNMENT -> Color(0xFF6366F1)
        BillerCategory.OTHER -> Color(0xFF64748B)
    }
}
