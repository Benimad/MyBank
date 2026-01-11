package com.example.mybank.presentation.account_details

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mybank.data.model.Account
import com.example.mybank.data.model.AccountType
import com.example.mybank.data.model.PotColorTag
import com.example.mybank.data.model.Transaction
import com.example.mybank.data.model.TransactionType
import com.example.mybank.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AccountDetailsScreen(
    accountId: String,
    viewModel: AccountDetailsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onTransactionClick: (String) -> Unit = {},
    onTransferMoney: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(accountId) {
        viewModel.loadAccount(accountId)
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
        } else if (uiState.account != null) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                item {
                    TopBar(
                        accountName = uiState.account?.accountName ?: "",
                        onNavigateBack = onNavigateBack
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    AccountInfoCard(
                        account = uiState.account!!,
                        onTransferClick = onTransferMoney
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    AccountStatsRow(
                        account = uiState.account!!,
                        totalIncome = uiState.totalIncome,
                        totalExpenses = uiState.totalExpenses
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Transaction History",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            fontSize = 18.sp
                        )
                        
                        Text(
                            text = "${uiState.transactions.size} total",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextGray500,
                            fontSize = 14.sp
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (uiState.transactions.isEmpty()) {
                    item {
                        EmptyTransactionsView()
                    }
                } else {
                    items(uiState.transactions) { transaction ->
                        TransactionItem(
                            transaction = transaction,
                            onClick = { onTransactionClick(transaction.id) }
                        )
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Account not found",
                    color = TextWhite,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun TopBar(
    accountName: String,
    onNavigateBack: () -> Unit
) {
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
            text = accountName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextWhite,
            fontSize = 18.sp
        )

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(SurfaceDark)
                .clickable(onClick = { }),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More",
                tint = TextWhite,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun AccountInfoCard(
    account: Account,
    onTransferClick: () -> Unit
) {
    val colorTag = getPotColor(account.potColorTag)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            PrimaryBlue.copy(alpha = 0.2f),
                            PrimaryBlue.copy(alpha = 0.05f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark.copy(alpha = 0.6f))
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "Current Balance",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextGray500,
                            fontSize = 13.sp
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = NumberFormat.getCurrencyInstance(Locale.US).format(account.balance),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            fontSize = 32.sp
                        )
                    }
                    
                    if (account.potColorTag != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(colorTag.copy(alpha = 0.2f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = account.accountType.name,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colorTag
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Account Number",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextGray500,
                            fontSize = 12.sp
                        )
                        Text(
                            text = account.accountNumber,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Currency",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextGray500,
                            fontSize = 12.sp
                        )
                        Text(
                            text = account.currency,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (account.accountType == AccountType.GOAL && account.goalAmount != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    GoalProgressSection(
                        currentAmount = account.balance,
                        goalAmount = account.goalAmount,
                        deadline = account.goalDeadline
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onTransferClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Transfer Money",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun GoalProgressSection(
    currentAmount: Double,
    goalAmount: Double,
    deadline: Long?
) {
    val progress = (currentAmount / goalAmount).toFloat().coerceIn(0f, 1f)
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Goal Progress",
                fontSize = 12.sp,
                color = TextGray500
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                fontSize = 12.sp,
                color = SuccessGreen,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = SuccessGreen,
            trackColor = SurfaceDark,
        )
        
        if (deadline != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Deadline: ${SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date(deadline))}",
                fontSize = 11.sp,
                color = TextGray500
            )
        }
    }
}

@Composable
private fun AccountStatsRow(
    account: Account,
    totalIncome: Double,
    totalExpenses: Double
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            title = "Income",
            amount = totalIncome,
            icon = Icons.Default.TrendingUp,
            color = SuccessGreen,
            modifier = Modifier.weight(1f)
        )
        
        StatCard(
            title = "Expenses",
            amount = totalExpenses,
            icon = Icons.Default.TrendingDown,
            color = ErrorRed,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    amount: Double,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .padding(16.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = TextGray500
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = NumberFormat.getCurrencyInstance(Locale.US).format(amount),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
        }
    }
}

@Composable
private fun TransactionItem(
    transaction: Transaction,
    onClick: () -> Unit
) {
    val isCredit = transaction.type == TransactionType.CREDIT
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.US)
    val timeFormatter = SimpleDateFormat("hh:mm a", Locale.US)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .shadow(2.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceDark)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
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
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = transaction.description,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextWhite
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "${dateFormatter.format(Date(transaction.timestamp))} • ${timeFormatter.format(Date(transaction.timestamp))}",
                        fontSize = 12.sp,
                        color = TextGray500
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isCredit) "+" else "-"}${NumberFormat.getCurrencyInstance(Locale.US).format(transaction.amount)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCredit) SuccessGreen else TextWhite
                )
                
                if (transaction.status != "COMPLETED") {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = transaction.status,
                        fontSize = 10.sp,
                        color = WarningOrange,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyTransactionsView() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Receipt,
                contentDescription = null,
                tint = TextGray500.copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "No transactions yet",
                fontSize = 16.sp,
                color = TextGray500,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Your transaction history will appear here",
                fontSize = 13.sp,
                color = TextGray500.copy(alpha = 0.7f)
            )
        }
    }
}

private fun getPotColor(tag: PotColorTag?): Color {
    return when (tag) {
        PotColorTag.BLUE -> Color(0xFF3B82F6)
        PotColorTag.EMERALD -> Color(0xFF10B981)
        PotColorTag.PURPLE -> Color(0xFF8B5CF6)
        PotColorTag.TEAL -> Color(0xFF14B8A6)
        PotColorTag.AMBER -> Color(0xFFF59E0B)
        PotColorTag.ORANGE -> Color(0xFFF97316)
        PotColorTag.PINK -> Color(0xFFEC4899)
        null -> Color(0xFF3B82F6)
    }
}
