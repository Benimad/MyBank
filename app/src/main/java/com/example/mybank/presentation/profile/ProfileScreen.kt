package com.example.mybank.presentation.profile

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.mybank.presentation.profile.components.*
import com.example.mybank.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavHostController,
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToPersonalDetails: () -> Unit = {},
    onNavigateToStatements: () -> Unit = {},
    onNavigateToLimitsFees: () -> Unit = {},
    onNavigateToChangePassword: () -> Unit = {},
    on2FASettings: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToAppearance: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            viewModel.clearError()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundDark.copy(alpha = 0.9f))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.navigate("home") }) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    
                    Text(
                        text = "Profile",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color.White,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.size(48.dp))
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(bottom = 100.dp)
            ) {
                AnimatedVisibility(
                    visible = !uiState.isLoading,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        ProfileHeaderSection(
                            userProfile = uiState.userProfile,
                            onEditProfile = onNavigateToEditProfile
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        AccountSection(
                            onPersonalDetails = onNavigateToPersonalDetails,
                            onStatements = onNavigateToStatements,
                            onLimitsFees = onNavigateToLimitsFees
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        SecuritySection(
                            isFaceIdEnabled = uiState.userProfile?.isFaceIdEnabled ?: false,
                            is2FAEnabled = uiState.userProfile?.is2FAEnabled ?: false,
                            onChangePassword = onNavigateToChangePassword,
                            onToggleFaceId = { viewModel.toggleFaceId(it) },
                            on2FASettings = on2FASettings
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        PreferencesSection(
                            onNotifications = onNavigateToNotifications,
                            onAppearance = onNavigateToAppearance
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        LogoutButton(
                            onClick = { showLogoutDialog = true }
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "MyBank v2.4.0",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = TextGray600,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 100.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                }
            }
        }
    }

    if (showLogoutDialog) {
        LogoutConfirmationDialog(
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                showLogoutDialog = false
                viewModel.logout(onLogout)
            }
        )
    }
}

@Composable
private fun ProfileHeaderSection(
    userProfile: com.example.mybank.data.model.UserProfile?,
    onEditProfile: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        val scale by rememberInfiniteTransition(label = "avatar scale").animateFloat(
            initialValue = 1f,
            targetValue = 1.02f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "avatar scale"
        )
        
        ProfileAvatar(
            photoUrl = userProfile?.photoUrl,
            size = 100,
            showVerifiedBadge = true,
            modifier = Modifier
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = userProfile?.displayName ?: "User",
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = userProfile?.email ?: "",
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = TextGray400
        )
        
        if (userProfile?.isPremium == true) {
            Spacer(modifier = Modifier.height(12.dp))
            PremiumBadge()
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedButton(
            onClick = onEditProfile,
            modifier = Modifier
                .width(200.dp)
                .height(40.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
        ) {
            Text(
                text = "Edit Profile",
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun AccountSection(
    onPersonalDetails: () -> Unit,
    onStatements: () -> Unit,
    onLimitsFees: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        SettingsSectionHeader("ACCOUNT")
        
        SettingsCard {
            SettingsItem(
                icon = Icons.Outlined.Person,
                title = "Personal Details",
                iconBackgroundColor = BlueIconBgDark,
                iconTintColor = BlueIconTintDark,
                onClick = onPersonalDetails
            )
            
            SettingsItem(
                icon = Icons.Outlined.Description,
                title = "Statements & Documents",
                iconBackgroundColor = Color(0xFF581C87).copy(alpha = 0.3f),
                iconTintColor = Color(0xFFC084FC),
                onClick = onStatements
            )
            
            SettingsItem(
                icon = Icons.Outlined.AccountBalanceWallet,
                title = "Limits & Fees",
                iconBackgroundColor = OrangeIconBgDark,
                iconTintColor = OrangeIconTintDark,
                onClick = onLimitsFees,
                showDivider = false
            )
        }
    }
}

@Composable
private fun SecuritySection(
    isFaceIdEnabled: Boolean,
    is2FAEnabled: Boolean,
    onChangePassword: () -> Unit,
    onToggleFaceId: (Boolean) -> Unit,
    on2FASettings: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        SettingsSectionHeader("SECURITY")
        
        SettingsCard {
            SettingsItem(
                icon = Icons.Outlined.Lock,
                title = "Change Password",
                iconBackgroundColor = GreenIconBgDark,
                iconTintColor = GreenIconTintDark,
                onClick = onChangePassword
            )
            
            SettingsItem(
                icon = Icons.Outlined.Face,
                title = "Face ID",
                iconBackgroundColor = Color(0xFF134E4A).copy(alpha = 0.3f),
                iconTintColor = Color(0xFF5EEAD4),
                hasToggle = true,
                isToggleOn = isFaceIdEnabled,
                onToggle = onToggleFaceId
            )
            
            SettingsItem(
                icon = Icons.Outlined.Shield,
                title = "Two-Factor Authentication",
                iconBackgroundColor = RedIconBgDark,
                iconTintColor = RedIconTintDark,
                badge = if (is2FAEnabled) "Enabled" else "Disabled",
                badgeColor = if (is2FAEnabled) SuccessGreen else TextGray500,
                onClick = on2FASettings,
                showDivider = false
            )
        }
    }
}

@Composable
private fun PreferencesSection(
    onNotifications: () -> Unit,
    onAppearance: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        SettingsSectionHeader("PREFERENCES")
        
        SettingsCard {
            SettingsItem(
                icon = Icons.Outlined.Notifications,
                title = "Notifications",
                iconBackgroundColor = TextGray700,
                iconTintColor = TextGray400,
                onClick = onNotifications
            )
            
            SettingsItem(
                icon = Icons.Outlined.DarkMode,
                title = "Appearance",
                iconBackgroundColor = TextGray700,
                iconTintColor = TextGray400,
                subtitle = "System Default",
                onClick = onAppearance,
                showDivider = false
            )
        }
    }
}

@Composable
private fun LogoutButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AlertRed.copy(alpha = 0.1f),
            contentColor = AlertRed
        )
    ) {
        Icon(
            imageVector = Icons.Outlined.Logout,
            contentDescription = "Logout",
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Log Out",
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun LogoutConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = {
            Text(
                text = "Log Out",
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            Text(
                text = "Are you sure you want to log out?",
                fontFamily = InterFontFamily,
                color = TextGray300
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = AlertRed)
            ) {
                Text(
                    text = "Log Out",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
            ) {
                Text(
                    text = "Cancel",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    )
}
