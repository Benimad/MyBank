package com.example.mybank.presentation.create_pot

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mybank.data.model.AccountType
import com.example.mybank.data.model.PotColorTag
import com.example.mybank.ui.theme.*

data class PotTypeOption(
    val type: AccountType,
    val icon: ImageVector,
    val title: String,
    val description: String
)

@Composable
fun CreatePotScreen(
    viewModel: CreatePotViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onPotCreated: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var potName by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(PotColorTag.BLUE) }
    var targetAmount by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("") }

    val potTypes = remember {
        listOf(
            PotTypeOption(
                type = AccountType.SAVINGS,
                icon = Icons.Default.AccountBalanceWallet,
                title = "Savings",
                description = "General savings"
            ),
            PotTypeOption(
                type = AccountType.GOAL,
                icon = Icons.Default.TrackChanges,
                title = "Goal",
                description = "Specific target"
            ),
            PotTypeOption(
                type = AccountType.INVESTMENT,
                icon = Icons.Default.TrendingUp,
                title = "Investment",
                description = "Grow your money"
            ),
            PotTypeOption(
                type = AccountType.SPENDING,
                icon = Icons.Default.Payment,
                title = "Spending",
                description = "Daily expenses"
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp)
        ) {
            item {
                TopBar(
                    onCloseClick = onNavigateBack
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                PotTypeSelection(
                    potTypes = potTypes,
                    selectedType = uiState.selectedType,
                    onTypeSelected = { viewModel.selectType(it) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                PotDetailsSection(
                    potName = potName,
                    onPotNameChange = { potName = it },
                    selectedColor = selectedColor,
                    onColorSelected = { selectedColor = it }
                )
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                GoalsSection(
                    targetAmount = targetAmount,
                    onTargetAmountChange = { targetAmount = it },
                    deadline = deadline,
                    onDeadlineChange = { deadline = it },
                    onClear = {
                        targetAmount = ""
                        deadline = ""
                    }
                )
            }
        }

        BottomCreateButton(
            isEnabled = potName.isNotEmpty() && !uiState.isLoading,
            onCreateClick = {
                viewModel.createPot(
                    name = potName,
                    colorTag = selectedColor,
                    targetAmount = targetAmount.toDoubleOrNull(),
                    deadline = null
                ) {
                    onPotCreated()
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun TopBar(onCloseClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundDark.copy(alpha = 0.8f))
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onCloseClick) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = TextWhite
            )
        }

        Text(
            text = "Create New Pot",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = TextWhite,
            fontSize = 18.sp
        )

        IconButton(onClick = { }) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Info",
                tint = PrimaryBlue
            )
        }
    }
}

@Composable
private fun PotTypeSelection(
    potTypes: List<PotTypeOption>,
    selectedType: AccountType,
    onTypeSelected: (AccountType) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Choose Pot Type",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                fontSize = 16.sp
            )
            Text(
                text = "Step 1 of 3",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = TextGray500,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PotTypeCard(
                    option = potTypes[0],
                    isSelected = selectedType == potTypes[0].type,
                    onClick = { onTypeSelected(potTypes[0].type) },
                    modifier = Modifier.weight(1f)
                )
                PotTypeCard(
                    option = potTypes[1],
                    isSelected = selectedType == potTypes[1].type,
                    onClick = { onTypeSelected(potTypes[1].type) },
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PotTypeCard(
                    option = potTypes[2],
                    isSelected = selectedType == potTypes[2].type,
                    onClick = { onTypeSelected(potTypes[2].type) },
                    modifier = Modifier.weight(1f)
                )
                PotTypeCard(
                    option = potTypes[3],
                    isSelected = selectedType == potTypes[3].type,
                    onClick = { onTypeSelected(potTypes[3].type) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PotTypeCard(
    option: PotTypeOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    Column(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) PrimaryBlue.copy(alpha = 0.1f) else SurfaceDark)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) PrimaryBlue else BorderDark,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = option.icon,
            contentDescription = option.title,
            tint = if (isSelected) PrimaryBlue else TextGray400,
            modifier = Modifier.size(32.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = option.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) TextWhite else TextWhite,
                fontSize = 14.sp
            )
            Text(
                text = option.description,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Normal,
                color = if (isSelected) PrimaryBlue.copy(alpha = 0.8f) else TextGray500,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun PotDetailsSection(
    potName: String,
    onPotNameChange: (String) -> Unit,
    selectedColor: PotColorTag,
    onColorSelected: (PotColorTag) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Pot Details",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextWhite,
            fontSize = 16.sp
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "POT NAME",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = TextGray500,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )
            OutlinedTextField(
                value = potName,
                onValueChange = onPotNameChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                placeholder = {
                    Text(
                        text = "e.g. Summer Vacation",
                        color = TextGray600
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = BorderDark,
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark,
                    cursorColor = PrimaryBlue
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "POT COLOR TAG",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = TextGray500,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ColorOption(
                    color = Color(0xFF3B82F6),
                    colorTag = PotColorTag.BLUE,
                    isSelected = selectedColor == PotColorTag.BLUE,
                    onClick = { onColorSelected(PotColorTag.BLUE) }
                )
                ColorOption(
                    color = Color(0xFF10B981),
                    colorTag = PotColorTag.EMERALD,
                    isSelected = selectedColor == PotColorTag.EMERALD,
                    onClick = { onColorSelected(PotColorTag.EMERALD) }
                )
                ColorOption(
                    color = Color(0xFFA855F7),
                    colorTag = PotColorTag.PURPLE,
                    isSelected = selectedColor == PotColorTag.PURPLE,
                    onClick = { onColorSelected(PotColorTag.PURPLE) }
                )
                ColorOption(
                    color = Color(0xFF14B8A6),
                    colorTag = PotColorTag.TEAL,
                    isSelected = selectedColor == PotColorTag.TEAL,
                    onClick = { onColorSelected(PotColorTag.TEAL) }
                )
                ColorOption(
                    color = Color(0xFFFBBF24),
                    colorTag = PotColorTag.AMBER,
                    isSelected = selectedColor == PotColorTag.AMBER,
                    onClick = { onColorSelected(PotColorTag.AMBER) }
                )
            }
        }
    }
}

@Composable
private fun ColorOption(
    color: Color,
    colorTag: PotColorTag,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .size(40.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(color)
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 4.dp,
                        color = color.copy(alpha = 0.2f),
                        shape = CircleShape
                    )
                } else {
                    Modifier
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    )
}

@Composable
private fun GoalsSection(
    targetAmount: String,
    onTargetAmountChange: (String) -> Unit,
    deadline: String,
    onDeadlineChange: (String) -> Unit,
    onClear: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Goals (Optional)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                fontSize = 16.sp
            )
            TextButton(onClick = onClear) {
                Text(
                    text = "CLEAR",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "TARGET AMOUNT",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TextGray500,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
                OutlinedTextField(
                    value = targetAmount,
                    onValueChange = { value ->
                        if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                            onTargetAmountChange(value)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    leadingIcon = {
                        Text(
                            text = "$",
                            color = TextGray400,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    placeholder = {
                        Text(
                            text = "0.00",
                            color = TextGray600
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = BorderDark,
                        focusedContainerColor = SurfaceDark,
                        unfocusedContainerColor = SurfaceDark,
                        cursorColor = PrimaryBlue
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "DEADLINE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TextGray500,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
                OutlinedTextField(
                    value = deadline,
                    onValueChange = onDeadlineChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Calendar",
                            tint = TextGray400,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    placeholder = {
                        Text(
                            text = "Select date",
                            color = TextGray600
                        )
                    },
                    readOnly = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = BorderDark,
                        focusedContainerColor = SurfaceDark,
                        unfocusedContainerColor = SurfaceDark,
                        cursorColor = PrimaryBlue
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        }
    }
}

@Composable
private fun BottomCreateButton(
    isEnabled: Boolean,
    onCreateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(BackgroundDark.copy(alpha = 0.9f))
            .border(
                width = 1.dp,
                color = BorderDarkSubtle,
                shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onCreateClick,
            enabled = isEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryBlue,
                contentColor = Color.White,
                disabledContainerColor = SurfaceDark,
                disabledContentColor = TextGray500
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Create Pot",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "SECURE TRANSACTION • NO MONTHLY FEES",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = TextGray500,
            fontSize = 10.sp,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}
