package com.example.mybank.presentation.transactions

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mybank.data.model.Transaction
import com.example.mybank.data.model.TransactionCategory
import com.example.mybank.data.model.TransactionType
import com.example.mybank.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

// Colors from HTML
private val BackgroundDark = Color(0xFF101622)
private val SurfaceDark = Color(0xFF1A2230)
private val PrimaryBlue = Color(0xFF1152D4)
private val SuccessGreen = Color(0xFF4ADE80)
private val DangerRed = Color(0xFFF87171)
private val TextWhite = Color(0xFFFFFFFF)
private val TextGray400 = Color(0xFF9CA3AF)
private val TextGray500 = Color(0xFF6B7280)
private val BorderGray = Color(0xFF374151)

enum class TransactionFilter(val displayName: String) {
    ALL("All"),
    INCOME("Income"),
    EXPENSES("Expenses"),
    PENDING("Pending")
}

@Composable
fun AllTransactionsScreen(
    viewModel: TransactionsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onTransactionClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedFilter by remember { mutableStateOf(TransactionFilter.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }

    // Get transactions with static data fallback
    val transactions = if (uiState.transactions.isEmpty()) {
        getStaticTransactions()
    } else {
        uiState.transactions
    }

    // Filter transactions
    val filteredTransactions = remember(transactions, selectedFilter, searchQuery) {
        transactions.filter { transaction ->
            val matchesFilter = when (selectedFilter) {
                TransactionFilter.ALL -> true
                TransactionFilter.INCOME -> transaction.type == TransactionType.CREDIT
                TransactionFilter.EXPENSES -> transaction.type == TransactionType.DEBIT
                TransactionFilter.PENDING -> transaction.status == "PENDING"
            }
            val matchesSearch = searchQuery.isEmpty() || 
                transaction.description.contains(searchQuery, ignoreCase = true)
            matchesFilter && matchesSearch
        }
    }

    // Group by date
    val groupedTransactions = remember(filteredTransactions) {
        filteredTransactions.groupBy { formatDateHeader(it.timestamp) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Header
            item {
                TransactionsHeader(
                    showSearch = showSearch,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onBackClick = onNavigateBack,
                    onSearchClick = { showSearch = !showSearch }
                )
            }

            // Filter Chips
            item {
                FilterChipsRow(
                    selectedFilter = selectedFilter,
                    onFilterSelected = { selectedFilter = it }
                )
            }

            // Transactions grouped by date
            groupedTransactions.forEach { (dateHeader, transactionsForDate) ->
                item {
                    DateHeaderSection(dateHeader)
                }

                items(transactionsForDate) { transaction ->
                    TransactionItemCard(
                        transaction = transaction,
                        onClick = { onTransactionClick(transaction.id) }
                    )
                }
            }

            // Loading indicator
            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = TextGray400,
                            strokeWidth = 2.dp
                        )
                    }
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = { /* Download/Export */ },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .size(56.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = CircleShape,
                    spotColor = PrimaryBlue.copy(alpha = 0.4f)
                ),
            containerColor = PrimaryBlue,
            contentColor = Color.White
        ) {
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = "Download",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun TransactionsHeader(
    showSearch: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundDark.copy(alpha = 0.95f))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = TextWhite,
                    modifier = Modifier.size(28.dp)
                )
            }

            if (!showSearch) {
                Text(
                    text = "Transactions",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextWhite,
                    letterSpacing = (-0.25).sp
                )
            }

            IconButton(
                onClick = onSearchClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = if (showSearch) Icons.Default.Close else Icons.Default.Search,
                    contentDescription = "Search",
                    tint = TextWhite,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = showSearch,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            TextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                placeholder = {
                    Text(
                        text = "Search transactions...",
                        color = TextGray400,
                        fontSize = 15.sp
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    cursorColor = PrimaryBlue,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }
    }
}

@Composable
private fun FilterChipsRow(
    selectedFilter: TransactionFilter,
    onFilterSelected: (TransactionFilter) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(TransactionFilter.entries) { filter ->
            FilterChip(
                filter = filter,
                isSelected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) }
            )
        }
    }
}

@Composable
private fun FilterChip(
    filter: TransactionFilter,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.95f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Surface(
        modifier = Modifier
            .scale(scale)
            .height(36.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = if (isSelected) PrimaryBlue else SurfaceDark,
        shadowElevation = if (isSelected) 8.dp else 0.dp,
        border = if (!isSelected) androidx.compose.foundation.BorderStroke(
            1.dp,
            BorderGray
        ) else null
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = filter.displayName,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                color = if (isSelected) TextWhite else TextGray400
            )
        }
    }
}

@Composable
private fun DateHeaderSection(dateHeader: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundDark.copy(alpha = 0.95f))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = dateHeader.uppercase(),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextGray400,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun TransactionItemCard(
    transaction: Transaction,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.99f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
    }

    val (icon, iconBg, iconTint) = getTransactionIconData(transaction)
    val isIncome = transaction.type == TransactionType.CREDIT
    val amountColor = if (isIncome) SuccessGreen else TextWhite

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .scale(scale)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = Color.Black.copy(alpha = 0.1f)
            )
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceDark)
            .border(1.dp, BorderGray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .clickable(
                onClick = {
                    isPressed = true
                    onClick()
                },
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
        }

        // Details
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = transaction.description,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Text(
                    text = "${if (isIncome) "+" else "-"}$${String.format("%.2f", transaction.amount)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = amountColor
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = transaction.category.name.lowercase()
                        .replaceFirstChar { it.uppercase() }
                        .replace("_", " "),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextGray400,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatTime(transaction.timestamp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextGray500
                )
            }
        }
    }
}

private fun getTransactionIconData(transaction: Transaction): Triple<ImageVector, Color, Color> {
    return when (transaction.category) {
        TransactionCategory.FOOD -> Triple(
            Icons.Default.LocalCafe,
            Color(0xFFFB923C).copy(alpha = 0.1f),
            Color(0xFFFB923C)
        )
        TransactionCategory.SALARY, TransactionCategory.DEPOSIT -> Triple(
            Icons.Default.AccountBalance,
            Color(0xFF4ADE80).copy(alpha = 0.1f),
            Color(0xFF4ADE80)
        )
        TransactionCategory.SHOPPING -> Triple(
            Icons.Default.Devices,
            Color(0xFF6B7280).copy(alpha = 0.2f),
            Color(0xFF9CA3AF)
        )
        TransactionCategory.BILL -> Triple(
            Icons.Default.Movie,
            Color(0xFFF87171).copy(alpha = 0.1f),
            Color(0xFFF87171)
        )
        TransactionCategory.TRANSPORT -> Triple(
            Icons.Default.DirectionsCar,
            Color(0xFF6B7280).copy(alpha = 0.1f),
            Color(0xFFFFFFFF)
        )
        TransactionCategory.TRANSFER -> Triple(
            Icons.Default.SwapHoriz,
            PrimaryBlue.copy(alpha = 0.2f),
            Color(0xFF60A5FA)
        )
        else -> Triple(
            Icons.Default.ShoppingCart,
            Color(0xFF10B981).copy(alpha = 0.1f),
            Color(0xFF10B981)
        )
    }
}

private fun formatDateHeader(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val days = diff / (1000 * 60 * 60 * 24)

    return when {
        days == 0L -> "Today"
        days == 1L -> "Yesterday"
        else -> SimpleDateFormat("MMMM dd", Locale.getDefault()).format(Date(timestamp))
    }
}

private fun formatTime(timestamp: Long): String {
    return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timestamp))
}

private fun getStaticTransactions(): List<Transaction> {
    val now = System.currentTimeMillis()
    return listOf(
        Transaction(
            id = "1",
            accountId = "acc1",
            type = TransactionType.DEBIT,
            category = TransactionCategory.FOOD,
            amount = 5.40,
            description = "Starbucks",
            timestamp = now - 3600000,
            status = "COMPLETED"
        ),
        Transaction(
            id = "2",
            accountId = "acc1",
            type = TransactionType.CREDIT,
            category = TransactionCategory.SALARY,
            amount = 3200.00,
            description = "Salary Transfer",
            timestamp = now - 7200000,
            status = "COMPLETED"
        ),
        Transaction(
            id = "3",
            accountId = "acc1",
            type = TransactionType.DEBIT,
            category = TransactionCategory.SHOPPING,
            amount = 1099.00,
            description = "Apple Store",
            timestamp = now - 10800000,
            status = "COMPLETED"
        ),
        Transaction(
            id = "4",
            accountId = "acc1",
            type = TransactionType.DEBIT,
            category = TransactionCategory.BILL,
            amount = 15.99,
            description = "Netflix Subscription",
            timestamp = now - 86400000,
            status = "COMPLETED"
        ),
        Transaction(
            id = "5",
            accountId = "acc1",
            type = TransactionType.DEBIT,
            category = TransactionCategory.TRANSPORT,
            amount = 24.50,
            description = "Uber Ride",
            timestamp = now - 86400000 - 3600000,
            status = "COMPLETED"
        ),
        Transaction(
            id = "6",
            accountId = "acc1",
            type = TransactionType.DEBIT,
            category = TransactionCategory.SHOPPING,
            amount = 124.32,
            description = "Whole Foods Market",
            timestamp = now - 86400000 - 7200000,
            status = "COMPLETED"
        ),
        Transaction(
            id = "7",
            accountId = "acc1",
            type = TransactionType.CREDIT,
            category = TransactionCategory.TRANSFER,
            amount = 50.00,
            description = "Alice Smith",
            timestamp = now - 172800000,
            status = "COMPLETED"
        )
    )
}
