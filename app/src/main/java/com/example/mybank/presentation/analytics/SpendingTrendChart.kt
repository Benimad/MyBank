package com.example.mybank.presentation.analytics

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.dp

@Composable
fun SpendingTrendChart(
    dataPoints: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFF1152d4)
) {
    if (dataPoints.isEmpty()) return

    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(dataPoints) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 1500,
                easing = FastOutSlowInEasing
            )
        )
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val spacing = width / (dataPoints.size - 1)
        
        // Normalize data points to fit height
        val maxData = dataPoints.maxOrNull() ?: 1f
        val minData = dataPoints.minOrNull() ?: 0f
        val range = maxData - minData
        // Add some padding to top and bottom (10%)
        val verticalPadding = height * 0.1f
        val usableHeight = height - (verticalPadding * 2)
        
        val points = dataPoints.mapIndexed { index, value ->
            val normalizedValue = if (range == 0f) 0.5f else (value - minData) / range
            Offset(
                x = index * spacing,
                y = height - verticalPadding - (normalizedValue * usableHeight)
            )
        }

        val path = Path()
        val fillPath = Path()

        if (points.isNotEmpty()) {
            path.moveTo(points.first().x, points.first().y)
            fillPath.moveTo(points.first().x, height) // Start at bottom-left
            fillPath.lineTo(points.first().x, points.first().y)

            for (i in 0 until points.size - 1) {
                val p0 = points[i]
                val p1 = points[i + 1]
                
                // Cubic Bezier Control Points
                val controlPoint1 = Offset(p0.x + (p1.x - p0.x) / 2, p0.y)
                val controlPoint2 = Offset(p0.x + (p1.x - p0.x) / 2, p1.y)

                path.cubicTo(
                    controlPoint1.x, controlPoint1.y,
                    controlPoint2.x, controlPoint2.y,
                    p1.x, p1.y
                )
                
                fillPath.cubicTo(
                    controlPoint1.x, controlPoint1.y,
                    controlPoint2.x, controlPoint2.y,
                    p1.x, p1.y
                )
            }
            
            fillPath.lineTo(points.last().x, height) // End at bottom-right
            fillPath.close()
        }

        // Draw Gradient Fill
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    lineColor.copy(alpha = 0.3f),
                    lineColor.copy(alpha = 0.0f)
                ),
                startY = 0f,
                endY = height
            )
        )

        // Draw Animated Line
        // We can't easily animate path drawing along the path with PathEffect alone for cubic curves perfectly without path measure
        // But for simplicity in Compose Canvas without native PathMeasure in standard drawPath, 
        // we usually use a clipRect or similar if we want left-to-right reveal.
        // Or simpler: just use alpha or simple scale if complex.
        // However, let's try a simple clip reveal.
        
        clipRect(
            left = 0f,
            top = 0f,
            right = width * animationProgress.value,
            bottom = height
        ) {
            // Draw Glow (Outer Line)
            drawPath(
                path = path,
                color = lineColor.copy(alpha = 0.5f),
                style = Stroke(
                    width = 8.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )
            
            // Draw Main Line
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(
                    width = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }
    }
}
