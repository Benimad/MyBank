package com.example.mybank.presentation.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mybank.presentation.profile.components.ProfileAvatar
import com.example.mybank.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    
    var displayName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var hasChanges by remember { mutableStateOf(false) }
    
    LaunchedEffect(uiState.userProfile) {
        uiState.userProfile?.let { profile ->
            displayName = profile.displayName
            phone = profile.phone ?: ""
            address = profile.address ?: ""
        }
    }
    
    LaunchedEffect(displayName, phone, address) {
        hasChanges = uiState.userProfile?.let { profile ->
            displayName != profile.displayName ||
            phone != (profile.phone ?: "") ||
            address != (profile.address ?: "")
        } ?: false
    }
    
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.uploadProfilePhoto(it) }
    }
    
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            viewModel.clearSuccessMessage()
            if (it == "Profile updated successfully") {
                onNavigateBack()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundDark.copy(alpha = 0.9f))
                    .padding(horizontal = 4.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    
                    Text(
                        text = "Edit Profile",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color.White,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    
                    TextButton(
                        onClick = {
                            if (hasChanges) {
                                viewModel.updateProfile(
                                    displayName = displayName,
                                    phone = phone.ifBlank { null },
                                    address = address.ifBlank { null }
                                )
                            }
                        },
                        enabled = hasChanges && !uiState.isLoading
                    ) {
                        Text(
                            text = "Save",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = if (hasChanges && !uiState.isLoading) PrimaryBlue else TextGray600
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box {
                        ProfileAvatar(
                            photoUrl = uiState.userProfile?.photoUrl,
                            size = 100,
                            showVerifiedBadge = false,
                            onClick = { imagePicker.launch("image/*") }
                        )
                        
                        if (uiState.isUploadingPhoto) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = PrimaryBlue,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    TextButton(
                        onClick = { imagePicker.launch("image/*") },
                        enabled = !uiState.isUploadingPhoto
                    ) {
                        Text(
                            text = "Change Photo",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = PrimaryBlue
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = "Personal Information",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                CustomTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = "Full Name",
                    leadingIcon = Icons.Outlined.Person,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                CustomTextField(
                    value = uiState.userProfile?.email ?: "",
                    onValueChange = {},
                    label = "Email",
                    leadingIcon = Icons.Outlined.Email,
                    enabled = false
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                CustomTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "Phone Number",
                    leadingIcon = Icons.Outlined.Phone,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                CustomTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = "Address",
                    leadingIcon = Icons.Outlined.LocationOn,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
                    minLines = 2
                )
                
                if (uiState.error != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = uiState.error ?: "",
                        fontFamily = InterFontFamily,
                        fontSize = 12.sp,
                        color = AlertRed,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                fontFamily = InterFontFamily,
                fontSize = 14.sp
            )
        },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = label,
                tint = if (enabled) TextGray400 else TextGray600
            )
        },
        enabled = enabled,
        singleLine = minLines == 1,
        minLines = minLines,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            disabledTextColor = TextGray500,
            focusedIndicatorColor = PrimaryBlue,
            unfocusedIndicatorColor = BorderDark,
            disabledIndicatorColor = BorderDarkSubtle,
            focusedLabelColor = PrimaryBlue,
            unfocusedLabelColor = TextGray400,
            disabledLabelColor = TextGray600,
            cursorColor = PrimaryBlue,
            focusedContainerColor = SurfaceDark.copy(alpha = 0.3f),
            unfocusedContainerColor = SurfaceDark.copy(alpha = 0.3f),
            disabledContainerColor = SurfaceDark.copy(alpha = 0.3f)
        ),
        modifier = Modifier.fillMaxWidth()
    )
}
