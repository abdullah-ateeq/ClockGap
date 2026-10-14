package com.example.clockgap.ui.home.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clockgap.domain.ClockGapFormatter
import com.example.clockgap.model.ClockGapResult
import java.time.LocalTime
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HorizontalGapVisual(
    result: ClockGapResult,
    is24Hour: Boolean = false,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.outlineVariant
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    val compactDurationText = buildString {
        if (result.hours > 0) append("${result.hours}h ")
        append("${result.minutes}m")
    }.trim()

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Label Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "TIME A",
                        style = MaterialTheme.typography.labelSmall,
                        color = onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = ClockGapFormatter.formatTime(result.timeA, is24Hour),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    tonalElevation = 1.dp
                ) {
                    Text(
                        text = compactDurationText,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "TIME B",
                        style = MaterialTheme.typography.labelSmall,
                        color = onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = ClockGapFormatter.formatTime(result.timeB, is24Hour),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Canvas bar with 2 dots and connecting line
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
            ) {
                val startX = 24.dp.toPx()
                val endX = size.width - 24.dp.toPx()
                val centerY = size.height / 2f
                val dotRadius = 6.dp.toPx()

                // Connecting line
                drawLine(
                    color = primaryColor,
                    start = Offset(startX, centerY),
                    end = Offset(endX, centerY),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Start dot (Time A)
                drawCircle(
                    color = primaryColor,
                    radius = dotRadius,
                    center = Offset(startX, centerY)
                )

                // End dot (Time B)
                drawCircle(
                    color = primaryColor,
                    radius = dotRadius,
                    center = Offset(endX, centerY)
                )
            }
        }
    }
}

@Composable
fun Circular24HourVisual(
    timeA: LocalTime,
    timeB: LocalTime,
    result: ClockGapResult,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.outlineVariant
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    val hoursA = timeA.hour + timeA.minute / 60f
    val hoursB = timeB.hour + timeB.minute / 60f

    // 24 hours mapped to 360 degrees: 1 hour = 15 degrees. 0h (midnight) is top (-90 degrees)
    val angleA = (hoursA * 15f - 90f)
    val angleB = (hoursB * 15f - 90f)

    // Calculate clockwise distance from A to B in degrees
    val clockwiseDegrees = ((angleB - angleA) % 360f + 360f) % 360f
    val (startAngle, sweepAngle) = if (clockwiseDegrees <= 180f) {
        Pair(angleA, clockwiseDegrees)
    } else {
        Pair(angleB, 360f - clockwiseDegrees)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "24-HOUR CLOCK SPAN",
                style = MaterialTheme.typography.labelSmall,
                color = labelColor,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier.size(130.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(110.dp)) {
                    val strokeWidth = 5.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2f
                    val center = Offset(size.width / 2f, size.height / 2f)

                    // Background 24h circle track
                    drawCircle(
                        color = trackColor.copy(alpha = 0.5f),
                        radius = radius,
                        center = center,
                        style = Stroke(width = strokeWidth)
                    )

                    // Connecting shortest arc
                    if (sweepAngle > 0f) {
                        drawArc(
                            color = primaryColor,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2f, radius * 2f),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    // Dot A
                    val radA = Math.toRadians(angleA.toDouble())
                    val dotAX = (center.x + radius * cos(radA)).toFloat()
                    val dotAY = (center.y + radius * sin(radA)).toFloat()
                    drawCircle(
                        color = primaryColor,
                        radius = 6.dp.toPx(),
                        center = Offset(dotAX, dotAY)
                    )

                    // Dot B
                    val radB = Math.toRadians(angleB.toDouble())
                    val dotBX = (center.x + radius * cos(radB)).toFloat()
                    val dotBY = (center.y + radius * sin(radB)).toFloat()
                    drawCircle(
                        color = secondaryColor,
                        radius = 6.dp.toPx(),
                        center = Offset(dotBX, dotBY)
                    )
                }

                // Center label
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${result.hours}h ${result.minutes}m",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Shortest",
                        style = MaterialTheme.typography.labelSmall,
                        color = labelColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Legend
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(primaryColor, CircleShape)
                    )
                    Text(
                        text = "Time A",
                        style = MaterialTheme.typography.labelSmall,
                        color = labelColor
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(secondaryColor, CircleShape)
                    )
                    Text(
                        text = "Time B",
                        style = MaterialTheme.typography.labelSmall,
                        color = labelColor
                    )
                }
            }
        }
    }
}
