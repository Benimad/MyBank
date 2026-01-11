package com.example.mybank.presentation.cards

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.mybank.presentation.components.BankCard
import com.example.mybank.ui.theme.InterFontFamily
import com.example.mybank.ui.theme.PrimaryBlue

@Composable
fun CardsScreen(
    onNavigateToHome: () -> Unit = {},
    onNavigateToTransfers: () -> Unit = {},
    onNavigateToMore: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToAccounts: () -> Unit = {},
    onAddCard: () -> Unit = {},
    onNavigateToStats: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onCardDetails: (String) -> Unit = {},
    viewModel: CardsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)) // background-dark
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 100.dp) // Space for bottom nav
        ) {
            // 1. Header
            CardsHeader(onBackClick = onNavigateToHome)

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Card Section
            if (uiState.selectedCard != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clickable { uiState.selectedCard?.let { onCardDetails(it.id) } },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .aspectRatio(1.586f)
                            .offset(y = 16.dp)
                            .blur(40.dp)
                            .background(PrimaryBlue.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                    )

                    BankCard(
                        cardHolderName = uiState.selectedCard!!.cardHolderName,
                        lastFour = uiState.selectedCard!!.lastFourDigits,
                        expiry = uiState.selectedCard!!.expiryFormatted,
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                shadowElevation = 10.dp.toPx()
                                shape = RoundedCornerShape(16.dp)
                                clip = true
                            }
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No cards available",
                        color = Color(0xFF64748B),
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Page Indicators
            if (uiState.cards.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    uiState.cards.take(3).forEachIndexed { index, card ->
                        Box(
                            modifier = Modifier
                                .let {
                                    if (card.id == uiState.selectedCard?.id) {
                                        it.width(24.dp).height(6.dp)
                                    } else {
                                        it.size(6.dp)
                                    }
                                }
                                .clip(if (card.id == uiState.selectedCard?.id) RoundedCornerShape(3.dp) else CircleShape)
                                .background(if (card.id == uiState.selectedCard?.id) PrimaryBlue else Color(0xFF334155))
                                .clickable { viewModel.selectCard(card) }
                        )
                        if (index < minOf(2, uiState.cards.size - 1)) {
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 4. Card Status
            if (uiState.selectedCard != null) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Card Status",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B),
                        fontSize = 14.sp
                    )
                    Text(
                        text = uiState.selectedCard!!.status.name.replace("_", " "),
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        color = when(uiState.selectedCard!!.status) {
                            com.example.mybank.data.model.CardStatus.ACTIVE -> Color(0xFF10B981)
                            com.example.mybank.data.model.CardStatus.FROZEN -> Color(0xFF3B82F6)
                            com.example.mybank.data.model.CardStatus.BLOCKED -> Color(0xFFEF4444)
                            else -> Color.White
                        },
                        fontSize = 24.sp,
                        letterSpacing = (-0.5).sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 5. Quick Actions
            if (uiState.selectedCard != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val isFrozen = uiState.selectedCard!!.status == com.example.mybank.data.model.CardStatus.FROZEN
                        QuickActionButton(
                            icon = if (isFrozen) Icons.Outlined.AcUnit else Icons.Outlined.AcUnit,
                            label = if (isFrozen) "Unfreeze" else "Freeze",
                            onClick = {
                                if (isFrozen) {
                                    viewModel.unfreezeCard(uiState.selectedCard!!.id)
                                } else {
                                    viewModel.freezeCard(uiState.selectedCard!!.id)
                                }
                            }
                        )
                        QuickActionButton(
                            icon = Icons.Outlined.Visibility,
                            label = "Details",
                            onClick = { onCardDetails(uiState.selectedCard!!.id) }
                        )
                        QuickActionButton(
                            icon = Icons.Outlined.AddCard,
                            label = "Add Card",
                            onClick = onAddCard
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 6. Settings List
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF1C1F27)) // Surface dark
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
            ) {
                SettingsListItem(
                    icon = Icons.Outlined.Tune,
                    iconBgColor = Color(0xFF3B82F6).copy(alpha = 0.2f),
                    iconColor = Color(0xFF60A5FA),
                    title = "Card Settings",
                    subtitle = "Limits, online payments, contactless",
                    showDivider = true,
                    onClick = { uiState.selectedCard?.let { onCardDetails(it.id) } }
                )
                SettingsListItem(
                    icon = Icons.Outlined.Shield,
                    iconBgColor = Color(0xFF10B981).copy(alpha = 0.2f),
                    iconColor = Color(0xFF34D399),
                    title = "Security",
                    subtitle = "Change PIN, block magnetic stripe",
                    showDivider = true,
                    onClick = { uiState.selectedCard?.let { onCardDetails(it.id) } }
                )
                SettingsListItem(
                    icon = Icons.Outlined.History,
                    iconBgColor = Color(0xFFA855F7).copy(alpha = 0.2f),
                    iconColor = Color(0xFFC084FC),
                    title = "Recent Transactions",
                    subtitle = "View your last 30 days activity",
                    showDivider = false,
                    onClick = {}
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            // 7. Order New Card Button
            Button(
                onClick = onAddCard,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 2.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Order New Card",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun CardsHeader(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Back Button
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.05f)) // Hover effect simulation
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        // Title
        Text(
            text = "My Cards",
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = 18.sp
        )

        // Profile Pic
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E293B)) // surface-dark
                .border(2.dp, Color.Transparent, CircleShape)
        ) {
            AsyncImage(
                model = "https://lh3.googleusercontent.com/aida-public/AB6AXuA-AYyscTdXZXp7-fCGxmSTAbNZDW0HrERSNza7Q1ce-yaDxZ0Dnm_H2kljBXTCLk6gCdMEqze421InNySB5PMr72VEwcATg6slifjzxVr7rCvZjhwL3lhiEjL9uiN-1h2mb6KxHeuiXHZYe5GDU3r7n6dhZpX39MSE8qg-A9tlfFNBjNtUGRZ3Xuuc8Tre7otPx5Msbogi9FflKWG0Dm3ROxoTskCmpYnCOEL5hOoBcxkP43V7HatAaY2KenvL2WqYB54kQHvv6SM",
                contentDescription = "Profile",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1C1F27)) // Surface dark
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFFCBD5E1), // Slate 300
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = Color(0xFF64748B) // Slate 500 (matched HTML)
        )
    }
}

@Composable
fun SettingsListItem(
    icon: ImageVector,
    iconBgColor: Color,
    iconColor: Color,
    title: String,
    subtitle: String,
    showDivider: Boolean,
    onClick: () -> Unit = {}
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBgColor)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8) // Slate 400
                )
            }

            // Arrow
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF94A3B8) // Slate 400
            )
        }
        
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                thickness = 1.dp,
                color = Color.White.copy(alpha = 0.05f)
            )
        }
    }
}
