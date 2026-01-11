package com.example.mybank.presentation.main

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mybank.navigation.Screen
import com.example.mybank.presentation.analytics.AnalyticsScreen
import com.example.mybank.presentation.cards.CardsScreen
import com.example.mybank.presentation.components.GlassBottomNavigation
import com.example.mybank.presentation.home.HomeScreen
import com.example.mybank.presentation.profile.ProfileScreen
import com.example.mybank.ui.theme.*
import dev.chrisbanes.haze.HazeState

@Composable
fun MainScreen(
    mainNavController: NavHostController,
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToSendMoney: () -> Unit = {},
    onNavigateToAddMoney: () -> Unit = {},
    onNavigateToInternalTransfer: () -> Unit = {},
    onNavigateToAccounts: () -> Unit = {}
) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"
    
    val hazeState = remember { HazeState() }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0.dp),
        bottomBar = {
            GlassBottomNavigation(
                currentRoute = currentRoute,
                hazeState = hazeState,
                onNavigate = { route ->
                    bottomNavController.navigate(route) {
                        popUpTo("home") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(GlassBackgroundGradient)
        ) {
            NavHost(
                navController = bottomNavController,
                startDestination = "home",
                modifier = Modifier.fillMaxSize()
            ) {
                composable(
                    route = "home",
                    enterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { -it },
                            animationSpec = tween(
                                durationMillis = 300,
                                easing = FastOutSlowInEasing
                            )
                        ) + fadeIn(
                            animationSpec = tween(300)
                        )
                    },
                    exitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { -it },
                            animationSpec = tween(
                                durationMillis = 300,
                                easing = FastOutSlowInEasing
                            )
                        ) + fadeOut(
                            animationSpec = tween(300)
                        )
                    }
                ) {
                    HomeScreen(
                        hazeState = hazeState,
                        onNavigateToTransactions = { /* Navigate to transactions */ },
                        onNavigateToNotifications = onNavigateToNotifications,
                        onNavigateToCards = { bottomNavController.navigate("cards") },
                        onNavigateToStats = { bottomNavController.navigate("analytics") },
                        onNavigateToProfile = { bottomNavController.navigate("profile") },
                        onNavigateToSendMoney = onNavigateToSendMoney,
                        onNavigateToAddMoney = onNavigateToAddMoney,
                        onNavigateToInternalTransfer = onNavigateToInternalTransfer,
                        onNavigateToAccounts = onNavigateToAccounts
                    )
                }

                composable(
                    route = "cards",
                    enterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { it },
                            animationSpec = tween(
                                durationMillis = 300,
                                easing = FastOutSlowInEasing
                            )
                        ) + fadeIn(
                            animationSpec = tween(300)
                        )
                    },
                    exitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { it },
                            animationSpec = tween(
                                durationMillis = 300,
                                easing = FastOutSlowInEasing
                            )
                        ) + fadeOut(
                            animationSpec = tween(300)
                        )
                    }
                ) {
                    CardsScreen(
                        onNavigateToHome = { 
                            bottomNavController.navigate("home") {
                                popUpTo("home") { 
                                    inclusive = false
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onNavigateToTransfers = { /* Navigate to transfers if needed */ },
                        onNavigateToMore = { mainNavController.navigate(Screen.Settings.route) },
                        onNavigateToSettings = { mainNavController.navigate(Screen.Settings.route) },
                        onNavigateToAccounts = { mainNavController.navigate(Screen.Accounts.route) },
                        onAddCard = { mainNavController.navigate(Screen.CreateCard.route) },
                        onNavigateToStats = { bottomNavController.navigate("analytics") },
                        onNavigateToProfile = { bottomNavController.navigate("profile") },
                        onCardDetails = { cardId -> mainNavController.navigate(Screen.CardDetails.route + "/$cardId") }
                    )
                }

                composable(
                    route = "analytics",
                    enterTransition = {
                        slideInVertically(
                            initialOffsetY = { it / 2 },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        ) + fadeIn(
                            animationSpec = tween(400)
                        ) + scaleIn(
                            initialScale = 0.95f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    },
                    exitTransition = {
                        slideOutVertically(
                            targetOffsetY = { it / 2 },
                            animationSpec = tween(
                                durationMillis = 300,
                                easing = FastOutSlowInEasing
                            )
                        ) + fadeOut(
                            animationSpec = tween(300)
                        ) + scaleOut(
                            targetScale = 0.95f,
                            animationSpec = tween(300)
                        )
                    }
                ) {
                    AnalyticsScreen(
                        navController = bottomNavController
                    )
                }

                composable(
                    route = "profile",
                    enterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { it },
                            animationSpec = tween(
                                durationMillis = 300,
                                easing = FastOutSlowInEasing
                            )
                        ) + fadeIn(
                            animationSpec = tween(300)
                        )
                    },
                    exitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { it },
                            animationSpec = tween(
                                durationMillis = 300,
                                easing = FastOutSlowInEasing
                            )
                        ) + fadeOut(
                            animationSpec = tween(300)
                        )
                    }
                ) {
                    ProfileScreen(
                        navController = bottomNavController,
                        onNavigateToEditProfile = { /* Navigate to edit profile */ },
                        onNavigateToPersonalDetails = { /* Navigate to personal details */ },
                        onNavigateToStatements = { /* Navigate to statements */ },
                        onNavigateToLimitsFees = { /* Navigate to limits & fees */ },
                        onNavigateToSettings = { mainNavController.navigate(Screen.Settings.route) },
                        onLogout = {
                            mainNavController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}
