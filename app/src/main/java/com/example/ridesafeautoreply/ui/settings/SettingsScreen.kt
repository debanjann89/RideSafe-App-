package com.example.ridesafeautoreply.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ridesafeautoreply.data.SettingsRepository
import com.example.ridesafeautoreply.theme.*

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModelOverride: SettingsViewModel? = null
) {
    val context = LocalContext.current
    val viewModel: SettingsViewModel = viewModelOverride ?: viewModel {
        SettingsViewModel(SettingsRepository(context.applicationContext))
    }

    val speedThreshold by viewModel.speedThreshold.collectAsState()
    val isAiSmartModeEnabled by viewModel.isAiSmartModeEnabled.collectAsState()
    val isEmergencyTrackingEnabled by viewModel.isEmergencyTrackingEnabled.collectAsState()
    val isTestModeEnabled by viewModel.isTestModeEnabled.collectAsState()
    val isDarkThemeEnabled by viewModel.isDarkThemeEnabled.collectAsState()
    
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

    var isBatteryExcluded by remember { mutableStateOf(viewModel.isBatteryOptimizedExcluded(context)) }

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
                    text = "APP SETTINGS",
                    color = textColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Speed Threshold Slider Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, cardBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "RIDING SPEED THRESHOLD",
                        color = greenAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Assume riding above:",
                            color = textColor,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${speedThreshold.toInt()} km/h",
                            color = greenAccent,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Slider(
                        value = speedThreshold,
                        onValueChange = { viewModel.setSpeedThreshold(it) },
                        valueRange = 5f..50f,
                        steps = 45,
                        colors = SliderDefaults.colors(
                            thumbColor = greenAccent,
                            activeTrackColor = greenAccent,
                            inactiveTrackColor = cardBorder
                        ),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    
                    Text(
                        text = "Speeds below this threshold will not trigger auto replies, ensuring safety when stationary or walking.",
                        color = textMuted,
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dark Theme Toggle Card
            ToggleSettingCard(
                title = "DARK THEME MODE",
                description = "Enable Dark Mode for high-contrast carbon graphics (recommended for night riding), or turn off for a premium mint-glow Light Mode.",
                icon = Icons.Default.Brightness4,
                iconColor = greenAccent,
                checked = isDarkThemeEnabled,
                onCheckedChange = { viewModel.setDarkThemeEnabled(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // AI Smart Mode Toggle Card
            ToggleSettingCard(
                title = "AI SMART GUARD",
                description = "Leverage accelerometer sensor vibrations & Bluetooth helmet audio to increase riding detection accuracy and eliminate fake GPS speed spikes.",
                icon = Icons.Default.Bolt,
                iconColor = greenGlow,
                checked = isAiSmartModeEnabled,
                onCheckedChange = { viewModel.setAiSmartModeEnabled(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Emergency Tracking Toggle Card
            ToggleSettingCard(
                title = "EMERGENCY GPS TRACKING",
                description = "Automatically texts your live Google Maps location coordinates to selected family emergency contacts every 30 minutes during active rides.",
                icon = Icons.Default.FamilyRestroom,
                iconColor = greenAccent,
                checked = isEmergencyTrackingEnabled,
                onCheckedChange = { viewModel.setEmergencyTrackingEnabled(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Force Test Mode Toggle Card
            ToggleSettingCard(
                title = "FORCE AUTO-REPLY (TEST MODE)",
                description = "Forces SMS auto-reply to trigger on incoming calls regardless of your speed. Use this to verify call interception and whitelist rules from home.",
                icon = Icons.Default.Bolt,
                iconColor = greenGlow,
                checked = isTestModeEnabled,
                onCheckedChange = { viewModel.setTestModeEnabled(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Battery Optimization Exclusion Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, cardBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.BatteryAlert,
                        contentDescription = "Battery Optimization",
                        tint = if (isBatteryExcluded) greenAccent else AlertRed,
                        modifier = Modifier.size(32.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "BATTERY OPTIMIZATION",
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = if (isBatteryExcluded) "Excluded from restriction (Recommended)" else "Restricting background services (May kill protection)",
                            color = textMuted,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    if (!isBatteryExcluded) {
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                        data = Uri.parse("package:${context.packageName}")
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // Fallback to general settings
                                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                    context.startActivity(intent)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = greenAccent, contentColor = if (isDark) Color.Black else Color.White),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Fix", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ToggleSettingCard(
    title: String,
    description: String,
    icon: ImageVector,
    iconColor: Color,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == com.example.ridesafeautoreply.theme.DeepBlack
    val cardBg = if (isDark) com.example.ridesafeautoreply.theme.CarbonGray else androidx.compose.ui.graphics.Color.White
    val cardBorder = if (isDark) com.example.ridesafeautoreply.theme.DividerGray else androidx.compose.ui.graphics.Color(0xFFE5E5EA)
    val cardBorderHighlight = if (isDark) com.example.ridesafeautoreply.theme.CarbonLight else androidx.compose.ui.graphics.Color(0xFFE5E5EA)
    val textColor = if (isDark) com.example.ridesafeautoreply.theme.PureWhite else androidx.compose.ui.graphics.Color(0xFF1C1C1E)
    val textMuted = if (isDark) com.example.ridesafeautoreply.theme.TextGray else androidx.compose.ui.graphics.Color(0xFF8E8E93)
    val greenAccent = if (isDark) com.example.ridesafeautoreply.theme.NeonGreen else androidx.compose.ui.graphics.Color(0xFF00A82D)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, cardBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                
                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = if (isDark) Color.Black else Color.White,
                        checkedTrackColor = greenAccent,
                        uncheckedThumbColor = textMuted,
                        uncheckedTrackColor = cardBorderHighlight
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Text(
                text = description,
                color = textMuted,
                fontSize = 11.sp,
                lineHeight = 14.sp
            )
        }
    }
}
