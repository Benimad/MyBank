package com.example.mybank.presentation.profile.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.mybank.ui.theme.*

@Composable
fun ProfileAvatar(
    photoUrl: String?,
    size: Int = 100,
    showVerifiedBadge: Boolean = true,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            ),
        contentAlignment = Alignment.BottomEnd
    ) {
        if (photoUrl != null) {
            AsyncImage(
                model = photoUrl,
                contentDescription = "Profile Photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .border(4.dp, SurfaceDark, CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(TextGray700, CircleShape)
                    .border(4.dp, SurfaceDark, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = "Default Avatar",
                    tint = TextGray400,
                    modifier = Modifier.size((size * 0.5).dp)
                )
            }
        }
        
        if (showVerifiedBadge) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(SuccessGreen, CircleShape)
                    .border(2.dp, SurfaceDark, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Verified",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun PremiumBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(
                color = PrimaryBlue.copy(alpha = 0.15f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = "PREMIUM MEMBER",
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            color = PrimaryBlue,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun SettingsSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        color = TextGray500,
        letterSpacing = 1.sp,
        modifier = modifier.padding(start = 8.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    iconBackgroundColor: Color,
    iconTintColor: Color,
    subtitle: String? = null,
    badge: String? = null,
    badgeColor: Color = SuccessGreen,
    hasToggle: Boolean = false,
    isToggleOn: Boolean = false,
    onToggle: (Boolean) -> Unit = {},
    onClick: (() -> Unit)? = null,
    showDivider: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (!hasToggle && onClick != null) {
                        Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = Color.White.copy(alpha = 0.1f)),
                            onClick = onClick
                        )
                    } else Modifier
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconBackgroundColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTintColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color.White
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = TextGray400
                    )
                }
                if (badge != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = badge,
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = badgeColor
                    )
                }
            }
            
            if (hasToggle) {
                CustomToggleSwitch(
                    checked = isToggleOn,
                    onCheckedChange = onToggle
                )
            } else if (onClick != null) {
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = "Navigate",
                    tint = TextGray500,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        if (showDivider) {
            Divider(
                color = BorderDarkSubtle,
                thickness = 1.dp,
                modifier = Modifier.padding(start = 72.dp)
            )
        }
    }
}

@Composable
fun CustomToggleSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val thumbPosition by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        label = "thumb position"
    )
    
    Box(
        modifier = modifier
            .width(44.dp)
            .height(24.dp)
            .background(
                color = if (checked) PrimaryBlue else TextGray700,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onCheckedChange(!checked) }
            )
            .padding(2.dp),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(Color.White, CircleShape)
        )
    }
}

@Composable
fun SettingsCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = SurfaceDark,
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                color = BorderDarkSubtle,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        content()
    }
}
