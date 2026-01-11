package com.example.mybank.presentation.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
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
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    hazeState: HazeState,
    onNavigateToTransactions: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToCards: () -> Unit = {},
    onNavigateToStats: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToSendMoney: () -> Unit = {},
    onNavigateToAddMoney: () -> Unit = {},
    onNavigateToInternalTransfer: () -> Unit = {},
    onNavigateToAccounts: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var isBalanceVisible by remember { mutableStateOf(true) }
    var showMoreMenu by remember { mutableStateOf(false) }
    
    val listState = rememberLazyListState()
    val scrollOffset = remember { derivedStateOf { listState.firstVisibleItemScrollOffset.toFloat() } }
    val parallaxOffset = remember { derivedStateOf { min(scrollOffset.value * 0.3f, 200f) } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GlassBackgroundGradient)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 90.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                TopAppBar(
                    userName = uiState.userName.ifEmpty { "Alex Johnson" },
                    profileImageUrl = uiState.profileImageUrl,
                    hasUnreadNotifications = true,
                    onNotificationClick = onNavigateToNotifications
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                GlassBalanceCard(
                    balance = uiState.totalBalance,
                    accountName = "MyBank Checking",
                    accountNumber = if (uiState.accounts.isNotEmpty()) 
                        uiState.accounts[0].accountNumber.takeLast(4) 
                    else "5245",
                    isVisible = isBalanceVisible,
                    onVisibilityToggle = { isBalanceVisible = !isBalanceVisible },
                    parallaxOffset = parallaxOffset.value
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                QuickActionsGrid(
                    onSendClick = onNavigateToSendMoney,
                    onAddMoneyClick = onNavigateToAddMoney,
                    onBillPayClick = { },
                    onMoreClick = { showMoreMenu = !showMoreMenu },
                    showMoreMenu = showMoreMenu,
                    onInternalTransferClick = {
                        showMoreMenu = false
                        onNavigateToInternalTransfer()
                    },
                    onAccountsClick = {
                        showMoreMenu = false
                        onNavigateToAccounts()
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                GlassSavingsGoalCard(progress = 0.8f)
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Activity",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        fontSize = 20.sp
                    )
                    TextButton(onClick = onNavigateToTransactions) {
                        Text(
                            text = "See all",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryBlueGlow,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            val transactions = if (uiState.recentTransactions.isEmpty()) 
                getDefaultTransactions() 
            else 
                uiState.recentTransactions.take(4)
                
            items(transactions) { transaction ->
                GlassTransactionItem(
                    transaction = transaction,
                    onClick = { }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun TopAppBar(
    userName: String,
    profileImageUrl: String?,
    hasUnreadNotifications: Boolean,
    onNotificationClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(44.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(PrimaryBlue, PrimaryBlueDark)
                            )
                        )
                        .border(2.dp, PrimaryBlueGlow.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userName.take(2).uppercase(),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(SuccessGreen)
                        .border(2.dp, BackgroundDark, CircleShape)
                )
            }

            Column {
                Text(
                    text = "Welcome back,",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = TextGray400,
                    fontSize = 12.sp
                )
                Text(
                    text = userName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    fontSize = 15.sp
                )
            }
        }

        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(GlassWhite)
                .border(1.dp, GlassWhiteBorder, CircleShape)
                .clickable(onClick = onNotificationClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = "Notifications",
                tint = TextGray300,
                modifier = Modifier.size(22.dp)
            )
            if (hasUnreadNotifications) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = (-8).dp, y = 8.dp)
                        .clip(CircleShape)
                        .background(AlertRed)
                )
            }
        }
    }
}

@Composable
private fun GlassBalanceCard(
    balance: Double,
    accountName: String,
    accountNumber: String,
    isVisible: Boolean,
    onVisibilityToggle: () -> Unit,
    parallaxOffset: Float
) {
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.98f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .scale(scale)
            .graphicsLayer {
                translationY = -parallaxOffset * 0.2f
            }
    ) {
        Box(
            modifier = Modifier
                .offset(x = 260.dp, y = (-50).dp)
                .size(180.dp)
                .blur(100.dp)
                .background(PrimaryBlue.copy(alpha = 0.15f), CircleShape)
        )
        Box(
            modifier = Modifier
                .offset(x = (-30).dp, y = 70.dp)
                .size(120.dp)
                .blur(80.dp)
                .background(Color(0xFF60A5FA).copy(alpha = 0.2f), CircleShape)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(24.dp),
                    ambientColor = PrimaryBlue.copy(alpha = 0.15f),
                    spotColor = PrimaryBlue.copy(alpha = 0.15f)
                )
                .clip(RoundedCornerShape(24.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            PrimaryBlue,
                            PrimaryBlueDark
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "Total Balance",
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFBFDBFE),
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AnimatedContent(
                                targetState = isVisible,
                                transitionSpec = {
                                    (fadeIn(animationSpec = tween(300)) + 
                                     scaleIn(initialScale = 0.92f, animationSpec = tween(300))) togetherWith
                                    (fadeOut(animationSpec = tween(300)) + 
                                     scaleOut(targetScale = 0.92f, animationSpec = tween(300)))
                                },
                                label = "balance"
                            ) { visible ->
                                Text(
                                    text = if (visible) "$${String.format("%,.2f", balance)}" else "••••••",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 36.sp,
                                    letterSpacing = (-0.5).sp
                                )
                            }
                            IconButton(
                                onClick = onVisibilityToggle,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (isVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = if (isVisible) "Hide balance" else "Show balance",
                                    tint = Color(0xFFBFDBFE),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Contactless,
                            contentDescription = "Contactless",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = accountName,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFBFDBFE),
                            fontSize = 12.sp
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "•••• $accountNumber",
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                fontWeight = FontWeight.Medium,
                                color = Color.White,
                                fontSize = 14.sp,
                                letterSpacing = 1.sp
                            )
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy account number",
                                tint = Color(0xFFBFDBFE),
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { }
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add money",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionsGrid(
    onSendClick: () -> Unit = {},
    onAddMoneyClick: () -> Unit = {},
    onBillPayClick: () -> Unit = {},
    onMoreClick: () -> Unit = {},
    showMoreMenu: Boolean = false,
    onInternalTransferClick: () -> Unit = {},
    onAccountsClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            GlassQuickActionButton(
                icon = Icons.Outlined.Send,
                label = "Send",
                modifier = Modifier.weight(1f),
                onClick = onSendClick
            )
            GlassQuickActionButton(
                icon = Icons.Outlined.Add,
                label = "Add Money",
                modifier = Modifier.weight(1f),
                onClick = onAddMoneyClick
            )
            GlassQuickActionButton(
                icon = Icons.Outlined.Receipt,
                label = "Bill Pay",
                modifier = Modifier.weight(1f),
                onClick = onBillPayClick
            )
            GlassQuickActionButton(
                icon = Icons.Outlined.MoreHoriz,
                label = "More",
                modifier = Modifier.weight(1f),
                onClick = onMoreClick,
                isActive = showMoreMenu
            )
        }
        
        AnimatedVisibility(
            visible = showMoreMenu,
            enter = expandVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ) + fadeIn(
                animationSpec = tween(300)
            ),
            exit = shrinkVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ) + fadeOut(
                animationSpec = tween(200)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                GlassQuickActionButton(
                    icon = Icons.Outlined.SwapHoriz,
                    label = "Transfer",
                    modifier = Modifier.weight(1f),
                    onClick = onInternalTransferClick
                )
                GlassQuickActionButton(
                    icon = Icons.Outlined.AccountBalance,
                    label = "Accounts",
                    modifier = Modifier.weight(1f),
                    onClick = onAccountsClick
                )
                Spacer(modifier = Modifier.weight(2f))
            }
        }
    }
}

@Composable
private fun GlassQuickActionButton(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    isActive: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    val rotation by animateFloatAsState(
        targetValue = if (isActive) 180f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "rotation"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .shadow(
                    elevation = if (isActive) 4.dp else 1.dp,
                    shape = RoundedCornerShape(18.dp),
                    ambientColor = if (isActive) PrimaryBlue.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.05f)
                )
                .clip(RoundedCornerShape(18.dp))
                .background(
                    if (isActive) PrimaryBlue else GlassCardBg
                )
                .border(
                    width = 1.dp,
                    color = if (isActive) PrimaryBlueLight.copy(alpha = 0.4f) else GlassCardBorder,
                    shape = RoundedCornerShape(18.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) Color.White else PrimaryBlueGlow,
                modifier = Modifier
                    .size(26.dp)
                    .graphicsLayer { rotationZ = rotation }
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
            color = if (isActive) PrimaryBlueGlow else TextGray300,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun GlassSavingsGoalCard(progress: Float) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = GlassShadow
            )
            .clip(RoundedCornerShape(20.dp))
            .background(GlassCardBg)
            .border(
                width = 1.dp,
                color = GlassCardBorder,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Savings Goal Reached!",
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                fontSize = 15.sp
            )
            Text(
                text = "You've hit ${(progress * 100).toInt()}% of your vacation fund. Keep it up!",
                fontWeight = FontWeight.Normal,
                color = TextGray400,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(WarningYellow.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🏆",
                fontSize = 24.sp
            )
        }
    }
}

@Composable
private fun GlassTransactionItem(
    transaction: Transaction,
    onClick: () -> Unit
) {
    val amountColor = if (transaction.type == TransactionType.CREDIT) SuccessGreen else TextWhite
    val amountPrefix = if (transaction.type == TransactionType.CREDIT) "+ " else "- "

    val (iconBgColor, iconTintColor, icon) = when (transaction.category) {
        TransactionCategory.FOOD -> Triple(
            RedIconBgDark,
            RedIconTintDark,
            Icons.Outlined.Restaurant
        )
        TransactionCategory.SHOPPING -> Triple(
            BlueIconBgDark,
            BlueIconTintDark,
            Icons.Outlined.ShoppingBag
        )
        TransactionCategory.TRANSPORT -> Triple(
            OrangeIconBgDark,
            OrangeIconTintDark,
            Icons.Outlined.DirectionsCar
        )
        TransactionCategory.SALARY -> Triple(
            GreenIconBgDark,
            GreenIconTintDark,
            Icons.Outlined.AccountBalanceWallet
        )
        TransactionCategory.PAYMENT, TransactionCategory.BILL -> Triple(
            RedIconBgDark,
            RedIconTintDark,
            Icons.Outlined.Movie
        )
        else -> Triple(
            Color(0xFF374151).copy(alpha = 0.3f),
            TextGray300,
            Icons.Outlined.Receipt
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(GlassCardBg)
            .border(
                width = 1.dp,
                color = GlassCardBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTintColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = transaction.description,
                    fontWeight = FontWeight.SemiBold,
                    color = TextWhite,
                    fontSize = 15.sp
                )
                Text(
                    text = "${transaction.category.name.lowercase().replaceFirstChar { it.uppercase() }} • ${
                        getRelativeTimeString(transaction.timestamp)
                    }",
                    fontWeight = FontWeight.Medium,
                    color = TextGray400,
                    fontSize = 12.sp
                )
            }
        }

        Text(
            text = "$amountPrefix$${String.format("%.2f", transaction.amount)}",
            fontWeight = FontWeight.Bold,
            color = amountColor,
            fontSize = 15.sp
        )
    }
}

private fun getRelativeTimeString(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < 86400000 -> "Today"
        diff < 172800000 -> "Yesterday"
        diff < 259200000 -> "2 days ago"
        diff < 345600000 -> "3 days ago"
        else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(timestamp))
    }
}

private fun getDefaultTransactions(): List<Transaction> {
    return listOf(
        Transaction(
            id = "1",
            accountId = "default",
            type = TransactionType.DEBIT,
            category = TransactionCategory.PAYMENT,
            amount = 15.99,
            description = "Netflix",
            timestamp = System.currentTimeMillis()
        ),
        Transaction(
            id = "2",
            accountId = "default",
            type = TransactionType.DEBIT,
            category = TransactionCategory.FOOD,
            amount = 5.50,
            description = "Starbucks",
            timestamp = System.currentTimeMillis() - 86400000
        ),
        Transaction(
            id = "3",
            accountId = "default",
            type = TransactionType.CREDIT,
            category = TransactionCategory.SALARY,
            amount = 3200.00,
            description = "Salary Deposit",
            timestamp = System.currentTimeMillis() - 172800000
        ),
        Transaction(
            id = "4",
            accountId = "default",
            type = TransactionType.DEBIT,
            category = TransactionCategory.SHOPPING,
            amount = 1299.00,
            description = "Apple Store",
            timestamp = System.currentTimeMillis() - 259200000
        )
    )
}
