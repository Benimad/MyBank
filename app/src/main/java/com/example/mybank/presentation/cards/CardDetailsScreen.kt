package com.example.mybank.presentation.cards

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailsScreen(
    cardId: String,
    onNavigateBack: () -> Unit,
    onBlockCard: () -> Unit = {},
    onUnblockCard: () -> Unit = {},
    viewModel: CardDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(cardId) {
        viewModel.loadCard(cardId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Card Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Show card settings */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                uiState.card?.let { card ->
                    CardSection(card = card)

                    Spacer(modifier = Modifier.height(16.dp))

                    CardSettingsSection(
                        card = card,
                        onToggleFreeze = {
                            if (card.status.name == "FROZEN") {
                                viewModel.unfreezeCard(cardId)
                            } else {
                                viewModel.freezeCard(cardId)
                            }
                        },
                        onSetLimit = { /* Open limit dialog */ }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    CardActionsSection(
                        card = card,
                        onBlockCard = {
                            viewModel.blockCard(cardId)
                            onBlockCard()
                        },
                        onUnblockCard = {
                            // Unblock action
                            onUnblockCard()
                        }
                    )
                } ?: run {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Card not found", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}

@Composable
fun CardSection(card: com.example.mybank.data.model.Card) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "MyBank ${card.cardType.name}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Column {
                    Text(
                        text = "•••• ${card.cardNumber.takeLast(4)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "Card Holder",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = card.cardHolderName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Column {
                            Text(
                                text = "Expires",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${String.format("%02d", card.expiryMonth)}/${card.expiryYear % 100}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        CardInfoItem("Status", card.status.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() })
        CardInfoItem("Network", card.cardNetwork.name)
    }
}

@Composable
fun CardInfoItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun CardSettingsSection(
    card: com.example.mybank.data.model.Card,
    onToggleFreeze: () -> Unit,
    onSetLimit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Card Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            SettingItem(
                title = "Freeze Card",
                description = if (card.status.name == "FROZEN") "Unfreeze your card" else "Temporarily freeze your card",
                value = card.status.name == "FROZEN",
                onToggle = onToggleFreeze
            )

            Divider(modifier = Modifier.padding(vertical = 4.dp))

            SettingItem(
                title = "Set Spending Limit",
                description = "Set a monthly spending limit",
                enabled = true,
                onToggle = onSetLimit
            )
        }
    }
}

@Composable
fun SettingItem(
    title: String,
    description: String,
    value: Boolean? = null,
    enabled: Boolean = value != null,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (value != null) {
            Switch(checked = value, onCheckedChange = { onToggle() })
        } else {
            TextButton(onClick = onToggle) {
                Text("Manage")
            }
        }
    }
}

@Composable
fun CardActionsSection(
    card: com.example.mybank.data.model.Card,
    onBlockCard: () -> Unit,
    onUnblockCard: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Danger Zone",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )

            val isActive = card.status.name == "ACTIVE"

            Text(
                text = if (isActive) {
                    "If you block your card, all transactions will be declined"
                } else {
                    "Your card is currently blocked"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
            )

            Button(
                onClick = if (isActive) onBlockCard else onUnblockCard,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isActive) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    if (isActive) "Block Card" else "Unblock Card",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}