package com.example.mybank.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.mybank.presentation.analytics.AnalyticsScreen
import com.example.mybank.presentation.auth.AuthViewModel
import com.example.mybank.presentation.auth.LoginScreen
import com.example.mybank.presentation.auth.RegisterScreen
import com.example.mybank.presentation.auth.SplashScreen
import com.example.mybank.presentation.home.HomeScreen
import com.example.mybank.presentation.main.MainScreen
import com.example.mybank.presentation.cards.CardsScreen
import com.example.mybank.presentation.notifications.NotificationsScreen
import com.example.mybank.presentation.profile.EditProfileScreen
import com.example.mybank.presentation.profile.ProfileScreen
import com.example.mybank.presentation.settings.SettingsScreen
import com.example.mybank.presentation.transfers.TransfersScreen
import com.example.mybank.presentation.transactions.AllTransactionsScreen
import com.example.mybank.presentation.send_money.SendMoneyScreen
import com.example.mybank.presentation.send_money.SendMoneyConfirmScreen
import com.example.mybank.presentation.send_money.SendMoneySuccessScreen
import com.example.mybank.presentation.add_money.*
import com.example.mybank.presentation.internal_transfer.InternalTransferScreen
import com.example.mybank.presentation.internal_transfer.InternalTransferSuccessScreen
import com.example.mybank.presentation.create_pot.CreatePotScreen
import com.example.mybank.presentation.accounts.MyAccountsScreen
import com.example.mybank.presentation.account_details.AccountDetailsScreen
import com.example.mybank.presentation.transaction_details.TransactionDetailsScreen
import com.example.mybank.presentation.bill_payments.BillPaymentsScreen
import com.example.mybank.presentation.bill_payments.AddBillerScreen
import com.example.mybank.presentation.bill_payments.PayBillScreen
import com.example.mybank.presentation.savings_goals.SavingsGoalsScreen
import com.example.mybank.presentation.savings_goals.AutomateSavingsScreen
import com.example.mybank.presentation.statements.BankStatementScreen
import com.example.mybank.presentation.statements.StatementsScreen
import com.example.mybank.presentation.settings.LimitsFeesScreen
import com.example.mybank.presentation.settings.ChangePasswordScreen
import com.example.mybank.presentation.settings.TwoFactorAuthScreen
import com.example.mybank.presentation.settings.AppearanceScreen
import com.example.mybank.presentation.cards.CreateCardScreen
import com.example.mybank.presentation.cards.CardDetailsScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Splash.route) {
            val authViewModel: AuthViewModel = hiltViewModel()
            val authState by authViewModel.authState.collectAsState()
            
            SplashScreen(
                isAuthenticated = authState.isAuthenticated,
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Login.route) {
            val authViewModel: AuthViewModel = hiltViewModel()
            val authState by authViewModel.authState.collectAsState()
            
            LoginScreen(
                authState = authState,
                onLogin = { email, password ->
                    authViewModel.login(email, password)
                },
                onGoogleSignIn = {
                    // Google Sign-In is handled in MainActivity
                },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onForgotPassword = { navController.navigate(Screen.ForgotPassword.route) }
            )
            
            // Navigate to home on successful login
            LaunchedEffect(authState.isAuthenticated) {
                if (authState.isAuthenticated) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }
        
        composable(Screen.Register.route) {
            val authViewModel: AuthViewModel = hiltViewModel()
            val authState by authViewModel.authState.collectAsState()
            
            RegisterScreen(
                authState = authState,
                onRegister = { email, password, name ->
                    authViewModel.register(email, password, name)
                },
                onNavigateToLogin = { navController.popBackStack() },
                onNavigateBack = { navController.popBackStack() }
            )
            
            // Navigate to home on successful registration
            LaunchedEffect(authState.isAuthenticated) {
                if (authState.isAuthenticated) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }
        
        composable(Screen.Home.route) {
            MainScreen(
                mainNavController = navController,
                onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                onNavigateToSendMoney = { navController.navigate(Screen.SendMoney.route) },
                onNavigateToAddMoney = { navController.navigate(Screen.AddMoney.route) },
                onNavigateToInternalTransfer = { navController.navigate(Screen.InternalTransfer.route) },
                onNavigateToAccounts = { navController.navigate(Screen.Accounts.route) }
            )
        }
        
        composable(Screen.Accounts.route) {
            MyAccountsScreen(
                onNavigateBack = { navController.popBackStack() },
                onCreatePot = { navController.navigate(Screen.CreatePot.route) },
                onAccountClick = { accountId ->
                    navController.navigate(Screen.AccountDetails.createRoute(accountId))
                }
            )
        }
        
        composable(Screen.CreatePot.route) {
            CreatePotScreen(
                onNavigateBack = { navController.popBackStack() },
                onPotCreated = {
                    navController.navigate(Screen.Accounts.route) {
                        popUpTo(Screen.Accounts.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(
            route = Screen.AccountDetails.route,
            arguments = listOf(navArgument("accountId") { type = NavType.StringType })
        ) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getString("accountId") ?: ""
            AccountDetailsScreen(
                accountId = accountId,
                onNavigateBack = { navController.popBackStack() },
                onTransactionClick = { transactionId ->
                    navController.navigate(Screen.TransactionDetails.createRoute(transactionId))
                },
                onTransferMoney = {
                    navController.navigate(Screen.InternalTransfer.route)
                }
            )
        }
        
        composable(
            route = Screen.TransactionDetails.route,
            arguments = listOf(navArgument("transactionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getString("transactionId") ?: ""
            TransactionDetailsScreen(
                transactionId = transactionId,
                onNavigateBack = { navController.popBackStack() },
                onDisputeTransaction = {
                }
            )
        }
        
        composable(
            route = Screen.Transactions.route,
            arguments = listOf(navArgument("accountId") { type = NavType.StringType })
        ) {
        }
        
        composable(Screen.Notifications.route) {
            NotificationsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Analytics.route) {
            AnalyticsScreen(
                navController = navController
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Cards.route) {
            CardsScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToTransfers = {
                    navController.navigate(Screen.Transfers.route)
                },
                onNavigateToMore = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToAccounts = {
                    navController.navigate(Screen.Accounts.route)
                },
                onAddCard = {
                    navController.navigate(Screen.CreateCard.route)
                },
                onNavigateToStats = { navController.navigate(Screen.Analytics.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onCardDetails = { cardId ->
                    navController.navigate(Screen.CardDetails.createRoute(cardId))
                }
            )
        }
        
        composable(Screen.Transfers.route) {
            TransfersScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Transfers.route) { inclusive = true }
                    }
                },
                onNavigateToCards = {
                    navController.navigate(Screen.Cards.route) {
                        popUpTo(Screen.Transfers.route) { inclusive = true }
                    }
                },
                onNavigateToMore = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToAccounts = {
                    navController.navigate(Screen.Accounts.route)
                }
            )
        }
        
        composable(Screen.AllTransactions.route) {
            AllTransactionsScreen(
                onNavigateBack = { navController.popBackStack() },
                onTransactionClick = { transactionId ->
                    navController.navigate(Screen.TransactionDetails.createRoute(transactionId))
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                navController = navController,
                onNavigateToEditProfile = { navController.navigate("edit_profile") },
                onNavigateToPersonalDetails = { navController.navigate("edit_profile") },
                onNavigateToStatements = { navController.navigate(Screen.Statements.route) },
                onNavigateToLimitsFees = { navController.navigate(Screen.LimitsFees.route) },
                onNavigateToChangePassword = { navController.navigate(Screen.ChangePassword.route) },
                on2FASettings = { navController.navigate(Screen.TwoFactorAuth.route) },
                onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                onNavigateToAppearance = { navController.navigate(Screen.Appearance.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        composable("edit_profile") {
            EditProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Statements.route) {
            StatementsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.LimitsFees.route) {
            LimitsFeesScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ChangePassword.route) {
            ChangePasswordScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.TwoFactorAuth.route) {
            TwoFactorAuthScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Appearance.route) {
            AppearanceScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.SendMoney.route) {
            SendMoneyScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToConfirm = { recipientId, amount ->
                    navController.navigate(Screen.SendMoneyConfirm.createRoute(recipientId, amount))
                }
            )
        }
        
        composable(
            route = Screen.SendMoneyConfirm.route,
            arguments = listOf(
                navArgument("recipientId") { type = NavType.StringType },
                navArgument("amount") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val recipientId = backStackEntry.arguments?.getString("recipientId") ?: ""
            val amount = backStackEntry.arguments?.getString("amount") ?: "0.00"
            
            SendMoneyConfirmScreen(
                recipientId = recipientId,
                amount = amount,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSuccess = { transactionId ->
                    navController.navigate(Screen.SendMoneySuccess.createRoute(transactionId)) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                }
            )
        }
        
        composable(
            route = Screen.SendMoneySuccess.route,
            arguments = listOf(
                navArgument("transactionId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getString("transactionId") ?: ""
            
            SendMoneySuccessScreen(
                transactionId = transactionId,
                onDone = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.AddMoney.route) {
            AddMoneyAmountScreen(
                onNavigateBack = { navController.popBackStack() },
                onContinue = { amount ->
                    navController.navigate(Screen.AddMoneyMethod.createRoute(amount))
                }
            )
        }
        
        composable(
            route = Screen.AddMoneyMethod.route,
            arguments = listOf(navArgument("amount") { type = NavType.StringType })
        ) { backStackEntry ->
            val amount = backStackEntry.arguments?.getString("amount") ?: "0"
            
            AddMoneyMethodScreen(
                amount = amount,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCardEntry = {
                    navController.navigate(Screen.AddMoneyCardEntry.createRoute(amount))
                },
                onNavigateToConfirm = { paymentMethod ->
                    navController.navigate(Screen.AddMoneyConfirm.createRoute(amount, paymentMethod))
                }
            )
        }
        
        composable(
            route = Screen.AddMoneyCardEntry.route,
            arguments = listOf(navArgument("amount") { type = NavType.StringType })
        ) { backStackEntry ->
            val amount = backStackEntry.arguments?.getString("amount") ?: "0"
            
            AddMoneyCardEntryScreen(
                amount = amount,
                onNavigateBack = { navController.popBackStack() },
                onContinue = {
                    navController.navigate(Screen.AddMoneyConfirm.createRoute(amount, "new_card"))
                }
            )
        }
        
        composable(
            route = Screen.AddMoneyConfirm.route,
            arguments = listOf(
                navArgument("amount") { type = NavType.StringType },
                navArgument("paymentMethod") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val amount = backStackEntry.arguments?.getString("amount") ?: "0"
            val paymentMethod = backStackEntry.arguments?.getString("paymentMethod") ?: ""
            
            AddMoneyConfirmScreen(
                amount = amount,
                paymentMethod = paymentMethod,
                onNavigateBack = { navController.popBackStack() },
                onConfirm = { transactionId ->
                    navController.navigate(Screen.AddMoneySuccess.createRoute(transactionId, amount)) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                }
            )
        }
        
        composable(
            route = Screen.AddMoneySuccess.route,
            arguments = listOf(
                navArgument("transactionId") { type = NavType.StringType },
                navArgument("amount") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getString("transactionId") ?: ""
            val amount = backStackEntry.arguments?.getString("amount") ?: "0"
            
            AddMoneySuccessScreen(
                transactionId = transactionId,
                amount = amount,
                onDone = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onAddMore = {
                    navController.navigate(Screen.AddMoney.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                }
            )
        }
        
        composable(Screen.InternalTransfer.route) {
            InternalTransferScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSuccess = { transactionId ->
                    navController.navigate(Screen.InternalTransferSuccess.createRoute(transactionId)) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                }
            )
        }
        
        composable(
            route = Screen.InternalTransferSuccess.route,
            arguments = listOf(
                navArgument("transactionId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getString("transactionId") ?: ""
            
            InternalTransferSuccessScreen(
                transactionId = transactionId,
                onDone = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onTransferAgain = {
                    navController.navigate(Screen.InternalTransfer.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                }
            )
        }
        
        composable(Screen.CreateCard.route) {
            CreateCardScreen(
                onNavigateBack = { navController.popBackStack() },
                onCardCreated = {
                    navController.navigate(Screen.Cards.route) {
                        popUpTo(Screen.Cards.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.CardDetails.route,
            arguments = listOf(navArgument("cardId") { type = NavType.StringType })
        ) { backStackEntry ->
            val cardId = backStackEntry.arguments?.getString("cardId") ?: ""
            CardDetailsScreen(
                cardId = cardId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.BillPayments.route) {
            BillPaymentsScreen(
                onNavigateBack = { navController.popBackStack() },
                onAddBiller = { navController.navigate(Screen.AddBiller.route) },
                onPayBill = { billerId ->
                    navController.navigate(Screen.PayBill.createRoute(billerId))
                }
            )
        }
        
        composable(Screen.AddBiller.route) {
            AddBillerScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.PayBill.route,
            arguments = listOf(navArgument("billerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val billerId = backStackEntry.arguments?.getString("billerId") ?: ""
            PayBillScreen(
                billerId = billerId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.SavingsGoals.route) {
            SavingsGoalsScreen(
                onNavigateBack = { navController.popBackStack() },
                onAutomateSavings = { goalAccountId ->
                    navController.navigate(Screen.AutomateSavings.createRoute(goalAccountId))
                }
            )
        }
        
        composable(
            route = Screen.AutomateSavings.route,
            arguments = listOf(navArgument("goalAccountId") { type = NavType.StringType })
        ) { backStackEntry ->
            val goalAccountId = backStackEntry.arguments?.getString("goalAccountId") ?: ""
            AutomateSavingsScreen(
                goalAccountId = goalAccountId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.BankStatement.route,
            arguments = listOf(navArgument("accountId") { type = NavType.StringType })
        ) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getString("accountId") ?: ""
            BankStatementScreen(
                accountId = accountId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
