package com.example.ridesafeautoreply.ui.history

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.SportsMotorsports
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ridesafeautoreply.data.RideSession
import com.example.ridesafeautoreply.data.SettingsRepository
import com.example.ridesafeautoreply.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RideHistoryScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModelOverride: RideHistoryViewModel? = null
) {
    val context = LocalContext.current
    val viewModel: RideHistoryViewModel = viewModelOverride ?: viewModel {
        RideHistoryViewModel(SettingsRepository(context.applicationContext))
    }

    val history by viewModel.rideHistory.collectAsState()

    // Formatter helpers
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.getDefault()) }
    
    fun formatDuration(seconds: Long): String {
        val hrs = seconds / 3600
        val mins = (seconds % 3600) / 60
        val secs = seconds % 60
        return if (hrs > 0) "${hrs}h ${mins}m" else "${mins}m ${secs}s"
    }

    // Calculate summaries
    val totalRides = history.size
    val totalDistance = history.sumOf { it.distanceKm }
    val avgSpeed = if (history.isNotEmpty()) history.map { it.avgSpeedKmh }.average() else 0.0

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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        colors = IconButtonDefaults.iconButtonColors(contentColor = textColor)
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Go Back")
                    }
                    Text(
                        text = "RIDE LOGS & HISTORY",
                        color = textColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                if (history.isNotEmpty()) {
                    IconButton(
                        onClick = { viewModel.clearHistory() },
                        colors = IconButtonDefaults.iconButtonColors(contentColor = AlertRed)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear History")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Summary Analytics Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, cardBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Trip Summary".uppercase(),
                        color = greenAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SummaryStatItem("Total Trips", "$totalRides rides", modifier = Modifier.weight(1f))
                        SummaryStatItem("Total Distance", "%.1f km".format(totalDistance), modifier = Modifier.weight(1f))
                        SummaryStatItem("Avg Speed", "%.1f km/h".format(avgSpeed), modifier = Modifier.weight(1f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Trips Feed
            if (history.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SportsMotorsports,
                            contentDescription = "Empty History",
                            tint = cardBorder,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No ride sessions logged yet.",
                            color = textMuted,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Ride logs are automatically recorded when protection is active and speed exceeds your threshold.",
                            color = textMuted,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(history, key = { it.id }) { session ->
                        RideSessionItemCard(
                            session = session,
                            dateFormatter = dateFormatter,
                            durationFormatter = ::formatDuration,
                            onOpenMap = { lat, lng ->
                                try {
                                    val mapUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng")
                                    val mapIntent = Intent(Intent.ACTION_VIEW, mapUri).apply {
                                        setPackage("com.google.android.apps.maps")
                                    }
                                    // Fallback to browser if Google Maps app not installed
                                    if (mapIntent.resolveActivity(context.packageManager) != null) {
                                        context.startActivity(mapIntent)
                                    } else {
                                        val webIntent = Intent(Intent.ACTION_VIEW, mapUri)
                                        context.startActivity(webIntent)
                                    }
                                } catch (e: Exception) {
                                    // fallback launch
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng"))
                                    context.startActivity(intent)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryStatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background == com.example.ridesafeautoreply.theme.DeepBlack
    val textColor = if (isDark) com.example.ridesafeautoreply.theme.PureWhite else androidx.compose.ui.graphics.Color(0xFF1C1C1E)
    val textMuted = if (isDark) com.example.ridesafeautoreply.theme.TextGray else androidx.compose.ui.graphics.Color(0xFF8E8E93)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, color = textMuted, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, color = textColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun RideSessionItemCard(
    session: RideSession,
    dateFormatter: SimpleDateFormat,
    durationFormatter: (Long) -> String,
    onOpenMap: (Double, Double) -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == com.example.ridesafeautoreply.theme.DeepBlack
    val cardBg = if (isDark) com.example.ridesafeautoreply.theme.CarbonLight else androidx.compose.ui.graphics.Color.White
    val cardBorder = if (isDark) com.example.ridesafeautoreply.theme.DividerGray else androidx.compose.ui.graphics.Color(0xFFE5E5EA)
    val cardBorderHighlight = if (isDark) com.example.ridesafeautoreply.theme.CarbonLight else androidx.compose.ui.graphics.Color(0xFFE5E5EA)
    val textColor = if (isDark) com.example.ridesafeautoreply.theme.PureWhite else androidx.compose.ui.graphics.Color(0xFF1C1C1E)
    val textMuted = if (isDark) com.example.ridesafeautoreply.theme.TextGray else androidx.compose.ui.graphics.Color(0xFF8E8E93)
    val greenAccent = if (isDark) com.example.ridesafeautoreply.theme.NeonGreen else androidx.compose.ui.graphics.Color(0xFF00A82D)
    val greenGlow = if (isDark) com.example.ridesafeautoreply.theme.NeonGreenGlow else androidx.compose.ui.graphics.Color(0xFF00D230)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, cardBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = if (isDark) com.example.ridesafeautoreply.theme.CarbonLight else androidx.compose.ui.graphics.Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateFormatter.format(Date(session.timestamp)),
                    color = textMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = greenAccent.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "Ride Logged",
                        color = greenAccent,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))

            // Trip Telemetries
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("DISTANCE", color = textMuted, fontSize = 10.sp)
                    Text("%.2f km".format(session.distanceKm), color = textColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                
                Column {
                    Text("DURATION", color = textMuted, fontSize = 10.sp)
                    Text(durationFormatter(session.durationSeconds), color = textColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                
                Column {
                    Text("AVG SPEED", color = textMuted, fontSize = 10.sp)
                    Text("%.1f km/h".format(session.avgSpeedKmh), color = textColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Direct maps tracking click button
            Button(
                onClick = { onOpenMap(session.endLatitude, session.endLongitude) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = if (isDark) com.example.ridesafeautoreply.theme.CarbonGray else cardBorder, contentColor = greenGlow),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                Icon(imageVector = Icons.Default.Map, contentDescription = "View Map", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Google Maps: View Ending Location", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
