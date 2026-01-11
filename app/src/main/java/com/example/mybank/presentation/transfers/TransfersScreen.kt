package com.example.mybank.presentation.transfers

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.mybank.R
import kotlinx.coroutines.delay

// ====================== PREMIUM COLORS ======================
private val PurplePrimary = Color(0xFF8B7FFF)
private val PurpleLight = Color(0xFFB8A9FF)
private val PurpleMedium = Color(0xFF9B8FFF)
private val PurpleVibrant = Color(0xFF7B6FEE)
private val PurpleDark = Color(0xFF5B4FCC)
private val PurpleAccent = Color(0xFF6C5CE7)
private val TextPrimary = Color(0xFF1A1A2E)
private val TextSecondary = Color(0xFF9CA3AF)
private val TextWhite = Color(0xFFFFFFFF)
private val SuccessGreen = Color(0xFF00C566)
private val CardBgWhite = Color(0xFFFAFAFC)
private val IconBgLight = Color(0xFFF5F5F7)
private val SearchBgColor = Color(0xFFF3F4F6)
private val DividerColor = Color(0xFFE5E7EB)

// Premium background gradient (light purple to white)
private val BackgroundGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFE8E0FF),
        Color(0xFFF0ECFF),
        Color(0xFFF8F6FF),
        Color(0xFFFFFEFF)
    )
)

// ====================== DATA CLASSES ======================

/**
 * Data class representing a recent contact for quick transfers
 */
data class RecentContact(
    val id: Int,
    val name: String,
    val initials: String,
    val avatarUrl: String? = null, // Optional avatar image URL
    val backgroundColor: Color = PurplePrimary
)

/**
 * Data class representing a transfer action card
 */
data class TransferAction(
    val id: Int,
    val icon: ImageVector,
    val title: String,
    val subtitle: String? = null
)

/**
 * Data class representing a transaction item
 */
data class TransferTransaction(
    val id: Int,
    val name: String,
    val dateTime: String,
    val amount: Double,
    val avatarInitials: String,
    val avatarUrl: String? = null, // Professional avatar image URL loaded via Coil
    val avatarColor: Color = PurpleLight
)

// ====================== MAIN SCREEN ======================

/**
 * TransfersScreen - Premium Banking Transfers Screen
 * 
 * This screen displays the transfers functionality with a modern banking UI.
 * Features include:
 * - Search functionality for finding contacts/transactions
 * - Quick send section with recent contacts
 * - 2x2 grid of main transfer actions
 * - Floating action button for new transfers
 * - Recent transactions list
 * - Bottom navigation bar
 * 
 * @param onNavigateToHome Navigation callback for Home tab
 * @param onNavigateToCards Navigation callback for Cards tab
 * @param onNavigateToMore Navigation callback for More tab
 * @param onNavigateBack Navigation callback for back action
 */
@Composable
fun TransfersScreen(
    onNavigateToHome: () -> Unit = {},
    onNavigateToCards: () -> Unit = {},
    onNavigateToMore: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToAccounts: () -> Unit = {}
) {
    // State for search text
    var searchText by remember { mutableStateOf("") }
    
    // More menu state
    var showMoreMenu by remember { mutableStateOf(false) }

    // Animation states
    var isVisible by remember { mutableStateOf(false) }
    
    // Context for Toast messages
    val context = LocalContext.current
    
    // Sample recent contacts data with professional avatar URLs from randomuser.me
    val recentContacts = remember {
        listOf(
            RecentContact(
                id = 1, 
                name = "Anna L.", 
                initials = "AL", 
                avatarUrl = "https://randomuser.me/api/portraits/women/44.jpg",
                backgroundColor = Color(0xFFE8DEF8)
            ),
            RecentContact(
                id = 2, 
                name = "Penny K.", 
                initials = "PK", 
                avatarUrl = "https://randomuser.me/api/portraits/women/68.jpg",
                backgroundColor = PurplePrimary
            ),
            RecentContact(
                id = 3, 
                name = "Klark", 
                initials = "K", 
                avatarUrl = "https://randomuser.me/api/portraits/men/32.jpg",
                backgroundColor = Color(0xFFE8DEF8)
            ),
            RecentContact(
                id = 4, 
                name = "John D.", 
                initials = "JD", 
                avatarUrl = "https://randomuser.me/api/portraits/men/75.jpg",
                backgroundColor = PurpleMedium
            ),
            RecentContact(
                id = 5, 
                name = "Sarah M.", 
                initials = "SM", 
                avatarUrl = "https://randomuser.me/api/portraits/women/90.jpg",
                backgroundColor = Color(0xFFE8DEF8)
            )
        )
    }
    
    // Transfer action cards data
    val transferActions = remember {
        listOf(
            TransferAction(1, Icons.Outlined.Send, "Popular", "payments"),
            TransferAction(2, Icons.Outlined.AccountBalance, "Bank of", "recipient"),
            TransferAction(3, Icons.Outlined.CreditCard, "Between", "my cards"),
            TransferAction(4, Icons.Outlined.Person, "Recipient's", "card")
        )
    }
    
    // Sample transactions data with professional avatar URLs
    val transactions = remember {
        listOf(
            TransferTransaction(
                id = 1, 
                name = "Mark Rivero", 
                dateTime = "Today, 16:40", 
                amount = 40.0, 
                avatarInitials = "MR", 
                avatarUrl = "https://randomuser.me/api/portraits/men/46.jpg",
                avatarColor = Color(0xFFE8DEF8)
            ),
            TransferTransaction(
                id = 2, 
                name = "Anna Lopez", 
                dateTime = "Today, 14:20", 
                amount = 125.0, 
                avatarInitials = "AL", 
                avatarUrl = "https://randomuser.me/api/portraits/women/65.jpg",
                avatarColor = PurpleLight
            ),
            TransferTransaction(
                id = 3, 
                name = "Kapital Bank", 
                dateTime = "Yesterday, 10:30", 
                amount = 500.0, 
                avatarInitials = "KB", 
                avatarUrl = null, // Bank doesn't have avatar
                avatarColor = PurpleMedium
            ),
            TransferTransaction(
                id = 4, 
                name = "John Smith", 
                dateTime = "Yesterday, 09:15", 
                amount = -75.0, 
                avatarInitials = "JS", 
                avatarUrl = "https://randomuser.me/api/portraits/men/22.jpg",
                avatarColor = Color(0xFFE8DEF8)
            )
        )
    }
    
    // Trigger entrance animations
    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGradient)
            .statusBarsPadding() // Disable edge-to-edge for status bar
    ) {
        // Main scrollable content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp), // Space for bottom navigation
            contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
        ) {
            // ========== HEADER ==========
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { -40 }
                ) {
                    TransfersHeader()
                }
            }
            
            // ========== SEARCH BAR ==========
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(600, 100)) + slideInVertically(tween(600, 100)) { -30 }
                ) {
                    SearchBar(
                        searchText = searchText,
                        onSearchTextChange = { searchText = it },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    )
                }
            }
            
            // ========== QUICK SEND SECTION ==========
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(600, 200)) + slideInVertically(tween(600, 200)) { -20 }
                ) {
                    QuickSendSection(
                        contacts = recentContacts,
                        onSendClick = {
                            Toast.makeText(context, "New transfer", Toast.LENGTH_SHORT).show()
                        },
                        onContactClick = { contact ->
                            Toast.makeText(context, "Send to ${contact.name}", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
            
            // ========== TRANSFER ACTIONS GRID WITH FAB ==========
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(600, 300)) + slideInVertically(tween(600, 300)) { -20 }
                ) {
                    TransferActionsGrid(
                        actions = transferActions,
                        onActionClick = { action ->
                            Toast.makeText(context, "${action.title} ${action.subtitle ?: ""}", Toast.LENGTH_SHORT).show()
                        },
                        onFabClick = {
                            Toast.makeText(context, "New transfer", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
            
            // ========== TRANSACTIONS HEADER ==========
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(600, 400)) + slideInVertically(tween(600, 400)) { -20 }
                ) {
                    TransactionsHeader(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                    )
                }
            }
            
            // ========== TRANSACTIONS LIST ==========
            itemsIndexed(transactions) { index, transaction ->
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(600, 500 + index * 50)) + slideInVertically(tween(600, 500 + index * 50)) { 20 }
                ) {
                    TransactionItem(
                        transaction = transaction,
                        onClick = {
                            Toast.makeText(context, "View transaction: ${transaction.name}", Toast.LENGTH_SHORT).show()
                        },
                        showDivider = index < transactions.size - 1,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }
        }
        
        // ========== BOTTOM NAVIGATION BAR ==========
        TransfersBottomNavigationBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            selectedIndex = 2, // Transfers tab is selected
            onHomeClick = onNavigateToHome,
            onCardsClick = onNavigateToCards,
            onTransfersClick = { /* Already on Transfers */ },
            onMoreClick = { showMoreMenu = true }
        )
        
        // More Menu Overlay
        TransfersMoreMenuOverlay(
            isVisible = showMoreMenu,
            onDismiss = { showMoreMenu = false },
            onSettingsClick = {
                showMoreMenu = false
                onNavigateToSettings()
            },
            onAccountsClick = {
                showMoreMenu = false
                onNavigateToAccounts()
            }
        )
    }
}

// ====================== HEADER SECTION ======================

/**
 * Header with "Transfers" title
 */
@Composable
private fun TransfersHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Transfers",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            letterSpacing = (-0.5).sp
        )
    }
}

// ====================== SEARCH BAR ======================

/**
 * Search bar with placeholder and search icon
 */
@Composable
private fun SearchBar(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = SearchBgColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search icon
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = TextSecondary,
                modifier = Modifier.size(22.dp)
            )
            
            // Search text field
            if (searchText.isEmpty()) {
                Text(
                    text = "Search",
                    fontSize = 16.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Normal
                )
            } else {
                Text(
                    text = searchText,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
            }
        }
    }
}

// ====================== QUICK SEND SECTION ======================

/**
 * Quick send section with "Send" button and horizontal scrollable contacts
 */
@Composable
private fun QuickSendSection(
    contacts: List<RecentContact>,
    onSendClick: () -> Unit,
    onContactClick: (RecentContact) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Send button (first item)
        item {
            SendButton(onClick = onSendClick)
        }
        
        // Recent contacts
        items(contacts) { contact ->
            RecentContactItem(
                contact = contact,
                onClick = { onContactClick(contact) }
            )
        }
    }
}

/**
 * Purple circular "Send" button with + icon
 */
@Composable
private fun SendButton(onClick: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "sendButtonScale"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isPressed = true
                onClick()
            }
    ) {
        // Purple circle with + icon
        Box(
            modifier = Modifier
                .size(64.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = CircleShape,
                    spotColor = PurplePrimary.copy(alpha = 0.4f)
                )
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(PurplePrimary, PurpleMedium)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Send",
                tint = TextWhite,
                modifier = Modifier.size(28.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // "Send" label
        Text(
            text = "Send",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
    }
    
    // Reset press state
    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(150)
            isPressed = false
        }
    }
}

/**
 * Individual recent contact item with avatar and name
 */
@Composable
private fun RecentContactItem(
    contact: RecentContact,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "contactScale"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isPressed = true
                onClick()
            }
    ) {
        // Avatar circle with professional image loaded from web via Coil
        Box(
            modifier = Modifier
                .size(64.dp)
                .shadow(
                    elevation = 6.dp,
                    shape = CircleShape,
                    spotColor = Color.Black.copy(alpha = 0.15f)
                )
                .clip(CircleShape)
                .background(contact.backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            if (contact.avatarUrl != null) {
                // Load professional avatar image from web using Coil
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(contact.avatarUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Profile photo of ${contact.name}",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    loading = {
                        // Show initials while loading
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(contact.backgroundColor),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = PurplePrimary,
                                strokeWidth = 2.dp
                            )
                        }
                    },
                    error = {
                        // Show initials on error
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(contact.backgroundColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = contact.initials,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (contact.backgroundColor == PurplePrimary) TextWhite else PurpleDark
                            )
                        }
                    }
                )
            } else {
                // Fallback to initials if no URL provided
                Text(
                    text = contact.initials,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (contact.backgroundColor == PurplePrimary) TextWhite else PurpleDark
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Contact name
        Text(
            text = contact.name,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(70.dp)
        )
    }
    
    // Reset press state
    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(150)
            isPressed = false
        }
    }
}

// ====================== TRANSFER ACTIONS GRID ======================

/**
 * 2x2 grid of transfer action cards with floating action button
 */
@Composable
private fun TransferActionsGrid(
    actions: List<TransferAction>,
    onActionClick: (TransferAction) -> Unit,
    onFabClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp)
    ) {
        // Grid of action cards
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // First row
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                TransferActionCard(
                    action = actions[0],
                    onClick = { onActionClick(actions[0]) },
                    modifier = Modifier.weight(1f)
                )
                TransferActionCard(
                    action = actions[1],
                    onClick = { onActionClick(actions[1]) },
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Second row
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                TransferActionCard(
                    action = actions[2],
                    onClick = { onActionClick(actions[2]) },
                    modifier = Modifier.weight(1f)
                )
                TransferActionCard(
                    action = actions[3],
                    onClick = { onActionClick(actions[3]) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        // Floating Action Button positioned at the left side overlapping the grid
        FloatingPlusButton(
            onClick = onFabClick,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = (-10).dp, y = 20.dp)
                .zIndex(1f)
        )
    }
}

/**
 * Individual transfer action card (white rounded card with icon and text)
 */
@Composable
private fun TransferActionCard(
    action: TransferAction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "cardScale"
    )
    
    Surface(
        modifier = modifier
            .height(130.dp)
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isPressed = true
                onClick()
            },
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 8.dp,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            // Icon box with dark background
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(TextPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = action.icon,
                    contentDescription = action.title,
                    tint = TextWhite,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Title and subtitle
            Column {
                Text(
                    text = action.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    lineHeight = 18.sp
                )
                if (action.subtitle != null) {
                    Text(
                        text = action.subtitle,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        color = TextPrimary,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
    
    // Reset press state
    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(150)
            isPressed = false
        }
    }
}

/**
 * Floating purple "+" button that overlaps the action cards grid
 */
@Composable
private fun FloatingPlusButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "fabScale"
    )
    
    // Pulsing animation for the FAB
    val infiniteTransition = rememberInfiniteTransition(label = "fabPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    
    Box(
        modifier = modifier
            .size(64.dp)
            .scale(scale * pulseScale)
            .shadow(
                elevation = 16.dp,
                shape = CircleShape,
                spotColor = PurplePrimary.copy(alpha = 0.5f)
            )
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(PurpleLight, PurplePrimary)
                )
            )
            .border(
                width = 3.dp,
                color = Color.White.copy(alpha = 0.3f),
                shape = CircleShape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isPressed = true
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "New Transfer",
            tint = TextWhite.copy(alpha = 0.9f),
            modifier = Modifier.size(28.dp)
        )
    }
    
    // Reset press state
    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(150)
            isPressed = false
        }
    }
}

// ====================== TRANSACTIONS SECTION ======================

/**
 * "TRANSACTIONS" section header
 */
@Composable
private fun TransactionsHeader(modifier: Modifier = Modifier) {
    Text(
        text = "TRANSACTIONS",
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextSecondary,
        letterSpacing = 1.sp,
        modifier = modifier
    )
}

/**
 * Individual transaction item with avatar, name, date, and amount
 */
@Composable
private fun TransactionItem(
    transaction: TransferTransaction,
    onClick: () -> Unit,
    showDivider: Boolean = true,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "transactionScale"
    )
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isPressed = true
                onClick()
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side: Avatar and info
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar with professional image loaded from web via Coil
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(
                            elevation = 4.dp,
                            shape = CircleShape,
                            spotColor = Color.Black.copy(alpha = 0.1f)
                        )
                        .clip(CircleShape)
                        .background(transaction.avatarColor),
                    contentAlignment = Alignment.Center
                ) {
                    if (transaction.avatarUrl != null) {
                        // Load professional avatar image from web using Coil
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(transaction.avatarUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Profile photo of ${transaction.name}",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop,
                            loading = {
                                // Show initials while loading
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(transaction.avatarColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = transaction.avatarInitials,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = PurpleDark
                                    )
                                }
                            },
                            error = {
                                // Show initials on error
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(transaction.avatarColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = transaction.avatarInitials,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = PurpleDark
                                    )
                                }
                            }
                        )
                    } else {
                        // Fallback to initials (for banks, companies, etc.)
                        Text(
                            text = transaction.avatarInitials,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PurpleDark
                        )
                    }
                }
                
                // Name and date
                Column {
                    Text(
                        text = transaction.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = transaction.dateTime,
                        fontSize = 13.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
            
            // Right side: Amount
            val isPositive = transaction.amount > 0
            Text(
                text = "${if (isPositive) "+" else ""}$${String.format("%.0f", kotlin.math.abs(transaction.amount))}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isPositive) SuccessGreen else TextPrimary
            )
        }
        
        // Divider
        if (showDivider) {
            HorizontalDivider(
                color = DividerColor,
                thickness = 1.dp
            )
        }
    }
    
    // Reset press state
    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(150)
            isPressed = false
        }
    }
}

// ====================== MORE MENU OVERLAY ======================
@Composable
private fun TransfersMoreMenuOverlay(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onSettingsClick: () -> Unit,
    onAccountsClick: () -> Unit
) {
    val overlayAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(300),
        label = "overlayAlpha"
    )
    
    val menuOffset by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 100.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "menuOffset"
    )
    
    val menuScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "menuScale"
    )
    
    if (isVisible || overlayAlpha > 0f) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = overlayAlpha }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onDismiss() }
            )
            
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 100.dp, start = 20.dp, end = 20.dp)
                    .offset(y = menuOffset)
                    .scale(menuScale)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    shadowElevation = 20.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Quick Actions",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        TransfersMoreMenuItem(
                            icon = Icons.Outlined.Settings,
                            title = "Settings",
                            subtitle = "App preferences & security",
                            iconBgColor = Color(0xFFE8E0FF),
                            iconColor = PurplePrimary,
                            onClick = onSettingsClick
                        )
                        
                        HorizontalDivider(
                            color = DividerColor,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        
                        TransfersMoreMenuItem(
                            icon = Icons.Outlined.AccountBalance,
                            title = "Accounts",
                            subtitle = "View all your accounts",
                            iconBgColor = Color(0xFFE5F9E7),
                            iconColor = SuccessGreen,
                            onClick = onAccountsClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TransfersMoreMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconBgColor: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "menuItemScale"
    )
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isPressed = true
                onClick()
            }
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(26.dp)
            )
        }
        
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = TextSecondary
            )
        }
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(24.dp)
        )
    }
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(150)
            isPressed = false
        }
    }
}

// ====================== BOTTOM NAVIGATION BAR ======================

/**
 * Premium bottom navigation bar with animated selection
 */
@Composable
private fun TransfersBottomNavigationBar(
    modifier: Modifier = Modifier,
    selectedIndex: Int = 2,
    onHomeClick: () -> Unit,
    onCardsClick: () -> Unit,
    onTransfersClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding() // Disable edge-to-edge for navigation bar
            .height(80.dp),
        color = Color.White,
        shadowElevation = 16.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                icon = Icons.Outlined.Home,
                selectedIcon = Icons.Filled.Home,
                label = "Home",
                isSelected = selectedIndex == 0,
                onClick = onHomeClick
            )
            BottomNavItem(
                icon = Icons.Outlined.Layers,
                selectedIcon = Icons.Filled.Layers,
                label = "Cards",
                isSelected = selectedIndex == 1,
                onClick = onCardsClick
            )
            BottomNavItem(
                icon = Icons.Outlined.CompareArrows,
                selectedIcon = Icons.Filled.CompareArrows,
                label = "Transfers",
                isSelected = selectedIndex == 2,
                onClick = onTransfersClick
            )
            BottomNavItem(
                icon = Icons.Outlined.Apps,
                selectedIcon = Icons.Filled.Apps,
                label = "More",
                isSelected = selectedIndex == 3,
                onClick = onMoreClick
            )
        }
    }
}

/**
 * Individual bottom navigation item with animation
 */
@Composable
private fun BottomNavItem(
    icon: ImageVector,
    selectedIcon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "navScale"
    )
    
    // Animate the indicator dot
    val indicatorAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(200),
        label = "indicatorAlpha"
    )
    
    Column(
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = if (isSelected) selectedIcon else icon,
            contentDescription = label,
            tint = if (isSelected) PurplePrimary else TextSecondary,
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (isSelected) PurplePrimary else TextSecondary,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
        
        // Purple indicator dot for selected item (like in the design)
        Box(
            modifier = Modifier
                .size(5.dp)
                .graphicsLayer { alpha = indicatorAlpha }
                .clip(CircleShape)
                .background(PurplePrimary)
        )
    }
}

// ====================== PREVIEWS ======================

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TransfersScreenPreview() {
    MaterialTheme {
        TransfersScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun SearchBarPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundGradient)
                .padding(20.dp)
        ) {
            SearchBar(
                searchText = "",
                onSearchTextChange = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun QuickSendSectionPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundGradient)
        ) {
            QuickSendSection(
                contacts = listOf(
                    RecentContact(1, "Anna L.", "AL"),
                    RecentContact(2, "Penny K.", "PK", backgroundColor = PurplePrimary),
                    RecentContact(3, "Klark", "K")
                ),
                onSendClick = {},
                onContactClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TransferActionCardPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .background(BackgroundGradient)
                .padding(20.dp)
        ) {
            TransferActionCard(
                action = TransferAction(1, Icons.Outlined.Send, "Popular", "payments"),
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TransactionItemPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundGradient)
                .padding(horizontal = 20.dp)
        ) {
            TransactionItem(
                transaction = TransferTransaction(
                    id = 1,
                    name = "Mark Rivero",
                    dateTime = "Today, 16:40",
                    amount = 40.0,
                    avatarInitials = "MR"
                ),
                onClick = {},
                showDivider = true
            )
        }
    }
}
