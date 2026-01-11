package com.example.mybank.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mybank.ui.theme.InterFontFamily
import com.example.mybank.ui.theme.PrimaryBlue

@Composable
fun BankCard(
    cardHolderName: String,
    lastFour: String,
    expiry: String,
    cardType: String = "Platinum Debit",
    modifier: Modifier = Modifier
) {
    // Shimmer Animation
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = -1000f,
        targetValue = 2000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            Color.Transparent,
            Color.White.copy(alpha = 0.05f),
            Color.White.copy(alpha = 0.15f),
            Color.White.copy(alpha = 0.05f),
            Color.Transparent
        ),
        start = Offset(translateAnim, 0f),
        end = Offset(translateAnim + 600f, 600f), // Diagonal shimmer
        tileMode = TileMode.Clamp
    )

    Box(
        modifier = modifier
            .aspectRatio(1.586f)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF374151), // Gray 700
                        Color(0xFF1C1F27), // Surface Dark
                        Color(0xFF000000)  // Black
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
    ) {
        // 1. Background Orb (Blurred Blue)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 20.dp, y = (-20).dp)
                .size(180.dp)
                .alpha(0.5f)
                .blur(80.dp)
                .background(PrimaryBlue, CircleShape)
        )

        // 2. Shimmer Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(shimmerBrush)
        )

        // 3. Texture Overlay (Simulated with simple pattern or gradient if image not available)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.05f),
                            Color.Transparent
                        ),
                        center = Offset.Unspecified,
                        radius = 400f
                    )
                )
        )

        // 4. Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row: Logo and Wifi
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Logo Section
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Savings,
                            contentDescription = "MyBank Logo",
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "MyBank",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Text(
                        text = cardType,
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 10.sp,
                        modifier = Modifier.padding(start = 2.dp, top = 2.dp)
                    )
                }

                // Contactless Icon
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = "Contactless",
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier
                        .rotate(90f)
                        .size(28.dp)
                )
            }

            // Chip
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .width(44.dp)
                    .height(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFDE68A), // yellow-200
                                Color(0xFFEAB308)  // yellow-500
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = Color(0xFFCA8A04).copy(alpha = 0.4f), // yellow-600
                        shape = RoundedCornerShape(6.dp)
                    )
            ) {
                // Chip lines simulation
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    
                    // Main horizontal line
                    drawLine(
                        color = Color.Black.copy(alpha = 0.15f),
                        start = Offset(0f, height / 2),
                        end = Offset(width, height / 2),
                        strokeWidth = 2f
                    )
                    
                    // Vertical lines
                    drawLine(
                        color = Color.Black.copy(alpha = 0.15f),
                        start = Offset(width / 3, 0f),
                        end = Offset(width / 3, height),
                        strokeWidth = 2f
                    )
                    drawLine(
                        color = Color.Black.copy(alpha = 0.15f),
                        start = Offset(width * 2 / 3, 0f),
                        end = Offset(width * 2 / 3, height),
                        strokeWidth = 2f
                    )
                    
                    // Rounded rect in center
                    drawRoundRect(
                        color = Color.Black.copy(alpha = 0.15f),
                        topLeft = Offset(width * 0.2f, height * 0.25f),
                        size = Size(width * 0.6f, height * 0.5f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f),
                        style = Stroke(width = 2f)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Card Number
            Text(
                text = "•••• $lastFour",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                fontSize = 22.sp,
                letterSpacing = 4.sp,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // Bottom Row: Details and Mastercard/Visa circles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    // Holder
                    Column {
                        Text(
                            text = "HOLDER",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 9.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = cardHolderName.uppercase(),
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            fontSize = 13.sp,
                            letterSpacing = 0.5.sp
                        )
                    }

                    // Expires
                    Column {
                        Text(
                            text = "EXPIRES",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 9.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = expiry,
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            fontSize = 13.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Circles (Mastercard-ish)
                Box(modifier = Modifier.width(44.dp).height(28.dp)) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .align(Alignment.CenterStart)
                            .background(Color.White.copy(alpha = 0.9f), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .align(Alignment.CenterEnd)
                            .background(Color.White.copy(alpha = 0.6f), CircleShape)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun BankCardPreview() {
    Box(modifier = Modifier.padding(16.dp)) {
        BankCard(
            cardHolderName = "Alex Morgan",
            lastFour = "1234",
            expiry = "09/28"
        )
    }
}
