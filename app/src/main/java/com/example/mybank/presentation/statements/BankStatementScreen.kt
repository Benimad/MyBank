package com.example.mybank.presentation.statements

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybank.data.model.Account
import com.example.mybank.data.model.Transaction
import com.example.mybank.data.repository.AccountRepository
import com.example.mybank.data.repository.TransactionRepository
import com.example.mybank.ui.theme.InterFontFamily
import com.example.mybank.ui.theme.PrimaryBlue
import com.example.mybank.util.BankStatementGenerator
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class BankStatementUiState(
    val account: Account? = null,
    val transactions: List<Transaction> = emptyList(),
    val startDate: Long = Calendar.getInstance().apply {
        add(Calendar.MONTH, -1)
    }.timeInMillis,
    val endDate: Long = System.currentTimeMillis(),
    val isGenerating: Boolean = false,
    val error: String? = null,
    val generatedFilePath: String? = null
)

@HiltViewModel
class BankStatementViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(BankStatementUiState())
    val uiState: StateFlow<BankStatementUiState> = _uiState.asStateFlow()

    fun loadAccount(accountId: String) {
        viewModelScope.launch {
            try {
                accountRepository.getAccount(accountId).collect { account ->
                    if (account != null) {
                        _uiState.value = _uiState.value.copy(account = account)
                        loadTransactions(accountId)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load account"
                )
            }
        }
    }

    private fun loadTransactions(accountId: String) {
        viewModelScope.launch {
            try {
                transactionRepository.getAccountTransactions(accountId).collect { transactions ->
                    _uiState.value = _uiState.value.copy(transactions = transactions)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load transactions"
                )
            }
        }
    }

    fun setStartDate(date: Long) {
        _uiState.value = _uiState.value.copy(startDate = date)
    }

    fun setEndDate(date: Long) {
        _uiState.value = _uiState.value.copy(endDate = date)
    }

    fun generateStatement(context: android.content.Context) {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.account == null) {
                _uiState.value = state.copy(error = "Account not found")
                return@launch
            }

            _uiState.value = state.copy(isGenerating = true, error = null)

            try {
                val generator = BankStatementGenerator(context)
                val filteredTransactions = state.transactions.filter {
                    it.timestamp in state.startDate..state.endDate
                }

                val result = generator.generateStatement(
                    account = state.account,
                    transactions = filteredTransactions,
                    startDate = state.startDate,
                    endDate = state.endDate
                )

                result.onSuccess { file ->
                    _uiState.value = state.copy(
                        isGenerating = false,
                        generatedFilePath = file.absolutePath
                    )
                }.onFailure { e ->
                    _uiState.value = state.copy(
                        isGenerating = false,
                        error = e.message ?: "Failed to generate statement"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    isGenerating = false,
                    error = e.message ?: "Failed to generate statement"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearGeneratedFile() {
        _uiState.value = _uiState.value.copy(generatedFilePath = null)
    }
}

@Composable
fun BankStatementScreen(
    accountId: String,
    onNavigateBack: () -> Unit = {},
    viewModel: BankStatementViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(accountId) {
        viewModel.loadAccount(accountId)
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.generatedFilePath) {
        uiState.generatedFilePath?.let {
            Toast.makeText(context, "Statement saved to: $it", Toast.LENGTH_LONG).show()
            viewModel.clearGeneratedFile()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Bank Statement",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = InterFontFamily
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                if (uiState.account != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1E293B)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(
                                            PrimaryBlue.copy(alpha = 0.2f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = uiState.account!!.accountName.take(1).uppercase(),
                                        color = PrimaryBlue,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = InterFontFamily
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = uiState.account!!.accountName,
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = InterFontFamily
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = uiState.account!!.accountNumber,
                                        color = Color(0xFF64748B),
                                        fontSize = 14.sp,
                                        fontFamily = InterFontFamily
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Divider(color = Color(0xFF334155))
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Current Balance",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 14.sp,
                                        fontFamily = InterFontFamily
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "$${String.format("%.2f", uiState.account!!.balance)}",
                                        color = Color.White,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = InterFontFamily
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Account Type",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 14.sp,
                                        fontFamily = InterFontFamily
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = uiState.account!!.accountType.name,
                                        color = PrimaryBlue,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = InterFontFamily
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                Text(
                    text = "Statement Period",
                    color = Color(0xFF94A3B8),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = InterFontFamily
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "From",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            fontFamily = InterFontFamily
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1E293B))
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFF334155),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { showStartDatePicker = true }
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = dateFormat.format(uiState.startDate),
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontFamily = InterFontFamily
                                )
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Calendar",
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "To",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            fontFamily = InterFontFamily
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1E293B))
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFF334155),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { showEndDatePicker = true }
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = dateFormat.format(uiState.endDate),
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontFamily = InterFontFamily
                                )
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Calendar",
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1E293B)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Statement Summary",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = InterFontFamily
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        val filteredTransactions = uiState.transactions.filter {
                            it.timestamp in uiState.startDate..uiState.endDate
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total Transactions:",
                                color = Color(0xFF94A3B8),
                                fontSize = 14.sp,
                                fontFamily = InterFontFamily
                            )
                            Text(
                                text = "${filteredTransactions.size}",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = InterFontFamily
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        viewModel.generateStatement(context)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        disabledContainerColor = PrimaryBlue.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !uiState.isGenerating && uiState.account != null
                ) {
                    if (uiState.isGenerating) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Generate PDF Statement",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = InterFontFamily
                            )
                        }
                    }
                }
            }
        }
    }
}
