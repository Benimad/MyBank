package com.example.mybank.presentation.auth

import android.app.Activity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.mybank.LocalGoogleSignInClient
import com.example.mybank.LocalGoogleSignInLauncher
import com.example.mybank.ui.theme.*

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun LoginScreen(
    authState: AuthState,
    onLogin: (String, String) -> Unit,
    onGoogleSignIn: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onForgotPassword: () -> Unit,
    onBiometricAuth: () -> Unit  // New callback for biometric authentication
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showBiometricDialog by remember { mutableStateOf(false) }
    var biometricMessage by remember { mutableStateOf<String?>(null) }
    
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val googleSignInClient = LocalGoogleSignInClient.current
    val googleSignInLauncher = LocalGoogleSignInLauncher.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101622))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp, bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(PrimaryBlue)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Welcome back",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontFamily = InterFontFamily,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "Securely log in to your MyBank account",
                            fontSize = 14.sp,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center,
                            fontFamily = InterFontFamily
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
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
                                "Enter your password",
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
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                if (email.isNotBlank() && password.isNotBlank()) {
                                    onLogin(email, password)
                                }
                            }
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Forgot password?",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = PrimaryBlue,
                        modifier = Modifier.clickable { onForgotPassword() },
                        fontFamily = InterFontFamily
                    )
                }

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
                        if (email.isNotBlank() && password.isNotBlank()) {
                            onLogin(email, password)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
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
                            Icon(
                                imageVector = Icons.Default.Login,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Log In",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = InterFontFamily
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF374151)
                )
                Text(
                    text = "Or continue with",
                    fontSize = 14.sp,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontFamily = InterFontFamily
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF374151)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        googleSignInClient?.signInIntent?.let { intent ->
                            googleSignInLauncher?.launch(intent)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFF1A2230),
                        contentColor = Color.White
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF374151))
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GlideImage(
                            model = "https://www.google.com/images/branding/googleg/1x/googleg_standard_color_128dp.png",
                            contentDescription = "Google",
                            modifier = Modifier.size(20.dp),
                            contentScale = ContentScale.Fit
                        )
                        Text(
                            text = "Google",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            fontFamily = InterFontFamily
                        )
                    }
                }

                OutlinedButton(
                    onClick = {
                        showBiometricPrompt(
                            activity = context as FragmentActivity,
                            onCredentialFound = {
                                // Biometric succeeded, trigger re-authentication
                                onBiometricAuth()
                            },
                            onNoCredentials = { message ->
                                biometricMessage = message
                                showBiometricDialog = true
                            },
                            onBiometricError = { error ->
                                biometricMessage = error
                                showBiometricDialog = true
                            }
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFF1A2230),
                        contentColor = Color.White
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF374151))
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Face,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Face ID",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            fontFamily = InterFontFamily
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Don't have an account?",
                    fontSize = 14.sp,
                    color = Color(0xFF94A3B8),
                    fontFamily = InterFontFamily
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Sign up",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryBlue,
                    modifier = Modifier.clickable { onNavigateToRegister() },
                    fontFamily = InterFontFamily
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .alpha(0.5f)
            ) {
                GlideImage(
                    model = "https://lh3.googleusercontent.com/aida-public/AB6AXuDFfLiKZUjo4q2N2TzH_P9gRgTEAFiVeg7KHJ1Xhfh9Ie4sf7gR3MQIuv6xSxAJlgYK6qI_pBmIhhItOxPSwL-vSfwwC8AlguQEeu00jD5qU689bS_I-9PE-v1LFPglxDQsMGpDDgtGXTdzPDRucdrYCSaKkxseCiQqZ0cp-E6WNtOI7nhikecNbDrIgVvvsbqNk7WpdPriI5kJEz7irh4XVYpdErXXjdk3EVvpwFXgxg4dGnuEShb3A3qLShFa_OmPN3W1ekh0CHg",
                    contentDescription = "Abstract pattern",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFF101622)
                                )
                            )
                        )
                )
            }
        }

        // Error Snackbar
        if (authState.error != null) {
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                containerColor = AlertRed
            ) {
                Text(authState.error ?: "Error", color = Color.White)
            }
        }

        // Biometric Message Snackbar
        if (biometricMessage != null) {
            val message = biometricMessage
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 16.dp, end = 16.dp, bottom = if (authState.error != null) 72.dp else 16.dp),
                containerColor = Color(0xFF6366F1),
                action = {
                    TextButton(
                        onClick = { biometricMessage = null },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                    ) {
                        Text("Dismiss")
                    }
                }
            ) {
                Text(message ?: "", color = Color.White)
            }
        }

        // Biometric Error Dialog
        if (showBiometricDialog && biometricMessage != null) {
            val message = biometricMessage
            AlertDialog(
                onDismissRequest = { showBiometricDialog = false },
                title = {
                    Text(
                        text = "Biometric Login",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = message ?: "",
                        fontFamily = InterFontFamily
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = { showBiometricDialog = false },
                        colors = ButtonDefaults.textButtonColors(contentColor = PrimaryBlue)
                    ) {
                        Text(
                            text = "OK",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            )
        }
    }
}

private fun showBiometricPrompt(
    activity: FragmentActivity,
    onCredentialFound: () -> Unit,  // Callback when biometric succeeds and session found
    onNoCredentials: (String) -> Unit,  // Callback when no saved session
    onBiometricError: (String) -> Unit  // Callback when biometric fails
) {
    val biometricManager = BiometricManager.from(activity)

    when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
        BiometricManager.BIOMETRIC_SUCCESS -> {
            // Biometric is available
            try {
                val executor = ContextCompat.getMainExecutor(activity)
                val biometricPrompt = BiometricPrompt(
                    activity,
                    executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            // Biometric succeeded - user is verified
                            onCredentialFound()
                        }

                        override fun onAuthenticationFailed() {
                            // Biometric failed - fingerprint/face didn't match
                            // Don't show error, let user try again
                        }

                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                            when (errorCode) {
                                BiometricPrompt.ERROR_USER_CANCELED -> {
                                    // User cancelled - no action needed, don't show dialog
                                }
                                BiometricPrompt.ERROR_NO_BIOMETRICS,
                                BiometricPrompt.ERROR_HW_UNAVAILABLE,
                                BiometricPrompt.ERROR_CANCELED -> {
                                    onNoCredentials("Biometric not available. Please use email/password login.")
                                }
                                BiometricPrompt.ERROR_LOCKOUT -> {
                                    onBiometricError("Too many attempts. Try again in 30 seconds.")
                                }
                                BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> {
                                    onBiometricError("Biometric temporarily disabled. Use email/password to login.")
                                }
                                BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                                    onBiometricError("Biometric authentication cancelled.")
                                }
                                else -> {
                                    if (errString.contains("canceled", ignoreCase = true)) {
                                        // User cancelled silently
                                    } else {
                                        onBiometricError("Biometric error: $errString")
                                    }
                                }
                            }
                        }
                    }
                )

                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Sign in with Biometric")
                    .setSubtitle("Use your fingerprint or face to sign in")
                    .setNegativeButtonText("Cancel")
                    .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                    .setConfirmationRequired(false)
                    .build()

                biometricPrompt.authenticate(promptInfo)
            } catch (e: Exception) {
                onBiometricError("Failed to show biometric prompt: ${e.message}")
            }
        }

        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
            onNoCredentials("Biometric hardware not available on this device.")
        }

        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
            onNoCredentials("Biometric hardware is unavailable. Please try again later.")
        }

        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
            onNoCredentials("No biometric credentials enrolled. Please set up Face ID or Fingerprint in your device settings first.")
        }

        BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
            onBiometricError("Security update required. Please update your device.")
        }

        BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
            onNoCredentials("Biometric not supported on this device.")
        }

        else -> {
            onNoCredentials("Biometric authentication not available. Please use email/password to sign in.")
        }
    }
}
