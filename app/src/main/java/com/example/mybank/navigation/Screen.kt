package com.example.mybank.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object Home : Screen("home")
    object Accounts : Screen("accounts")
    object AccountDetails : Screen("account_details/{accountId}") {
        fun createRoute(accountId: String) = "account_details/$accountId"
    }
    object Transactions : Screen("transactions/{accountId}") {
        fun createRoute(accountId: String) = "transactions/$accountId"
    }
    object TransactionDetails : Screen("transaction_details/{transactionId}") {
        fun createRoute(transactionId: String) = "transaction_details/$transactionId"
    }
    object Notifications : Screen("notifications")
    object Settings : Screen("settings")
    object Profile : Screen("profile")
    object Cards : Screen("cards")
    object Transfers : Screen("transfers")
    object AllTransactions : Screen("all_transactions")
    object Analytics : Screen("analytics")
    object SendMoney : Screen("send_money")
    object SendMoneyConfirm : Screen("send_money_confirm/{recipientId}/{amount}") {
        fun createRoute(recipientId: String, amount: String) = "send_money_confirm/$recipientId/$amount"
    }
    object SendMoneySuccess : Screen("send_money_success/{transactionId}") {
        fun createRoute(transactionId: String) = "send_money_success/$transactionId"
    }
    object AddMoney : Screen("add_money")
    object AddMoneyMethod : Screen("add_money_method/{amount}") {
        fun createRoute(amount: String) = "add_money_method/$amount"
    }
    object AddMoneyCardEntry : Screen("add_money_card_entry/{amount}") {
        fun createRoute(amount: String) = "add_money_card_entry/$amount"
    }
    object AddMoneyConfirm : Screen("add_money_confirm/{amount}/{paymentMethod}") {
        fun createRoute(amount: String, paymentMethod: String) = "add_money_confirm/$amount/$paymentMethod"
    }
    object AddMoneySuccess : Screen("add_money_success/{transactionId}/{amount}") {
        fun createRoute(transactionId: String, amount: String) = "add_money_success/$transactionId/$amount"
    }
    object InternalTransfer : Screen("internal_transfer")
    object InternalTransferSuccess : Screen("internal_transfer_success/{transactionId}") {
        fun createRoute(transactionId: String) = "internal_transfer_success/$transactionId"
    }
    object CreatePot : Screen("create_pot")
    
    object CardDetails : Screen("card_details/{cardId}") {
        fun createRoute(cardId: String) = "card_details/$cardId"
    }
    object CreateCard : Screen("create_card")
    
    object BillPayments : Screen("bill_payments")
    object AddBiller : Screen("add_biller")
    object PayBill : Screen("pay_bill/{billerId}") {
        fun createRoute(billerId: String) = "pay_bill/$billerId"
    }
    
    object SavingsGoals : Screen("savings_goals")
    object AutomateSavings : Screen("automate_savings/{goalAccountId}") {
        fun createRoute(goalAccountId: String) = "automate_savings/$goalAccountId"
    }
    
    object BankStatement : Screen("bank_statement/{accountId}") {
        fun createRoute(accountId: String) = "bank_statement/$accountId"
    }

    object Statements : Screen("statements")
    object LimitsFees : Screen("limits_fees")
    object ChangePassword : Screen("change_password")
    object TwoFactorAuth : Screen("two_factor_auth")
    object Appearance : Screen("appearance")
}
