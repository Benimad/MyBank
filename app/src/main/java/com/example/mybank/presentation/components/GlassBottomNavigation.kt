package com.example.mybank.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeChild

sealed class GlassNavDestination(
    val route: String,
    val iconUrl: String,
    val label: String
) {
    object Home : GlassNavDestination(
        route = "home",
        iconUrl = "https://cdn-icons-png.flaticon.com/512/609/609803.png",
        label = "Home"
    )
    
    object Cards : GlassNavDestination(
        route = "cards",
        iconUrl = "https://cdn-icons-png.flaticon.com/512/2695/2695971.png",
        label = "Cards"
    )
    
    object Analytics : GlassNavDestination(
        route = "analytics",
        iconUrl = "https://cdn-icons-png.flaticon.com/512/3106/3106773.png",
        label = "Analytics"
    )
    
    object Profile : GlassNavDestination(
        route = "profile",
        iconUrl = "https://cdn-icons-png.flaticon.com/512/456/456212.png",
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
            GlassNavDestination.Cards,
            GlassNavDestination.Analytics,
            GlassNavDestination.Profile
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeChild(
                        state = hazeState,
                        shape = RoundedCornerShape(32.dp)
                    )
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0x26FFFFFF),
                                Color(0x1A1E2433)
                            )
                        ),
                        shape = RoundedCornerShape(32.dp)
                    )
                    .border(
                        width = 0.8.dp,
                        color = Color(0x26FFFFFF),
                        shape = RoundedCornerShape(32.dp)
                    )
                    .graphicsLayer {
                        shadowElevation = 12f
                        shape = RoundedCornerShape(32.dp)
                        clip = true
                    }
            )
            
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
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
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "containerScale"
    )
    
    val backgroundAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "backgroundAlpha"
    )
    
    val glowAlpha by animateFloatAsState(
        targetValue = if (isSelected) 0.65f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "glowAlpha"
    )

    val iconAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.5f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "iconAlpha"
    )
    
    val underlineWidth by animateDpAsState(
        targetValue = if (isSelected) 20.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "underlineWidth"
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
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(40.dp)
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .scale(containerScale * 1.15f)
                        .graphicsLayer { alpha = glowAlpha }
                        .blur(14.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF4169E1),
                                    Color(0xFF3B5FE8).copy(alpha = 0.4f)
                                )
                            ),
                            shape = RoundedCornerShape(14.dp)
                        )
                )
            }
            
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(38.dp)
                    .scale(containerScale)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF4169E1).copy(alpha = backgroundAlpha),
                                Color(0xFF3B5FE8).copy(alpha = backgroundAlpha * 0.9f)
                            )
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )
            ) {
                GlideImage(
                    model = destination.iconUrl,
                    contentDescription = destination.label,
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer { alpha = iconAlpha },
                    colorFilter = ColorFilter.tint(
                        if (isSelected) Color.White else Color(0xFFB0B8C8)
                    ),
                    contentScale = ContentScale.Fit
                )
            }
        }
        
        Spacer(modifier = Modifier.height(1.dp))
        Box(
            modifier = Modifier
                .width(underlineWidth)
                .height(2.dp)
                .background(
                    color = Color(0xFF4169E1),
                    shape = RoundedCornerShape(1.dp)
                )
        )
    }
}
