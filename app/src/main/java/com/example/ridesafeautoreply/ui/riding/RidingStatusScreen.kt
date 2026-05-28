package com.example.ridesafeautoreply.ui.riding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ridesafeautoreply.service.RideSafeService
import com.example.ridesafeautoreply.theme.*
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.geometry.Offset
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RidingStatusScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Stream states directly from the service static flows
    val currentSpeed by RideSafeService.currentSpeedFlow.collectAsState()
    val isCurrentlyRiding by RideSafeService.isRidingFlow.collectAsState()
    val isServiceRunning by RideSafeService.isServiceRunningFlow.collectAsState()
    val sessionDistance by RideSafeService.sessionDistanceFlow.collectAsState()
    val sessionDuration by RideSafeService.sessionDurationFlow.collectAsState()

    // Formats elapsed seconds to MM:SS or HH:MM:SS
    fun formatDuration(seconds: Long): String {
        val hrs = seconds / 3600
        val mins = (seconds % 3600) / 60
        val secs = seconds % 60
        return if (hrs > 0) {
            "%02d:%02d:%02d".format(hrs, mins, secs)
        } else {
            "%02d:%02d".format(mins, secs)
        }
    }

    val isDark = MaterialTheme.colorScheme.background == com.example.ridesafeautoreply.theme.DeepBlack
    val bgColor = if (isDark) com.example.ridesafeautoreply.theme.DeepBlack else androidx.compose.ui.graphics.Color(0xFFF9F9FB)
    val radialColor = if (isDark) androidx.compose.ui.graphics.Color(0xFF041909) else androidx.compose.ui.graphics.Color(0xFFE8F5E9)
    val cardBg = if (isDark) com.example.ridesafeautoreply.theme.CarbonGray else androidx.compose.ui.graphics.Color.White
    val cardBorder = if (isDark) com.example.ridesafeautoreply.theme.DividerGray else androidx.compose.ui.graphics.Color(0xFFE5E5EA)
    val cardBorderHighlight = if (isDark) com.example.ridesafeautoreply.theme.CarbonLight else androidx.compose.ui.graphics.Color(0xFFE5E5EA)
    val textColor = if (isDark) com.example.ridesafeautoreply.theme.PureWhite else androidx.compose.ui.graphics.Color(0xFF1C1C1E)
    val textMuted = if (isDark) com.example.ridesafeautoreply.theme.TextGray else androidx.compose.ui.graphics.Color(0xFF8E8E93)
    val greenAccent = if (isDark) com.example.ridesafeautoreply.theme.NeonGreen else androidx.compose.ui.graphics.Color(0xFF00A82D)
    val greenGlow = if (isDark) com.example.ridesafeautoreply.theme.NeonGreenGlow else androidx.compose.ui.graphics.Color(0xFF00D230)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(radialColor, bgColor),
                    radius = 900f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    colors = IconButtonDefaults.iconButtonColors(contentColor = textColor)
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Go Back")
                }
                Text(
                    text = "HANDLEBAR HUD",
                    color = textColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!isServiceRunning) {
                // Warning state if protection service is off
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsBike,
                            contentDescription = "Warning",
                            tint = textMuted,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Protection is Inactive",
                            color = textColor,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Please enable 'Start Protection' on the Home Dashboard to track riding telemetry.",
                            color = textMuted,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onBack,
                            colors = ButtonDefaults.buttonColors(containerColor = cardBg, contentColor = greenAccent),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.border(1.dp, cardBorder, RoundedCornerShape(8.dp))
                        ) {
                            Text("Go Back Dashboard", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Interactive HUD active panel
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Giant Speed retro analog dial
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(280.dp)
                    ) {
                        val maxSpeed = 120f
                        
                        Canvas(
                            modifier = Modifier.size(270.dp)
                        ) {
                            val strokeWidth = 8.dp.toPx()
                            val radius = size.minDimension / 2f
                            
                            // 1. Draw Background Glass HUD disc reflection
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(greenAccent.copy(alpha = 0.05f), Color.Transparent),
                                    radius = radius
                                ),
                                radius = radius - 8.dp.toPx(),
                                center = center
                            )
                            
                            // 2. Draw Background Dial Track
                            drawArc(
                                color = cardBorder,
                                startAngle = 135f,
                                sweepAngle = 270f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                            
                            // 2. Draw Ticks & Numbers
                            for (i in 0..120 step 5) {
                                val angle = 135f + (i / maxSpeed) * 270f
                                val angleRad = Math.toRadians(angle.toDouble())
                                val isMajor = i % 20 == 0
                                
                                val tickLength = if (isMajor) 14.dp.toPx() else 8.dp.toPx()
                                val tickThickness = if (isMajor) 3.dp.toPx() else 1.5.dp.toPx()
                                val tickColor = if (isMajor) greenAccent else textMuted
                                
                                val startX = (center.x + cos(angleRad) * (radius - tickLength - 4.dp.toPx())).toFloat()
                                val startY = (center.y + sin(angleRad) * (radius - tickLength - 4.dp.toPx())).toFloat()
                                val endX = (center.x + cos(angleRad) * (radius - 4.dp.toPx())).toFloat()
                                val endY = (center.y + sin(angleRad) * (radius - 4.dp.toPx())).toFloat()
                                
                                drawLine(
                                    color = tickColor,
                                    start = Offset(startX, startY),
                                    end = Offset(endX, endY),
                                    strokeWidth = tickThickness,
                                    cap = StrokeCap.Round
                                )
                                
                                // Draw Major Numbers
                                if (isMajor) {
                                    val textRadius = radius - tickLength - 20.dp.toPx()
                                    val numberX = (center.x + cos(angleRad) * textRadius).toFloat()
                                    val numberY = (center.y + sin(angleRad) * textRadius + 4.dp.toPx()).toFloat() // slight vertical offset alignment
                                    
                                    drawIntoCanvas { canvas ->
                                        val paint = android.graphics.Paint().apply {
                                            color = if (isDark) android.graphics.Color.WHITE else android.graphics.Color.parseColor("#1C1C1E")
                                            textSize = 11.sp.toPx()
                                            typeface = android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.BOLD)
                                            textAlign = android.graphics.Paint.Align.CENTER
                                            isAntiAlias = true
                                        }
                                        canvas.nativeCanvas.drawText(i.toString(), numberX, numberY, paint)
                                    }
                                }
                            }
                            
                            // 3. Draw Active Speed Sweeping Gauge Arc
                            val speedCoerced = currentSpeed.coerceIn(0f, maxSpeed)
                            val sweepAngleSpeed = (speedCoerced / maxSpeed) * 270f
                            
                            if (sweepAngleSpeed > 0f) {
                                drawArc(
                                    color = greenGlow,
                                    startAngle = 135f,
                                    sweepAngle = sweepAngleSpeed,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                            }
                            
                            // 4. Draw Analog Sweeping Pointer Needle
                            val needleAngle = 135f + (speedCoerced / maxSpeed) * 270f
                            val needleRad = Math.toRadians(needleAngle.toDouble())
                            
                            drawLine(
                                color = AlertRed,
                                start = Offset(
                                    (center.x + cos(needleRad) * 16.dp.toPx()).toFloat(),
                                    (center.y + sin(needleRad) * 16.dp.toPx()).toFloat()
                                ),
                                end = Offset(
                                    (center.x + cos(needleRad) * (radius - 12.dp.toPx())).toFloat(),
                                    (center.y + sin(needleRad) * (radius - 12.dp.toPx())).toFloat()
                                ),
                                strokeWidth = 3.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                            
                            // 5. Draw center cap pivot
                            drawCircle(
                                color = cardBorderHighlight,
                                radius = 16.dp.toPx(),
                                center = center
                            )
                            drawCircle(
                                color = AlertRed,
                                radius = 6.dp.toPx(),
                                center = center
                            )
                        }
                        
                        // Digital speedometer text overlay at the bottom center of dial
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(top = 100.dp) // pushed below the center pivot
                        ) {
                            Text(
                                text = "%.0f".format(currentSpeed),
                                color = greenAccent,
                                fontSize = 56.sp,
                                fontWeight = FontWeight.Black,
                                lineHeight = 56.sp
                            )
                            Text(
                                text = "KM/H",
                                color = textMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Riding / Stopped badge status
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCurrentlyRiding) greenAccent.copy(alpha = if (isDark) 0.15f else 0.25f) else cardBorderHighlight
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.border(
                            1.dp, 
                            if (isCurrentlyRiding) greenAccent.copy(alpha = 0.5f) else cardBorder, 
                            RoundedCornerShape(16.dp)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(if (isCurrentlyRiding) greenAccent else textMuted, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isCurrentlyRiding) "STATUS: RIDING" else "STATUS: STATIONARY",
                                color = if (isCurrentlyRiding) greenAccent else textMuted,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Session stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Trip distance card
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, cardBorder, RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = cardBg)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("TRIP DISTANCE", color = textMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("%.2f km".format(sessionDistance), color = textColor, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Trip duration card
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, cardBorder, RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = cardBg)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("TRIP TIMER", color = textMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(formatDuration(sessionDuration), color = textColor, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Bottom feature badges
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "AI Active",
                        tint = greenAccent.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "AI Motion Guard",
                        color = textMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 4.dp, end = 16.dp)
                    )
                    
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "GPS Active",
                        tint = greenGlow.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "GPS Safe Tracking",
                        color = textMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    }
}
