package com.example.mybank.presentation.analytics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import java.text.NumberFormat
import java.util.Locale

// Colors
private val BackgroundDark = Color(0xFF101622)
private val SurfaceDark = Color(0xFF1c2433)
private val PrimaryBlue = Color(0xFF1152d4)
private val TextSecondary = Color(0xFF9da6b9)
private val TextPrimary = Color.White
private val SelectedSurface = Color(0xFF2d3748)

@Composable
fun AnalyticsScreen(
    navController: NavController,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            AnalyticsTopBar(
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {
            // Period Selector
            PeriodSelector(
                selectedPeriod = uiState.selectedPeriod,
                onPeriodSelected = { viewModel.setPeriod(it) }
            )

            // Total Spending
            TotalSpendingSection(
                total = uiState.totalSpent,
                trendPercent = uiState.trendPercent,
                isTrendingUp = uiState.isTrendingUp
            )

            // Chart
            SpendingChartSection(
                dataPoints = uiState.chartData,
                labels = uiState.chartLabels
            )

            // Categories Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Categories",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { /* View All */ }) {
                    Text(
                        text = "View All",
                        color = PrimaryBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Categories List
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                uiState.categories.forEachIndexed { index, category ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(300, delayMillis = index * 50)) +
                                slideInVertically(
                                    animationSpec = tween(300, delayMillis = index * 50),
                                    initialOffsetY = { 50 }
                                )
                    ) {
                        CategoryCard(category)
                    }
                }
            }

            // Insight Card
            uiState.savingInsight?.let { insight ->
                Spacer(modifier = Modifier.height(24.dp))
                InsightCard(insight)
                Spacer(modifier = Modifier.height(100.dp)) // Bottom padding for nav bar
            }
        }
    }
}

@Composable
fun AnalyticsTopBar(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundDark.copy(alpha = 0.8f))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Back Button
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable(onClick = onBackClick)
                .background(Color.Transparent), // Hover effect handled by clickable default indication or custom
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = "Back",
                tint = TextPrimary,
                modifier = Modifier.size(24.dp)
            )
        }

        Text(
            text = "Analytics",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        // Download Button
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable { /* Download */ }
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Download, // Need to make sure Download icon exists or use similar
                contentDescription = "Download",
                tint = PrimaryBlue,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun PeriodSelector(
    selectedPeriod: AnalyticsPeriod,
    onPeriodSelected: (AnalyticsPeriod) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceDark)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        AnalyticsPeriod.values().forEach { period ->
            val isSelected = period == selectedPeriod
            val backgroundColor by animateColorAsState(
                if (isSelected) SelectedSurface else Color.Transparent, label = "bg"
            )
            val textColor by animateColorAsState(
                if (isSelected) PrimaryBlue else TextSecondary, label = "text"
            )
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .background(backgroundColor)
                    .clickable { onPeriodSelected(period) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = period.name.lowercase().capitalize(Locale.ROOT),
                    color = textColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun TotalSpendingSection(
    total: Double,
    trendPercent: Double,
    isTrendingUp: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Total spent this month",
            color = TextSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = NumberFormat.getCurrencyInstance(Locale.US).format(total),
            color = TextPrimary,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-1).sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isTrendingUp) Icons.Outlined.TrendingUp else Icons.Outlined.TrendingDown,
                contentDescription = null,
                tint = if (isTrendingUp) Color.Red else Color.Green, // Typically Red for spending UP is bad? Or just Red/Green? HTML shows red + trending_up for +12%
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${String.format("%.1f", trendPercent)}% from last period",
                color = if (isTrendingUp) Color.Red else Color.Green, // Spending up = Red usually
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun SpendingChartSection(
    dataPoints: List<Float>,
    labels: List<String>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceDark)
            .border(1.dp, Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Spending Trend",
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Icon(
                imageVector = Icons.Outlined.Insights,
                contentDescription = null,
                tint = PrimaryBlue
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        SpendingTrendChart(
            dataPoints = dataPoints,
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEach { label ->
                Text(
                    text = label,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun CategoryCard(category: CategorySpending) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .border(1.dp, Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon Box
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(category.color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = getIconForName(category.icon),
                contentDescription = null,
                tint = category.color,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = category.name,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = NumberFormat.getCurrencyInstance(Locale.US).format(category.amount),
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(Color.Gray.copy(alpha = 0.2f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(category.percentage / 100f)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(category.color)
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "${String.format("%.0f", category.percentage)}% of total",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun InsightCard(insight: Insight) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(PrimaryBlue.copy(alpha = 0.1f))
            .border(1.dp, PrimaryBlue.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(PrimaryBlue),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Lightbulb,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column {
            Text(
                text = insight.title,
                color = PrimaryBlue,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = insight.message, // Would need rich text parsing for **bold**, keeping simple for now
                color = TextSecondary.copy(alpha = 0.9f),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

// Helper to get vector icon from name
fun getIconForName(name: String): ImageVector {
    return when (name) {
        "restaurant" -> Icons.Outlined.Restaurant
        "shopping_bag" -> Icons.Outlined.ShoppingBag
        "directions_car" -> Icons.Outlined.DirectionsCar
        "movie" -> Icons.Outlined.Movie
        "receipt_long" -> Icons.Outlined.ReceiptLong
        "attach_money" -> Icons.Outlined.AttachMoney
        "lightbulb" -> Icons.Outlined.Lightbulb
        else -> Icons.Outlined.Category
    }
}
