package com.example.mybank.presentation.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mybank.ui.theme.*

@Composable
fun RegisterScreen(
    authState: AuthState,
    onRegister: (String, String, String) -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    val focusManager = LocalFocusManager.current
    val passwordStrength = calculatePasswordStrength(password)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101622))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF101622).copy(alpha = 0.9f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(PrimaryBlue)
                        ) {
                            Text(
                                text = "M",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontFamily = InterFontFamily
                            )
                        }
                        Text(
                            text = "MYBANK",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF94A3B8),
                            letterSpacing = 2.sp,
                            fontFamily = InterFontFamily
                        )
                    }

                    Spacer(modifier = Modifier.size(40.dp))
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Create your account",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = InterFontFamily,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Start your secure banking journey today.",
                        fontSize = 16.sp,
                        color = Color(0xFF94A3B8),
                        fontFamily = InterFontFamily
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Full Name",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFCBD5E1),
                            modifier = Modifier.padding(start = 4.dp),
                            fontFamily = InterFontFamily
                        )
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            placeholder = { 
                                Text(
                                    "John Doe",
                                    color = Color(0xFF475569),
                                    fontFamily = InterFontFamily
                                ) 
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color(0xFF64748B)
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF1A2230),
                                unfocusedContainerColor = Color(0xFF1A2230),
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = Color(0xFF374151),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Email Address",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFCBD5E1),
                            modifier = Modifier.padding(start = 4.dp),
                            fontFamily = InterFontFamily
                        )
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            placeholder = { 
                                Text(
                                    "name@example.com",
                                    color = Color(0xFF475569),
                                    fontFamily = InterFontFamily
                                ) 
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = Color(0xFF64748B)
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF1A2230),
                                unfocusedContainerColor = Color(0xFF1A2230),
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = Color(0xFF374151),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Password",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFCBD5E1),
                            modifier = Modifier.padding(start = 4.dp),
                            fontFamily = InterFontFamily
                        )
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            placeholder = { 
                                Text(
                                    "••••••••",
                                    color = Color(0xFF475569),
                                    fontFamily = InterFontFamily
                                ) 
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFF64748B)
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = Color(0xFF64748B)
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF1A2230),
                                unfocusedContainerColor = Color(0xFF1A2230),
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = Color(0xFF374151),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )
                        )

                        if (password.isNotEmpty()) {
                            PasswordStrengthIndicator(passwordStrength)
                        }

                        Text(
                            text = "Must contain 8+ chars, 1 symbol, 1 uppercase.",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(start = 4.dp),
                            fontFamily = InterFontFamily
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Confirm Password",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFCBD5E1),
                            modifier = Modifier.padding(start = 4.dp),
                            fontFamily = InterFontFamily
                        )
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            placeholder = { 
                                Text(
                                    "••••••••",
                                    color = Color(0xFF475569),
                                    fontFamily = InterFontFamily
                                ) 
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.LockReset,
                                    contentDescription = null,
                                    tint = Color(0xFF64748B)
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Icon(
                                        imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = Color(0xFF64748B)
                                    )
                                }
                            },
                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF1A2230),
                                unfocusedContainerColor = Color(0xFF1A2230),
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = Color(0xFF374151),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    if (fullName.isNotBlank() && email.isNotBlank() && 
                                        password.isNotBlank() && password == confirmPassword) {
                                        onRegister(email, password, fullName)
                                    }
                                }
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val buttonInteractionSource = remember { MutableInteractionSource() }
                    val isPressed by buttonInteractionSource.collectIsPressedAsState()
                    val scale by animateFloatAsState(
                        targetValue = if (isPressed) 0.98f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "button_scale"
                    )

                    Button(
                        onClick = {
                            if (fullName.isNotBlank() && email.isNotBlank() && 
                                password.isNotBlank() && password == confirmPassword) {
                                onRegister(email, password, fullName)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .scale(scale),
                        enabled = !authState.isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlue,
                            contentColor = Color.White
                        ),
                        interactionSource = buttonInteractionSource
                    ) {
                        if (authState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Create Account",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = InterFontFamily
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = Color(0xFF94A3B8))) {
                                append("By registering, you agree to MyBank's ")
                            }
                            withStyle(SpanStyle(color = PrimaryBlue)) {
                                append("Terms of Service")
                            }
                            withStyle(SpanStyle(color = Color(0xFF94A3B8))) {
                                append(" and ")
                            }
                            withStyle(SpanStyle(color = PrimaryBlue)) {
                                append("Privacy Policy")
                            }
                            withStyle(SpanStyle(color = Color(0xFF94A3B8))) {
                                append(".")
                            }
                        },
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        fontFamily = InterFontFamily,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            color = Color(0xFF101622),
            tonalElevation = 4.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Already have an account?",
                        fontSize = 14.sp,
                        color = Color(0xFF94A3B8),
                        fontFamily = InterFontFamily
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Log in",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryBlue,
                        modifier = Modifier.clickable { onNavigateToLogin() },
                        fontFamily = InterFontFamily
                    )
                }
            }
        }

        if (authState.error != null) {
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                containerColor = AlertRed
            ) {
                Text(authState.error, color = Color.White)
            }
        }
    }
}

@Composable
fun PasswordStrengthIndicator(strength: PasswordStrength) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(4) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        when {
                            index < strength.level -> strength.color
                            else -> Color(0xFF374151)
                        }
                    )
            )
        }
        Text(
            text = strength.label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = strength.color,
            fontFamily = InterFontFamily
        )
    }
}

data class PasswordStrength(
    val level: Int,
    val label: String,
    val color: Color
)

fun calculatePasswordStrength(password: String): PasswordStrength {
    var strength = 0
    
    if (password.length >= 8) strength++
    if (password.any { it.isUpperCase() }) strength++
    if (password.any { it.isDigit() }) strength++
    if (password.any { !it.isLetterOrDigit() }) strength++
    
    return when (strength) {
        0, 1 -> PasswordStrength(1, "Weak", Color(0xFFEF4444))
        2 -> PasswordStrength(2, "Medium", Color(0xFF10B981))
        3 -> PasswordStrength(3, "Good", Color(0xFF10B981))
        else -> PasswordStrength(4, "Strong", Color(0xFF10B981))
    }
}
