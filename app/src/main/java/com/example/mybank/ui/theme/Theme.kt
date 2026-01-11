package com.example.mybank.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = PrimaryBlueDark,
    onPrimaryContainer = Color.White,
    
    secondary = EmeraldGreen,
    onSecondary = Color.White,
    secondaryContainer = EmeraldGreenDark,
    onSecondaryContainer = Color.White,
    
    tertiary = BlueCyan,
    onTertiary = Color.Black,
    
    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorRedDark,
    onErrorContainer = Color.White,
    
    background = BackgroundDarkColor,
    onBackground = Color.White,
    surface = SurfaceDarkColor,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF232D3F),
    onSurfaceVariant = TextSlate400,
    
    outline = Color(0xFF3A3A3A),
    outlineVariant = Color(0xFF2A2A2A)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = PrimaryBlueLight,
    onPrimaryContainer = PrimaryBlueDark,
    
    secondary = EmeraldGreen,
    onSecondary = Color.White,
    secondaryContainer = EmeraldGreenLight,
    onSecondaryContainer = EmeraldGreenDark,
    
    tertiary = BlueCyan,
    onTertiary = Color.Black,
    
    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorRedLight,
    onErrorContainer = ErrorRedDark,
    
    background = BackgroundLightColor,
    onBackground = TextSlate900,
    surface = SurfaceLightColor,
    onSurface = TextSlate900,
    surfaceVariant = Color(0xFFF5F5F7),
    onSurfaceVariant = TextSlate500,
    
    outline = Color(0xFFE0E0E0),
    outlineVariant = Color(0xFFF0F0F0)
)

@Composable
fun MyBankTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
