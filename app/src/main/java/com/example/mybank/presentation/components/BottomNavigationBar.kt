package com.example.mybank.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mybank.ui.theme.*

// Lime green glow color as requested
val LimeGreenGlow = Color(0xFFA3E635)
// Exact gray from prompt
val InactiveGray = Color(0xFF64748B)

sealed class BottomNavDestination(
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val label: String
) {
    object Home : BottomNavDestination(
        route = "home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        label = "Home"
    )
    
    object Cards : BottomNavDestination(
        route = "cards",
        selectedIcon = Icons.Filled.CreditCard,
        unselectedIcon = Icons.Outlined.CreditCard,
        label = "Cards"
    )
    
    object Analytics : BottomNavDestination(
        route = "analytics",
        selectedIcon = Icons.Filled.BarChart,
        unselectedIcon = Icons.Outlined.BarChart,
        label = "Analytics"
    )
    
    object Profile : BottomNavDestination(
        route = "profile",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person,
        label = "Profile"
    )
}

@Composable
fun BottomNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = remember {
        listOf(
            BottomNavDestination.Home,
            BottomNavDestination.Cards,
            BottomNavDestination.Analytics,
            BottomNavDestination.Profile
        )
    }

    // Container for the floating nav bar
    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(start = 16.dp, end = 16.dp, bottom = 24.dp, top = 8.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // The Pill Shape Background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .shadow(
                    elevation = 20.dp,
                    spotColor = Color.Black.copy(alpha = 0.5f),
                    ambientColor = PrimaryBlue.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(40.dp)
                )
                .background(
                    color = BackgroundDark.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(40.dp)
                )
                // Subtle border gradient
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.08f),
                            Color.White.copy(alpha = 0.02f)
                        )
                    ),
                    shape = RoundedCornerShape(40.dp)
                )
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { destination ->
                    val isSelected = currentRoute == destination.route || 
                        (destination.route == "home" && currentRoute.contains("home"))
                    
                    BottomNavItem(
                        destination = destination,
                        isSelected = isSelected,
                        onClick = { onNavigate(destination.route) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    destination: BottomNavDestination,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // 1. Scale Animation
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    // 2. Color Animation
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryBlue else InactiveGray,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "color"
    )

    // Easing for pulse
    val sineBounceEasing = { fraction: Float ->
        kotlin.math.sin(fraction * kotlin.math.PI / 2).toFloat()
    }

    // 3. Pulse / Glow Animation for active state
    val infiniteTransition = rememberInfiniteTransition(label = "glow_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .scale(scale)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(8.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            // Glow Effect behind icon
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .graphicsLayer {
                            scaleX = pulseScale
                            scaleY = pulseScale
                            alpha = if (isSelected) 1f else 0f
                        }
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    LimeGreenGlow.copy(alpha = 0.15f),
                                    PrimaryBlue.copy(alpha = 0.1f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )
            }

            // Icon with crossfade
            androidx.compose.animation.Crossfade(
                targetState = isSelected,
                animationSpec = tween(300),
                label = "icon_crossfade"
            ) { selected ->
                Icon(
                    imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                    contentDescription = destination.label,
                    tint = contentColor,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Label
        Text(
            text = destination.label,
            color = contentColor,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            letterSpacing = 0.5.sp,
            style = MaterialTheme.typography.labelSmall
        )
        
        // Active indicator dot (optional, adding extra polish)
        AnimatedVisibility(
            visible = isSelected,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
             Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(4.dp)
                    .background(LimeGreenGlow, CircleShape)
            )
        }
    }
}
