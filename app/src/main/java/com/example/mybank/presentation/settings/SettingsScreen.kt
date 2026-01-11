package com.example.mybank.presentation.settings

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mybank.presentation.auth.AuthViewModel
import kotlinx.coroutines.delay

// ====================== PREMIUM COLORS ======================
private val PurplePrimary = Color(0xFF8B7FFF)
private val PurpleLight = Color(0xFFB8A9FF)
private val PurpleMedium = Color(0xFF9B8FFF)
private val PurpleVibrant = Color(0xFF7B6FEE)
private val PurpleDark = Color(0xFF5B4FCC)
private val TextPrimary = Color(0xFF1A1A2E)
private val TextSecondary = Color(0xFF9CA3AF)
private val TextWhite = Color(0xFFFFFFFF)
private val SuccessGreen = Color(0xFF00C566)
private val ErrorRed = Color(0xFFFF4757)
private val WarningOrange = Color(0xFFFF9F43)
private val InfoBlue = Color(0xFF4FD8FF)
private val CardBgWhite = Color(0xFFFAFAFC)
private val DividerColor = Color(0xFFE5E7EB)

// Premium background gradient
private val BackgroundGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFE8E0FF),
        Color(0xFFF0ECFF),
        Color(0xFFF8F6FF),
        Color(0xFFFFFEFF)
    )
)

// ====================== MAIN SCREEN ======================

/**
 * SettingsScreen - Premium Banking Settings Screen
 * 
 * This screen displays all app settings with a modern, elegant UI.
 * Features include:
 * - User profile section
 * - Security & Privacy settings
 * - Notification preferences
 * - App preferences
 * - Help & Support
 * - Logout functionality
 */
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    // Animation states
    var isVisible by remember { mutableStateOf(false) }
    
    // Context for Toast messages
    val context = LocalContext.current
    
    // Trigger entrance animations
    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGradient)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // ========== HEADER ==========
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { -40 }
                ) {
                    SettingsHeader(onBackClick = onNavigateBack)
                }
            }
            
            // ========== PROFILE CARD ==========
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(600, 100)) + slideInVertically(tween(600, 100)) { -30 }
                ) {
                    ProfileCard(
                        userName = uiState.userName.ifBlank { "James Thompson" },
                        userEmail = uiState.userEmail.ifBlank { "james.thompson@email.com" },
                        onEditClick = {
                            Toast.makeText(context, "Edit profile", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    )
                }
            }
            
            // ========== ACCOUNT & SECURITY ==========
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(600, 200)) + slideInVertically(tween(600, 200)) { -20 }
                ) {
                    SettingsSection(
                        title = "ACCOUNT & SECURITY",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        SettingsCard {
                            PremiumSettingsItem(
                                icon = Icons.Outlined.Person,
                                title = "Personal Information",
                                subtitle = "Name, email, phone",
                                iconBgColor = Color(0xFFE8E0FF),
                                iconColor = PurplePrimary,
                                onClick = { Toast.makeText(context, "Personal Info", Toast.LENGTH_SHORT).show() }
                            )
                            
                            SettingsDivider()
                            
                            PremiumSettingsItem(
                                icon = Icons.Outlined.Lock,
                                title = "Change Password",
                                subtitle = "Update your password",
                                iconBgColor = Color(0xFFE3F2FD),
                                iconColor = InfoBlue,
                                onClick = { Toast.makeText(context, "Change Password", Toast.LENGTH_SHORT).show() }
                            )
                            
                            SettingsDivider()
                            
                            PremiumSettingsItem(
                                icon = Icons.Outlined.Fingerprint,
                                title = "Biometric Login",
                                subtitle = "Use fingerprint or Face ID",
                                iconBgColor = Color(0xFFE5F9E7),
                                iconColor = SuccessGreen,
                                trailing = {
                                    PremiumSwitch(
                                        checked = uiState.biometricEnabled,
                                        onCheckedChange = { viewModel.toggleBiometric() }
                                    )
                                }
                            )
                            
                            SettingsDivider()
                            
                            PremiumSettingsItem(
                                icon = Icons.Outlined.Security,
                                title = "Two-Factor Authentication",
                                subtitle = "Extra security for your account",
                                iconBgColor = Color(0xFFFFF3E0),
                                iconColor = WarningOrange,
                                onClick = { Toast.makeText(context, "2FA Settings", Toast.LENGTH_SHORT).show() }
                            )
                        }
                    }
                }
            }
            
            // ========== NOTIFICATIONS ==========
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(600, 300)) + slideInVertically(tween(600, 300)) { -20 }
                ) {
                    SettingsSection(
                        title = "NOTIFICATIONS",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        SettingsCard {
                            PremiumSettingsItem(
                                icon = Icons.Outlined.Notifications,
                                title = "Push Notifications",
                                subtitle = "Receive alerts and updates",
                                iconBgColor = Color(0xFFE8E0FF),
                                iconColor = PurplePrimary,
                                trailing = {
                                    PremiumSwitch(
                                        checked = uiState.notificationsEnabled,
                                        onCheckedChange = { viewModel.toggleNotifications() }
                                    )
                                }
                            )
                            
                            SettingsDivider()
                            
                            PremiumSettingsItem(
                                icon = Icons.Outlined.Email,
                                title = "Email Notifications",
                                subtitle = "Monthly statements & offers",
                                iconBgColor = Color(0xFFE3F2FD),
                                iconColor = InfoBlue,
                                trailing = {
                                    PremiumSwitch(
                                        checked = true,
                                        onCheckedChange = { }
                                    )
                                }
                            )
                            
                            SettingsDivider()
                            
                            PremiumSettingsItem(
                                icon = Icons.Outlined.Sms,
                                title = "SMS Alerts",
                                subtitle = "Transaction alerts via SMS",
                                iconBgColor = Color(0xFFE5F9E7),
                                iconColor = SuccessGreen,
                                trailing = {
                                    PremiumSwitch(
                                        checked = true,
                                        onCheckedChange = { }
                                    )
                                }
                            )
                        }
                    }
                }
            }
            
            // ========== APP PREFERENCES ==========
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(600, 400)) + slideInVertically(tween(600, 400)) { -20 }
                ) {
                    SettingsSection(
                        title = "APP PREFERENCES",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        SettingsCard {
                            PremiumSettingsItem(
                                icon = Icons.Outlined.DarkMode,
                                title = "Dark Mode",
                                subtitle = "Switch to dark theme",
                                iconBgColor = Color(0xFF2E2650).copy(alpha = 0.15f),
                                iconColor = Color(0xFF2E2650),
                                trailing = {
                                    PremiumSwitch(
                                        checked = uiState.isDarkTheme,
                                        onCheckedChange = { viewModel.toggleDarkTheme() }
                                    )
                                }
                            )
                            
                            SettingsDivider()
                            
                            PremiumSettingsItem(
                                icon = Icons.Outlined.Language,
                                title = "Language",
                                subtitle = "English (US)",
                                iconBgColor = Color(0xFFE8E0FF),
                                iconColor = PurplePrimary,
                                onClick = { Toast.makeText(context, "Language Settings", Toast.LENGTH_SHORT).show() }
                            )
                            
                            SettingsDivider()
                            
                            PremiumSettingsItem(
                                icon = Icons.Outlined.CurrencyExchange,
                                title = "Currency",
                                subtitle = "USD ($)",
                                iconBgColor = Color(0xFFE5F9E7),
                                iconColor = SuccessGreen,
                                onClick = { Toast.makeText(context, "Currency Settings", Toast.LENGTH_SHORT).show() }
                            )
                        }
                    }
                }
            }
            
            // ========== PAYMENT SETTINGS ==========
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(600, 500)) + slideInVertically(tween(600, 500)) { -20 }
                ) {
                    SettingsSection(
                        title = "PAYMENT SETTINGS",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        SettingsCard {
                            PremiumSettingsItem(
                                icon = Icons.Outlined.CreditCard,
                                title = "Manage Cards",
                                subtitle = "Add or remove payment cards",
                                iconBgColor = Color(0xFFE8E0FF),
                                iconColor = PurplePrimary,
                                onClick = { Toast.makeText(context, "Manage Cards", Toast.LENGTH_SHORT).show() }
                            )
                            
                            SettingsDivider()
                            
                            PremiumSettingsItem(
                                icon = Icons.Outlined.AccountBalance,
                                title = "Bank Accounts",
                                subtitle = "Linked bank accounts",
                                iconBgColor = Color(0xFFE3F2FD),
                                iconColor = InfoBlue,
                                onClick = { Toast.makeText(context, "Bank Accounts", Toast.LENGTH_SHORT).show() }
                            )
                            
                            SettingsDivider()
                            
                            PremiumSettingsItem(
                                icon = Icons.Outlined.Receipt,
                                title = "Transaction Limits",
                                subtitle = "Set daily spending limits",
                                iconBgColor = Color(0xFFFFF3E0),
                                iconColor = WarningOrange,
                                onClick = { Toast.makeText(context, "Transaction Limits", Toast.LENGTH_SHORT).show() }
                            )
                        }
                    }
                }
            }
            
            // ========== HELP & SUPPORT ==========
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(600, 600)) + slideInVertically(tween(600, 600)) { -20 }
                ) {
                    SettingsSection(
                        title = "HELP & SUPPORT",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        SettingsCard {
                            PremiumSettingsItem(
                                icon = Icons.Outlined.Help,
                                title = "Help Center",
                                subtitle = "FAQs and guides",
                                iconBgColor = Color(0xFFE8E0FF),
                                iconColor = PurplePrimary,
                                onClick = { Toast.makeText(context, "Help Center", Toast.LENGTH_SHORT).show() }
                            )
                            
                            SettingsDivider()
                            
                            PremiumSettingsItem(
                                icon = Icons.Outlined.Chat,
                                title = "Live Chat",
                                subtitle = "Chat with support 24/7",
                                iconBgColor = Color(0xFFE5F9E7),
                                iconColor = SuccessGreen,
                                onClick = { Toast.makeText(context, "Live Chat", Toast.LENGTH_SHORT).show() }
                            )
                            
                            SettingsDivider()
                            
                            PremiumSettingsItem(
                                icon = Icons.Outlined.Phone,
                                title = "Call Us",
                                subtitle = "+1 (800) 123-4567",
                                iconBgColor = Color(0xFFE3F2FD),
                                iconColor = InfoBlue,
                                onClick = { Toast.makeText(context, "Call Support", Toast.LENGTH_SHORT).show() }
                            )
                            
                            SettingsDivider()
                            
                            PremiumSettingsItem(
                                icon = Icons.Outlined.BugReport,
                                title = "Report a Problem",
                                subtitle = "Help us improve",
                                iconBgColor = Color(0xFFFFE5E5),
                                iconColor = ErrorRed,
                                onClick = { Toast.makeText(context, "Report Problem", Toast.LENGTH_SHORT).show() }
                            )
                        }
                    }
                }
            }
            
            // ========== ABOUT & LEGAL ==========
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(600, 700)) + slideInVertically(tween(600, 700)) { -20 }
                ) {
                    SettingsSection(
                        title = "ABOUT & LEGAL",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        SettingsCard {
                            PremiumSettingsItem(
                                icon = Icons.Outlined.Info,
                                title = "About MyBank",
                                subtitle = "Version 1.0.0",
                                iconBgColor = Color(0xFFE8E0FF),
                                iconColor = PurplePrimary,
                                onClick = { Toast.makeText(context, "About", Toast.LENGTH_SHORT).show() }
                            )
                            
                            SettingsDivider()
                            
                            PremiumSettingsItem(
                                icon = Icons.Outlined.Description,
                                title = "Terms of Service",
                                subtitle = "Read our terms",
                                iconBgColor = Color(0xFFF5F5F7),
                                iconColor = TextSecondary,
                                onClick = { Toast.makeText(context, "Terms", Toast.LENGTH_SHORT).show() }
                            )
                            
                            SettingsDivider()
                            
                            PremiumSettingsItem(
                                icon = Icons.Outlined.PrivacyTip,
                                title = "Privacy Policy",
                                subtitle = "How we handle your data",
                                iconBgColor = Color(0xFFF5F5F7),
                                iconColor = TextSecondary,
                                onClick = { Toast.makeText(context, "Privacy", Toast.LENGTH_SHORT).show() }
                            )
                            
                            SettingsDivider()
                            
                            PremiumSettingsItem(
                                icon = Icons.Outlined.Star,
                                title = "Rate Us",
                                subtitle = "Love the app? Rate us!",
                                iconBgColor = Color(0xFFFFF3E0),
                                iconColor = WarningOrange,
                                onClick = { Toast.makeText(context, "Rate App", Toast.LENGTH_SHORT).show() }
                            )
                        }
                    }
                }
            }
            
            // ========== LOGOUT BUTTON ==========
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(600, 800)) + slideInVertically(tween(600, 800)) { 20 }
                ) {
                    LogoutButton(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                    )
                }
            }
            
            // ========== APP VERSION ==========
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(600, 900))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "MyBank v1.0.0",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Made with ❤️ for secure banking",
                            fontSize = 11.sp,
                            color = TextSecondary.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
        
        // ========== LOGOUT DIALOG ==========
        if (showLogoutDialog) {
            PremiumLogoutDialog(
                onDismiss = { showLogoutDialog = false },
                onConfirm = {
                    showLogoutDialog = false
                    authViewModel.logout()
                    onNavigateToLogin()
                }
            )
        }
    }
}

// ====================== HEADER ======================

@Composable
private fun SettingsHeader(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back button
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(CardBgWhite)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onBackClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = TextPrimary,
                modifier = Modifier.size(22.dp)
            )
        }
        
        // Title
        Text(
            text = "Settings",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            letterSpacing = (-0.5).sp
        )
        
        // Placeholder for alignment
        Box(modifier = Modifier.size(44.dp))
    }
}

// ====================== PROFILE CARD ======================

@Composable
private fun ProfileCard(
    userName: String,
    userEmail: String,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = PurplePrimary.copy(alpha = 0.2f)
            ),
        shape = RoundedCornerShape(24.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(PurplePrimary, PurpleMedium)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userName.take(2).uppercase(),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            }
            
            // User info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = userName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = userEmail,
                    fontSize = 14.sp,
                    color = TextSecondary
                )
                
                // Verified badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Verified,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Verified Account",
                        fontSize = 12.sp,
                        color = SuccessGreen,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Edit button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(PurplePrimary.copy(alpha = 0.1f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onEditClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Edit profile",
                    tint = PurplePrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ====================== SETTINGS SECTION ======================

@Composable
private fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        content()
    }
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = Color.Black.copy(alpha = 0.08f)
            ),
        shape = RoundedCornerShape(20.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            content = content
        )
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 72.dp, end = 16.dp),
        color = DividerColor
    )
}

// ====================== PREMIUM SETTINGS ITEM ======================

@Composable
private fun PremiumSettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconBgColor: Color,
    iconColor: Color,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "itemScale"
    )
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = onClick != null
            ) {
                if (onClick != null) {
                    isPressed = true
                    onClick()
                }
            }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon container
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }
        
        // Text content
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = TextSecondary
            )
        }
        
        // Trailing content
        if (trailing != null) {
            trailing()
        } else if (onClick != null) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(24.dp)
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

// ====================== PREMIUM SWITCH ======================

@Composable
private fun PremiumSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = PurplePrimary,
            uncheckedThumbColor = Color.White,
            uncheckedTrackColor = TextSecondary.copy(alpha = 0.3f),
            uncheckedBorderColor = Color.Transparent
        )
    )
}

// ====================== LOGOUT BUTTON ======================

@Composable
private fun LogoutButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "buttonScale"
    )
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isPressed = true
                onClick()
            },
        shape = RoundedCornerShape(16.dp),
        color = ErrorRed.copy(alpha = 0.1f),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = Brush.linearGradient(listOf(ErrorRed.copy(alpha = 0.3f), ErrorRed.copy(alpha = 0.3f)))
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Logout,
                contentDescription = null,
                tint = ErrorRed,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Log Out",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = ErrorRed
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

// ====================== LOGOUT DIALOG ======================

@Composable
private fun PremiumLogoutDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        icon = {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(ErrorRed.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Logout,
                    contentDescription = null,
                    tint = ErrorRed,
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        title = {
            Text(
                text = "Log Out?",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = "Are you sure you want to log out of your account?",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ErrorRed
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Log Out",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Cancel",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
            }
        }
    )
}

// ====================== PREVIEWS ======================

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SettingsScreenPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundGradient)
        ) {
            Column {
                SettingsHeader(onBackClick = {})
                ProfileCard(
                    userName = "James Thompson",
                    userEmail = "james.thompson@email.com",
                    onEditClick = {},
                    modifier = Modifier.padding(20.dp)
                )
            }
        }
    }
}
