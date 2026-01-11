package com.example.mybank.presentation.accounts

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.scale
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
import com.example.mybank.ui.theme.*

@Composable
fun MyAccountsScreen(
    viewModel: AccountsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onCreatePot: () -> Unit = {},
    onAccountClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
        ) {
            item {
                TopBar(onAddClick = onCreatePot)
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                TotalBalanceCard(
                    totalBalance = uiState.accounts.sumOf { it.balance },
                    trendPercentage = "+2.4%"
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                PortfolioHeader(accountCount = uiState.accounts.size)
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }

            items(uiState.accounts) { account ->
                AccountCard(
                    account = account,
                    onClick = { onAccountClick(account.id) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                RecentActivitySection(transactions = emptyList())
            }
        }

        FAB(
            onClick = onCreatePot,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 100.dp, end = 24.dp)
        )
    }
}

@Composable
private fun TopBar(onAddClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundDark.copy(alpha = 0.8f))
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "My Accounts",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TextWhite,
            fontSize = 20.sp
        )

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFF374151))
                .clickable(onClick = onAddClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Account",
                tint = TextWhite,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun TotalBalanceCard(
    totalBalance: Double,
    trendPercentage: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .offset(x = 250.dp, y = (-60).dp)
                .size(192.dp)
                .blur(100.dp)
                .background(Color.White.copy(alpha = 0.1f), CircleShape)
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = PrimaryBlue.copy(alpha = 0.25f)
                ),
            shape = RoundedCornerShape(16.dp),
            color = PrimaryBlue
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "TOTAL BALANCE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "$${String.format("%,.2f", totalBalance)}",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    fontSize = 36.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = trendPercentage,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Text(
                        text = "vs last month",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun PortfolioHeader(accountCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Your Portfolio",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextWhite,
            fontSize = 18.sp
        )

        TextButton(onClick = { }) {
            Text(
                text = "View All",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryBlue,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun AccountCard(
    account: Account,
    onClick: () -> Unit
) {
    val tagColor = getTagColor(account)
    val interactionSource = remember { MutableInteractionSource() }
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(12.dp),
        color = SurfaceDark,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = BorderDark,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = tagColor.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = account.tagLabel ?: account.accountType.name,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = tagColor,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )
                    }

                    Text(
                        text = account.accountName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        fontSize = 16.sp
                    )

                    Text(
                        text = "•••• ${account.accountNumber.takeLast(4)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TextGray500,
                        fontSize = 12.sp,
                        letterSpacing = 2.sp
                    )
                }

                Text(
                    text = "$${String.format("%,.2f", account.balance)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    fontSize = 18.sp
                )
            }

            if (account.accountType == AccountType.GOAL && account.goalAmount != null) {
                ProgressBar(
                    progress = account.progress,
                    color = tagColor
                )
            }
        }
    }
}

@Composable
private fun ProgressBar(progress: Float, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "PROGRESS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = TextGray500,
                fontSize = 11.sp,
                letterSpacing = 1.sp
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color,
                fontSize = 11.sp,
                letterSpacing = 1.sp
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF334155))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
    }
}

@Composable
private fun RecentActivitySection(transactions: List<Transaction>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Recent Activity",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextWhite,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = SurfaceDark,
            tonalElevation = 1.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = BorderDark,
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                transactions.take(2).forEachIndexed { index, transaction ->
                    TransactionItem(transaction = transaction)
                    if (index < transactions.size - 1) {
                        Divider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 1.dp,
                            color = BorderDark
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionItem(transaction: Transaction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFF374151)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingBag,
                contentDescription = null,
                tint = TextGray300,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = transaction.description,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                fontSize = 14.sp
            )
            Text(
                text = "Today, 2:45 PM",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Normal,
                color = TextGray500,
                fontSize = 12.sp
            )
        }

        Text(
            text = "-$${String.format("%.2f", transaction.amount)}",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = TextWhite,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun FAB(onClick: () -> Unit, modifier: Modifier = Modifier) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.size(56.dp),
        shape = CircleShape,
        containerColor = PrimaryBlue,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 16.dp
        )
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Create Pot",
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )
    }
}

private fun getTagColor(account: Account): Color {
    return when (account.accountType) {
        AccountType.CHECKING -> Color(0xFF3B82F6)
        AccountType.SAVINGS -> Color(0xFF10B981)
        AccountType.GOAL -> Color(0xFFF97316)
        AccountType.INVESTMENT -> Color(0xFFA855F7)
        AccountType.SPENDING -> Color(0xFF14B8A6)
        AccountType.CREDIT -> Color(0xFFEC4899)
    }
}
