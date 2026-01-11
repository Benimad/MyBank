package com.example.mybank.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ====================== DARK MODE BANKING PALETTE 2026 PREMIUM ======================

// Primary Blue (Main brand color - Premium fintech 2026)
val PrimaryBlue = Color(0xFF2563EB)
val PrimaryBlueLight = Color(0xFF3B82F6)
val PrimaryBlueDark = Color(0xFF1E40AF)
val PrimaryBlueHover = Color(0xFF1D4ED8)
val PrimaryBlueGlow = Color(0xFF60A5FA)

// Dark Mode Background Colors (from HTML #0f172a and #1e293b)
val BackgroundDark = Color(0xFF0F172A)      // background-dark from HTML
val SurfaceDark = Color(0xFF1E293B)          // surface-dark from HTML
val SurfaceDarkVariant = Color(0xFF2C3342)   // Slightly lighter surface

// ====================== GLASSMORPHISM PREMIUM COLORS 2026 ======================

// Glass Effect Colors (ultra-subtle, professional)
val GlassWhite = Color.White.copy(alpha = 0.08f)
val GlassWhiteBorder = Color.White.copy(alpha = 0.15f)
val GlassWhiteHighlight = Color.White.copy(alpha = 0.20f)
val GlassBlur = Color(0xFF1E293B).copy(alpha = 0.30f)
val GlassShadow = Color.Black.copy(alpha = 0.08f)

// Glass Card Specific
val GlassCardBg = Color(0xFF1E293B).copy(alpha = 0.09f)
val GlassCardBorder = Color.White.copy(alpha = 0.12f)
val GlassNavBg = Color(0xFF1E293B).copy(alpha = 0.25f)
val GlassNavBorder = Color.White.copy(alpha = 0.10f)

// Chip Colors
val GoldLight = Color(0xFFFFD700)
val GoldDark = Color(0xFFDAA520)


// Light Mode Colors (fallback)
val BackgroundLight = Color(0xFFF6F6F8)
val SurfaceLight = Color(0xFFFFFFFF)

// Status Colors (from HTML)
val SuccessGreen = Color(0xFF10B981)         // success color
val SuccessGreenDark = Color(0xFF059669)
val SuccessGreenLight = Color(0xFF34D399)
val AlertRed = Color(0xFFEF4444)             // alert color
val AlertRedDark = Color(0xFFDC2626)
val AlertRedLight = Color(0xFFF87171)

// Warning/Achievement Color
val WarningYellow = Color(0xFFFBBF24)
val WarningYellowLight = Color(0xFFFCD34D)
val WarningOrange = Color(0xFFFF9800)

// Text Colors for Dark Mode
val TextWhite = Color(0xFFFFFFFF)
val TextGray100 = Color(0xFFF3F4F6)
val TextGray200 = Color(0xFFE5E7EB)
val TextGray300 = Color(0xFFD1D5DB)
val TextGray400 = Color(0xFF9CA3AF)
val TextGray500 = Color(0xFF6B7280)
val TextGray600 = Color(0xFF4B5563)
val TextGray700 = Color(0xFF374151)
val TextGray800 = Color(0xFF1F2937)
val TextGray900 = Color(0xFF111827)

// Border Colors for Dark Mode
val BorderDark = Color(0xFF374151)           // gray-700
val BorderDarkLight = Color(0xFF4B5563)      // gray-600
val BorderDarkSubtle = Color(0xFF1F2937)     // gray-800

// Transaction Icon Background Colors (Dark Mode)
val RedIconBgDark = Color(0xFF7F1D1D).copy(alpha = 0.3f)    // red-900/30
val OrangeIconBgDark = Color(0xFF7C2D12).copy(alpha = 0.3f) // orange-900/30
val GreenIconBgDark = Color(0xFF14532D).copy(alpha = 0.3f)  // green-900/30
val BlueIconBgDark = Color(0xFF1E3A8A).copy(alpha = 0.3f)   // blue-900/30

// Transaction Icon Tint Colors (Dark Mode)
val RedIconTintDark = Color(0xFFF87171)      // red-400
val OrangeIconTintDark = Color(0xFFFB923C)   // orange-400
val GreenIconTintDark = Color(0xFF4ADE80)    // green-400
val BlueIconTintDark = Color(0xFF60A5FA)     // blue-400

// ====================== GRADIENT BRUSHES 2026 ======================

// Premium Glass Background Gradient (very subtle)
val GlassBackgroundGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF0F172A),
        Color(0xFF0A0F1E),
        Color(0xFF050812)
    )
)

// Primary Card Gradient (Blue gradient for balance card)
val PrimaryCardGradient = Brush.linearGradient(
    colors = listOf(
        PrimaryBlue,
        PrimaryBlueDark
    )
)

// Dark Card Gradient (for insight/ad card)
val DarkCardGradient = Brush.horizontalGradient(
    colors = listOf(
        SurfaceDark,
        BackgroundDark
    )
)

// Savings Goal Card Gradient
val SavingsCardGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF111827),
        Color(0xFF1F2937)
    )
)

// Bottom Navigation Background
val BottomNavBackground = BackgroundDark.copy(alpha = 0.95f)

// Floating Action Button Gradient
val FabGradient = Brush.linearGradient(
    colors = listOf(
        PrimaryBlue,
        PrimaryBlueDark
    )
)

// ====================== SHADOW COLORS ======================
val PrimaryShadow = PrimaryBlue.copy(alpha = 0.4f)
val DarkShadow = Color.Black.copy(alpha = 0.3f)
val LightShadow = Color.Black.copy(alpha = 0.1f)

// ====================== LEGACY COMPATIBILITY ======================
// These are kept for backward compatibility with other screens

val PurplePrimary = PrimaryBlue
val PurpleLight = PrimaryBlueLight
val PurpleDark = PrimaryBlueDark
val EmeraldGreen = SuccessGreen
val EmeraldGreenLight = SuccessGreenLight
val EmeraldGreenDark = SuccessGreenDark
val ErrorRed = AlertRed
val ErrorRedLight = AlertRedLight
val ErrorRedDark = AlertRedDark
val BlueCyan = Color(0xFF4FD8FF)
val TextSlate900 = TextGray900
val TextSlate700 = TextGray700
val TextSlate500 = TextGray500
val TextSlate400 = TextGray400
val TextSlate300 = TextGray300

val BackgroundLightColor = BackgroundLight
val BackgroundDarkColor = BackgroundDark
val SurfaceLightColor = SurfaceLight
val SurfaceDarkColor = SurfaceDark

// Login Background Gradient
val LoginBackgroundGradient = Brush.verticalGradient(
    colors = listOf(
        BackgroundLight,
        Color(0xFFFAFAFA),
        Color(0xFFFCFCFC),
        Color(0xFFFFFFFF)
    )
)

// Splash Screen Gradient
val SplashGradient = Brush.linearGradient(
    colors = listOf(
        PrimaryBlue,
        Color(0xFF0A1F3D)
    )
)

