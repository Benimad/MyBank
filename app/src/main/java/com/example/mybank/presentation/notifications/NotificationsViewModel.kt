package com.example.mybank.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybank.data.manager.NotificationManager
import com.example.mybank.data.model.BankNotification
import com.example.mybank.data.model.NotificationType
import com.example.mybank.data.preferences.PreferencesManager
import com.example.mybank.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class NotificationFilter {
    ALL, TRANSACTIONAL, ALERTS
}

data class NotificationsUiState(
    val allNotifications: List<BankNotification> = emptyList(),
    val filteredNotifications: List<BankNotification> = emptyList(),
    val selectedFilter: NotificationFilter = NotificationFilter.ALL,
    val unreadCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val preferencesManager: PreferencesManager,
    private val notificationManager: NotificationManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()
    
    private var hasSeeded = false
    
    init {
        loadNotifications()
    }
    
    private fun loadNotifications() {
        viewModelScope.launch {
            preferencesManager.userId
                .filterNotNull()
                .flatMapLatest { userId ->
                    if (!hasSeeded) {
                        val notifications = notificationRepository.getUserNotifications(userId).first()
                        if (notifications.isEmpty()) {
                            seedInitialNotifications(userId)
                            hasSeeded = true
                        }
                    }
                    
                    combine(
                        notificationRepository.getUserNotifications(userId),
                        notificationRepository.getUnreadNotificationCount(userId)
                    ) { notifications, unreadCount ->
                        val filtered = applyFilter(notifications, _uiState.value.selectedFilter)
                        _uiState.value.copy(
                            allNotifications = notifications,
                            filteredNotifications = filtered,
                            unreadCount = unreadCount
                        )
                    }
                }
                .collect { newState ->
                    _uiState.value = newState
                }
        }
    }
    
    private fun seedInitialNotifications(userId: String) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val hour = 3600 * 1000L
            val day = 24 * 3600 * 1000L
            
            // Today: Salary (2h ago), Low Balance (5h ago)
            notificationManager.createSalaryNotification(userId, "$4,250.00", now - 2 * hour)
            notificationManager.createLowBalanceAlert(userId, "$50", now - 5 * hour)
            
            // Yesterday: Netflix (9 AM), Security (8:42 AM)
            notificationManager.createSubscriptionNotification(userId, "Netflix", "$15.99", now - day - 2 * hour)
            notificationManager.createSecurityAlert(userId, "iPhone 14 Pro", "New York, USA", now - day - 2 * hour - 18 * 60 * 1000)
            
            // This Week: Rate Update (Mon), Statement (Sun)
            notificationManager.createRateUpdateNotification(userId, "4.50%", now - 2 * day)
            notificationManager.createStatementNotification(userId, "September 2023", now - 3 * day)
        }
    }
    
    fun updateFilter(filter: NotificationFilter) {
        val currentNotifications = _uiState.value.allNotifications
        val filtered = applyFilter(currentNotifications, filter)
        _uiState.value = _uiState.value.copy(
            selectedFilter = filter,
            filteredNotifications = filtered
        )
    }
    
    private fun applyFilter(notifications: List<BankNotification>, filter: NotificationFilter): List<BankNotification> {
        return when (filter) {
            NotificationFilter.ALL -> notifications
            NotificationFilter.TRANSACTIONAL -> notifications.filter {
                it.type == NotificationType.INCOME ||
                it.type == NotificationType.EXPENSE ||
                it.type == NotificationType.SUBSCRIPTION
            }
            NotificationFilter.ALERTS -> notifications.filter {
                it.type == NotificationType.LOW_BALANCE ||
                it.type == NotificationType.SECURITY ||
                it.type == NotificationType.RATE_UPDATE ||
                it.type == NotificationType.STATEMENT ||
                it.type == NotificationType.INFO
            }
        }
    }
    
    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(notificationId)
        }
    }
    
    fun markAllAsRead() {
        viewModelScope.launch {
            val userId = preferencesManager.userId.first()
            if (userId != null) {
                notificationRepository.markAllAsRead(userId)
            }
        }
    }
    
    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            notificationRepository.syncNotifications()
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
}
