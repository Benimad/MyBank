package com.example.mybank.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.mybank.ui.theme.*
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeChild

sealed class GlassNavDestination(
    val route: String,
    val iconUrl: String,
    val label: String
) {
    object Home : GlassNavDestination(
        route = "home",
        iconUrl = "https://img.icons8.com/fluency-systems-regular/96/FFFFFF/home--v1.png",
        label = "Home"
    )
    
    object Analytics : GlassNavDestination(
        route = "analytics",
        iconUrl = "https://img.icons8.com/fluency-systems-regular/96/FFFFFF/combo-chart--v1.png",
        label = "Analytics"
    )
    
    object Cards : GlassNavDestination(
        route = "cards",
        iconUrl = "https://img.icons8.com/fluency-systems-regular/96/FFFFFF/bank-card-back-side.png",
        label = "Cards"
    )
    
    object Profile : GlassNavDestination(
        route = "profile",
        iconUrl = "https://img.icons8.com/fluency-systems-regular/96/FFFFFF/user-male-circle.png",
        label = "Profile"
    )
}

@Composable
fun GlassBottomNavigation(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
    showLabels: Boolean = false
) {
    val items = remember {
        listOf(
            GlassNavDestination.Home,
            GlassNavDestination.Analytics,
            GlassNavDestination.Cards,
            GlassNavDestination.Profile
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeChild(
                        state = hazeState,
                        shape = RoundedCornerShape(36.dp)
                    )
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1A2332).copy(alpha = 0.92f),
                                Color(0xFF0F1621).copy(alpha = 0.95f)
                            )
                        ),
                        shape = RoundedCornerShape(36.dp)
                    )
                    .border(
                        width = 0.5.dp,
                        color = Color.White.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(36.dp)
                    )
                    .graphicsLayer {
                        shadowElevation = 20f
                        shape = RoundedCornerShape(36.dp)
                        clip = true
                        spotShadowColor = Color.Black.copy(alpha = 0.4f)
                        ambientShadowColor = Color.Black.copy(alpha = 0.2f)
                    }
            )
            
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { destination ->
                    val isSelected = currentRoute == destination.route || 
                        (destination.route == "home" && currentRoute.contains("home"))
                    
                    GlassNavItem(
                        destination = destination,
                        isSelected = isSelected,
                        onClick = { onNavigate(destination.route) },
                        showLabel = showLabels,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun GlassNavItem(
    destination: GlassNavDestination,
    isSelected: Boolean,
    onClick: () -> Unit,
    showLabel: Boolean,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    val containerScale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "containerScale"
    )
    
    val backgroundAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.7f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "backgroundAlpha"
    )
    
    val glowAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = Spring.StiffnessLow
        ),
        label = "glowAlpha"
    )

    val iconAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.4f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "iconAlpha"
    )
    
    val labelAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.55f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "labelAlpha"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.height(40.dp)
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .scale(pulseScale)
                        .graphicsLayer { alpha = glowAlpha * shimmerAlpha * 0.6f }
                        .blur(20.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    PrimaryBlueGlow.copy(alpha = 0.8f),
                                    PrimaryBlue.copy(alpha = 0.5f),
                                    PrimaryBlueDark.copy(alpha = 0.2f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }
            
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .scale(containerScale)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                PrimaryBlue.copy(alpha = backgroundAlpha),
                                PrimaryBlueDark.copy(alpha = backgroundAlpha * 0.95f)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                GlideImage(
                    model = destination.iconUrl,
                    contentDescription = destination.label,
                    modifier = Modifier
                        .size(22.dp)
                        .graphicsLayer { alpha = iconAlpha },
                    colorFilter = ColorFilter.tint(
                        if (isSelected) Color.White else Color(0xFF64748B)
                    ),
                    contentScale = ContentScale.Fit
                )
            }
        }
        
        if (showLabel) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = destination.label,
                color = if (isSelected) Color.White else Color(0xFF94A3B8),
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                modifier = Modifier.graphicsLayer { alpha = labelAlpha },
                letterSpacing = 0.15.sp
            )
            
            if (isSelected) {
                Spacer(modifier = Modifier.height(3.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .scale(containerScale)
                        .background(
                            PrimaryBlue,
                            CircleShape
                        )
                        .graphicsLayer { 
                            alpha = backgroundAlpha
                        }
                )
            }
        }
    }
}
