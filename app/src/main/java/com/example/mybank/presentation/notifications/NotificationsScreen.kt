package com.example.mybank.presentation.notifications

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mybank.data.model.BankNotification
import com.example.mybank.data.model.NotificationType
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

// ====================== COLORS ======================
private val Primary = Color(0xFF1152d4)
private val PrimaryDark = Color(0xFF0d3ca0)
private val Secondary = Color(0xFF64748b)
private val AccentSuccess = Color(0xFF10b981)
private val AccentWarning = Color(0xFFef4444)
private val AccentInfo = Color(0xFF3b82f6)
private val BackgroundDark = Color(0xFF101622)
private val SurfaceDark = Color(0xFF171e2c)
private val SurfaceHover = Color(0xFF1e2536)
private val Separator = Color(0xFF262f40)
private val TextWhite = Color(0xFFFFFFFF)
private val TextGray = Color(0xFF94a3b8)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Group notifications by date
    val groupedNotifications = remember(uiState.filteredNotifications) {
        groupNotificationsByDate(uiState.filteredNotifications)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Sticky Top App Bar
            NotificationsHeader(
                onBackClick = onNavigateBack,
                onMarkAllRead = { viewModel.markAllAsRead() }
            )

            // Filter Tabs
            FilterTabs(
                selectedFilter = uiState.selectedFilter,
                onFilterSelected = { viewModel.updateFilter(it) }
            )

            // Notification List
            if (uiState.filteredNotifications.isEmpty() && !uiState.isLoading) {
                EmptyNotificationsState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    groupedNotifications.forEach { (header, notifications) ->
                        stickyHeader {
                            DateHeader(title = header)
                        }
                        
                        items(notifications, key = { it.id }) { notification ->
                            NotificationItem(
                                notification = notification,
                                onClick = { viewModel.markAsRead(notification.id) }
                            )
                        }
                    }
                    
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "End of notifications",
                                color = Secondary.copy(alpha = 0.5f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationsHeader(
    onBackClick: () -> Unit,
    onMarkAllRead: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundDark.copy(alpha = 0.95f))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Back Button
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Transparent)
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBackIosNew,
                    contentDescription = "Back",
                    tint = TextWhite,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Title
            Text(
                text = "Notifications",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                letterSpacing = (-0.5).sp
            )

            // Mark All Read Button
            IconButton(
                onClick = onMarkAllRead,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Rounded.DoneAll,
                    contentDescription = "Mark all as read",
                    tint = Primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        // Bottom Border
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(1.dp)
                .background(Separator)
        )
    }
}

@Composable
fun FilterTabs(
    selectedFilter: NotificationFilter,
    onFilterSelected: (NotificationFilter) -> Unit
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
        FilterTab(
            text = "All",
            selected = selectedFilter == NotificationFilter.ALL,
            onClick = { onFilterSelected(NotificationFilter.ALL) },
            modifier = Modifier.weight(1f)
        )
        FilterTab(
            text = "Transactional",
            selected = selectedFilter == NotificationFilter.TRANSACTIONAL,
            onClick = { onFilterSelected(NotificationFilter.TRANSACTIONAL) },
            modifier = Modifier.weight(1f)
        )
        FilterTab(
            text = "Alerts",
            selected = selectedFilter == NotificationFilter.ALERTS,
            onClick = { onFilterSelected(NotificationFilter.ALERTS) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun FilterTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) Primary else Color.Transparent,
        label = "tabBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) TextWhite else Secondary,
        label = "tabText"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
fun DateHeader(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Secondary,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun NotificationItem(
    notification: BankNotification,
    onClick: () -> Unit
) {
    val config = getNotificationConfig(notification.type)
    
    // Scale animation on press
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        label = "scale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null // Custom ripple could be added here
            ) { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(config.bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = config.icon,
                contentDescription = null,
                tint = config.iconColor,
                modifier = Modifier.size(24.dp)
            )
        }

        // Content
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = notification.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextWhite,
                    lineHeight = 20.sp
                )
                
                if (!notification.isRead) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Primary)
                            .shadow(8.dp, CircleShape, spotColor = Primary)
                    )
                }
            }

            Text(
                text = notification.message,
                fontSize = 14.sp,
                color = Secondary,
                lineHeight = 20.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = formatTimeAgo(notification.timestamp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Secondary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
    
    // Separator
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Separator.copy(alpha = 0.5f))
    )
}

@Composable
fun EmptyNotificationsState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(SurfaceDark),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.NotificationsOff,
                contentDescription = null,
                tint = Secondary,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No notifications yet",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextWhite
        )
        Text(
            text = "We'll notify you when something happens",
            fontSize = 14.sp,
            color = Secondary,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

// ====================== UTILS ======================

data class NotificationConfig(
    val icon: ImageVector,
    val bgColor: Color,
    val iconColor: Color
)

@Composable
fun getNotificationConfig(type: NotificationType): NotificationConfig {
    return when (type) {
        NotificationType.INCOME -> NotificationConfig(
            icon = Icons.Rounded.ArrowDownward,
            bgColor = AccentSuccess.copy(alpha = 0.1f),
            iconColor = AccentSuccess
        )
        NotificationType.EXPENSE -> NotificationConfig(
            icon = Icons.Rounded.ShoppingBag,
            bgColor = AccentInfo.copy(alpha = 0.1f),
            iconColor = AccentInfo
        )
        NotificationType.LOW_BALANCE -> NotificationConfig(
            icon = Icons.Rounded.PriorityHigh,
            bgColor = AccentWarning.copy(alpha = 0.1f),
            iconColor = AccentWarning
        )
        NotificationType.SECURITY -> NotificationConfig(
            icon = Icons.Rounded.Shield,
            bgColor = Color(0xFFea580c).copy(alpha = 0.1f), // Orange
            iconColor = Color(0xFFea580c)
        )
        NotificationType.SUBSCRIPTION -> NotificationConfig(
            icon = Icons.Rounded.ShoppingBag,
            bgColor = AccentInfo.copy(alpha = 0.1f),
            iconColor = AccentInfo
        )
        NotificationType.RATE_UPDATE -> NotificationConfig(
            icon = Icons.Rounded.Percent,
            bgColor = Color(0xFF9333ea).copy(alpha = 0.1f), // Purple
            iconColor = Color(0xFF9333ea)
        )
        NotificationType.STATEMENT -> NotificationConfig(
            icon = Icons.Rounded.Description,
            bgColor = Secondary.copy(alpha = 0.1f),
            iconColor = Secondary
        )
        NotificationType.INFO -> NotificationConfig(
            icon = Icons.Rounded.Info,
            bgColor = Secondary.copy(alpha = 0.1f),
            iconColor = Secondary
        )
        else -> NotificationConfig(
            icon = Icons.Rounded.Notifications,
            bgColor = Secondary.copy(alpha = 0.1f),
            iconColor = Secondary
        )
    }
}

fun groupNotificationsByDate(notifications: List<BankNotification>): Map<String, List<BankNotification>> {
    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    val thisWeek = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }

    val grouped = linkedMapOf<String, MutableList<BankNotification>>()

    notifications.sortedByDescending { it.timestamp }.forEach { notification ->
        val date = Calendar.getInstance().apply { timeInMillis = notification.timestamp }

        val header = when {
            isSameDay(date, today) -> "TODAY"
            isSameDay(date, yesterday) -> "YESTERDAY"
            date.after(thisWeek) -> "THIS WEEK"
            else -> "OLDER"
        }

        grouped.getOrPut(header) { mutableListOf() }.add(notification)
    }
    
    return grouped
}

fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

fun formatTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60 * 1000 -> "Just now"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}m ago"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}h ago"
        diff < 48 * 60 * 60 * 1000 -> "Yesterday at ${SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))}"
        else -> SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date(timestamp))
    }
}
