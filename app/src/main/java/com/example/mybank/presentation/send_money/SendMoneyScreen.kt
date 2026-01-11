package com.example.mybank.presentation.send_money

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mybank.data.model.RecipientContact
import com.example.mybank.ui.theme.*

@Composable
fun SendMoneyScreen(
    viewModel: SendMoneyViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToConfirm: (recipientId: String, amount: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var amount by remember { mutableStateOf("0.00") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedRecipient by remember { mutableStateOf<RecipientContact?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadRecentRecipients()
        viewModel.loadAllContacts()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(onNavigateBack = onNavigateBack)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                item {
                    AmountInputSection(
                        amount = amount,
                        onAmountChange = { newAmount ->
                            amount = formatAmountInput(newAmount)
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    SearchBar(
                        query = searchQuery,
                        onQueryChange = {
                            searchQuery = it
                            viewModel.searchContacts(it)
                        }
                    )
                }

                if (uiState.recentRecipients.isNotEmpty() && searchQuery.isEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        RecentRecipientsSection(
                            recipients = uiState.recentRecipients,
                            onRecipientClick = { recipient ->
                                selectedRecipient = recipient
                            }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Contacts",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            fontSize = 18.sp
                        )
                    }
                }

                items(
                    if (searchQuery.isEmpty()) uiState.allContacts
                    else uiState.searchResults
                ) { contact ->
                    ContactListItem(
                        contact = contact,
                        isSelected = selectedRecipient?.id == contact.id,
                        onClick = {
                            selectedRecipient = contact
                        }
                    )
                }
            }
        }

        ContinueButton(
            amount = amount,
            selectedRecipient = selectedRecipient,
            isEnabled = (amount.toDoubleOrNull() ?: 0.0) > 0 && selectedRecipient != null,
            onClick = {
                selectedRecipient?.let { recipient ->
                    onNavigateToConfirm(recipient.id, amount)
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun TopBar(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundDark.copy(alpha = 0.9f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = TextWhite,
                modifier = Modifier.size(24.dp)
            )
        }

        Text(
            text = "Send Money",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextWhite,
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.size(40.dp))
    }
}

@Composable
private fun AmountInputSection(
    amount: String,
    onAmountChange: (String) -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (amount != "0.00") 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "amount_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Enter Amount",
            style = MaterialTheme.typography.bodyMedium,
            color = TextGray500,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.scale(scale)
        ) {
            Text(
                text = "$",
                style = MaterialTheme.typography.displayLarge,
                color = TextWhite,
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )
            
            BasicTextField(
                value = amount,
                onValueChange = onAmountChange,
                textStyle = TextStyle(
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    letterSpacing = (-0.5).sp,
                    textAlign = TextAlign.Start
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                cursorBrush = SolidColor(PrimaryBlue),
                modifier = Modifier.widthIn(max = 300.dp)
            )
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceDark.copy(alpha = 0.5f))
            .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = TextGray400,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            textStyle = TextStyle(
                fontSize = 16.sp,
                color = TextWhite,
                fontWeight = FontWeight.Normal
            ),
            cursorBrush = SolidColor(PrimaryBlue),
            decorationBox = { innerTextField ->
                if (query.isEmpty()) {
                    Text(
                        text = "Name, @username, or email",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextGray500,
                        fontSize = 16.sp
                    )
                }
                innerTextField()
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun RecentRecipientsSection(
    recipients: List<RecipientContact>,
    onRecipientClick: (RecipientContact) -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                fontSize = 18.sp
            )

            TextButton(onClick = { }) {
                Text(
                    text = "See All",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryBlue,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            recipients.forEach { recipient ->
                RecentRecipientItem(
                    recipient = recipient,
                    onClick = { onRecipientClick(recipient) }
                )
            }
        }
    }
}

@Composable
private fun RecentRecipientItem(
    recipient: RecipientContact,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .width(64.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(PrimaryBlue.copy(alpha = 0.2f))
                .border(
                    width = 2.dp,
                    color = if (recipient.lastTransferredAt != null) PrimaryBlue else Color.Transparent,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = recipient.name.take(2).uppercase(),
                color = TextWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Text(
            text = recipient.name.split(" ").firstOrNull() ?: recipient.name,
            style = MaterialTheme.typography.bodySmall,
            color = TextWhite,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ContactListItem(
    contact: RecipientContact,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(if (isSelected) PrimaryBlue.copy(alpha = 0.1f) else Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (isSelected) PrimaryBlue else SurfaceDark)
                .border(
                    width = if (isSelected) 2.dp else 0.dp,
                    color = if (isSelected) PrimaryBlue else Color.Transparent,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = contact.name.take(2).uppercase(),
                color = TextWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = contact.name,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) PrimaryBlue else TextWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = contact.username?.let { "@$it" } 
                    ?: contact.email 
                    ?: contact.phone 
                    ?: "MyBank User",
                style = MaterialTheme.typography.bodySmall,
                color = TextGray500,
                fontSize = 14.sp
            )
        }

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Selected",
                tint = PrimaryBlue,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Select",
                tint = TextGray700,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun ContinueButton(
    amount: String,
    selectedRecipient: RecipientContact?,
    isEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        BackgroundDark.copy(alpha = 0.95f),
                        BackgroundDark
                    )
                )
            )
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (selectedRecipient != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(PrimaryBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = selectedRecipient.name.take(2).uppercase(),
                                color = TextWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Column {
                            Text(
                                text = "Sending to",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextGray500,
                                fontSize = 12.sp
                            )
                            Text(
                                text = selectedRecipient.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Text(
                        text = "$$amount",
                        style = MaterialTheme.typography.titleMedium,
                        color = PrimaryBlue,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    disabledContainerColor = SurfaceDark
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = isEnabled
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Continue",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isEnabled) Color.White else TextGray600,
                        fontSize = 16.sp
                    )

                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = if (isEnabled) Color.White else TextGray600,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

private fun formatAmountInput(input: String): String {
    val digitsOnly = input.filter { it.isDigit() }
    
    if (digitsOnly.isEmpty()) return "0.00"
    
    val number = digitsOnly.toLongOrNull() ?: return "0.00"
    
    val dollars = number / 100
    val cents = number % 100
    
    return String.format("%d.%02d", dollars, cents)
}
