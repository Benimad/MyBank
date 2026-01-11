package com.example.mybank.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.mybank.data.model.AccountType
import com.example.mybank.data.model.Transaction
import com.example.mybank.data.model.TransactionCategory
import com.example.mybank.data.model.TransactionType
import com.example.mybank.data.repository.AccountRepository
import com.example.mybank.data.repository.TransactionRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.*

@HiltWorker
class SavingsGoalsAutomationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val firestoreService: com.example.mybank.data.firebase.FirestoreService
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val userId = inputData.getString(KEY_USER_ID) ?: return Result.failure()
            val automationAmount = inputData.getDouble(KEY_AUTOMATION_AMOUNT, 0.0)
            val sourceAccountId = inputData.getString(KEY_SOURCE_ACCOUNT_ID) ?: return Result.failure()
            val goalAccountId = inputData.getString(KEY_GOAL_ACCOUNT_ID) ?: return Result.failure()

            if (automationAmount <= 0) {
                return Result.failure()
            }

            val sourceAccount = accountRepository.getAccount(sourceAccountId).first()
            val goalAccount = accountRepository.getAccount(goalAccountId).first()

            if (sourceAccount == null || goalAccount == null) {
                return Result.failure()
            }

            if (goalAccount.accountType != AccountType.GOAL && goalAccount.accountType != AccountType.SAVINGS) {
                return Result.failure()
            }

            if (sourceAccount.balance < automationAmount) {
                return Result.retry()
            }

            val result = firestoreService.processTransfer(
                fromAccountId = sourceAccountId,
                toAccountId = goalAccountId,
                amount = automationAmount,
                currency = sourceAccount.currency,
                description = "Automated Savings: ${goalAccount.accountName}"
            )

            return result.fold(
                onSuccess = { Result.success() },
                onFailure = { Result.failure() }
            )
        } catch (e: Exception) {
            return Result.failure()
        }
    }

    companion object {
        const val KEY_USER_ID = "user_id"
        const val KEY_AUTOMATION_AMOUNT = "automation_amount"
        const val KEY_SOURCE_ACCOUNT_ID = "source_account_id"
        const val KEY_GOAL_ACCOUNT_ID = "goal_account_id"
        const val WORK_NAME = "savings_goals_automation"
    }
}
