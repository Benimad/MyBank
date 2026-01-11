package com.example.mybank.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.ui.unit.dp

/**
 * Animation Constants and Configurations
 * Matching the smooth animations from the HTML design
 */

object AnimationConstants {
    // Duration
    const val FAST = 200
    const val NORMAL = 300
    const val SLOW = 500
    const val SPLASH_DELAY = 2500L
    
    // Spring Configuration
    val SPRING_BOUNCY = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
    
    val SPRING_SMOOTH = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )
    
    // Button Press Animation
    const val BUTTON_PRESS_SCALE = 0.98f
    
    // Blur Effects
    val BLUR_SMALL = 40.dp
    val BLUR_MEDIUM = 80.dp
    val BLUR_LARGE = 100.dp
}

/**
 * Easing Functions
 */
object Easing {
    val EaseInOutSine = CubicBezierEasing(0.37f, 0f, 0.63f, 1f)
    val EaseOutQuart = CubicBezierEasing(0.25f, 1f, 0.5f, 1f)
    val EaseInOutQuart = CubicBezierEasing(0.76f, 0f, 0.24f, 1f)
}
