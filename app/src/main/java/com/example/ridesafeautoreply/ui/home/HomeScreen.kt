package com.example.ridesafeautoreply.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.example.ridesafeautoreply.*
import com.example.ridesafeautoreply.data.SettingsRepository
import com.example.ridesafeautoreply.theme.*
import com.example.ridesafeautoreply.ui.common.PermissionHandler
import com.example.ridesafeautoreply.ui.common.hasAllPermissions

@Composable
fun HomeScreen(
    onNavigate: (NavKey) -> Unit,
    modifier: Modifier = Modifier,
    viewModelOverride: HomeScreenViewModel? = null
) {
    val context = LocalContext.current
    val viewModel: HomeScreenViewModel = viewModelOverride ?: viewModel {
        HomeScreenViewModel(SettingsRepository(context.applicationContext))
    }

    val isProtectionActive by viewModel.isProtectionActive.collectAsState()
    val totalDistance by viewModel.totalDistance.collectAsState()
    val totalHours by viewModel.totalRidingHours.collectAsState()
    val currentSpeed by viewModel.currentSpeed.collectAsState()
    val isCurrentlyRiding by viewModel.isCurrentlyRiding.collectAsState()

    var showPermissionDialog by remember { mutableStateOf(false) }

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

    // Pulse animation for active protection glow
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val buttonColor by animateColorAsState(
        targetValue = if (isProtectionActive) greenAccent else (if (isDark) cardBorderHighlight else androidx.compose.ui.graphics.Color(0xFFE5E5EA)),
        label = "buttonColor"
    )

    if (showPermissionDialog) {
        PermissionHandler(
            onDismiss = { showPermissionDialog = false },
            onPermissionsGranted = {
                viewModel.toggleProtection(context)
            }
        )
    }

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
            // Top App Title & Tagline
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Shield Logo",
                        tint = greenAccent,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "RIDE",
                        color = textColor,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "SAFE",
                        color = greenAccent,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp
                    )
                }
                Text(
                    text = "“Smart Auto-Reply & Live GPS HUD Guard”",
                    color = textMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Pulse glowing ring and central power toggle button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(220.dp)
                    .border(
                        1.dp, 
                        if (isProtectionActive) greenAccent.copy(alpha = glowAlpha) else cardBorder, 
                        CircleShape
                    )
                    .padding(8.dp)
                    .clip(CircleShape)
                    .clickable {
                        if (!isProtectionActive) {
                            // Check permissions before activating
                            if (hasAllPermissions(context)) {
                                viewModel.toggleProtection(context)
                            } else {
                                showPermissionDialog = true
                            }
                        } else {
                            viewModel.toggleProtection(context)
                        }
                    }
            ) {
                // Glow circle
                if (isProtectionActive) {
                    Box(
                        modifier = Modifier
                            .size(190.dp)
                            .border(
                                width = 8.dp,
                                brush = Brush.radialGradient(
                                    colors = listOf(greenAccent.copy(alpha = glowAlpha * 0.4f), Color.Transparent)
                                ),
                                shape = CircleShape
                            )
                    )
                }

                // Inner circle power button
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .shadow(
                            elevation = if (isProtectionActive) 24.dp else 8.dp,
                            shape = CircleShape,
                            clip = true,
                            ambientColor = greenAccent,
                            spotColor = greenAccent
                        )
                        .background(buttonColor)
                        .border(
                            width = 2.dp,
                            brush = Brush.verticalGradient(
                                colors = if (isProtectionActive) listOf(greenAccent, greenGlow) else listOf(cardBorder, cardBorderHighlight)
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = "Power Toggle",
                        tint = if (isProtectionActive) (if (isDark) Color.Black else Color.White) else textMuted,
                        modifier = Modifier.size(56.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status message
            Text(
                text = if (isProtectionActive) "PROTECTION RUNNING" else "PROTECTION OFF",
                color = if (isProtectionActive) greenAccent else textMuted,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            
            if (isCurrentlyRiding && isProtectionActive) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(greenGlow, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Riding Telemetry Active (%.1f km/h)".format(currentSpeed),
                        color = greenGlow,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Cumulative stats card row
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        Brush.verticalGradient(
                            colors = listOf(greenAccent.copy(alpha = 0.25f), Color.Transparent)
                        ),
                        RoundedCornerShape(12.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TOTAL RIDE STATS",
                            color = textMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Row(modifier = Modifier.padding(top = 6.dp)) {
                            Column(modifier = Modifier.padding(end = 24.dp)) {
                                Text("Distance", color = textMuted, fontSize = 11.sp)
                                Text("%.2f km".format(totalDistance), color = textColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Duration", color = textMuted, fontSize = 11.sp)
                                Text("%.1f hrs".format(totalHours), color = textColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    IconButton(
                        onClick = { viewModel.resetStats() },
                        colors = IconButtonDefaults.iconButtonColors(contentColor = AlertRed.copy(alpha = 0.8f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Stats",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Journey Logs & Map History Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .border(
                        1.dp,
                        Brush.horizontalGradient(
                            colors = listOf(greenAccent.copy(alpha = 0.4f), Color.Transparent)
                        ),
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onNavigate(RideHistory) },
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(greenAccent.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SportsMotorsports,
                                contentDescription = "Trip History",
                                tint = greenAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "RideSafe Journey Logs",
                                color = textColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Trip history, average speeds & maps tracking",
                                color = textMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                    
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "View History",
                        tint = textMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Navigation grid cards
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    MenuCard(
                        title = "Handlebar HUD",
                        subtitle = "Active speed meter",
                        icon = Icons.Default.DirectionsBike,
                        tint = greenAccent,
                        onClick = { onNavigate(RidingStatus) }
                    )
                }
                item {
                    MenuCard(
                        title = "Reply Rules",
                        subtitle = "Whitelist contacts",
                        icon = Icons.Default.Contacts,
                        tint = greenGlow,
                        onClick = { onNavigate(ContactsSelection) }
                    )
                }
                item {
                    MenuCard(
                        title = "SMS Template",
                        subtitle = "Custom reply message",
                        icon = Icons.Default.Sms,
                        tint = greenAccent,
                        onClick = { onNavigate(MessageCustomization) }
                    )
                }
                item {
                    MenuCard(
                        title = "App Settings",
                        subtitle = "Speed, AI, Location",
                        icon = Icons.Default.Settings,
                        tint = greenGlow,
                        onClick = { onNavigate(Settings) }
                    )
                }
            }
        }
    }
}

@Composable
fun MenuCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == com.example.ridesafeautoreply.theme.DeepBlack
    val cardBg = if (isDark) com.example.ridesafeautoreply.theme.CarbonGray else androidx.compose.ui.graphics.Color.White
    val textColor = if (isDark) com.example.ridesafeautoreply.theme.PureWhite else androidx.compose.ui.graphics.Color(0xFF1C1C1E)
    val textMuted = if (isDark) com.example.ridesafeautoreply.theme.TextGray else androidx.compose.ui.graphics.Color(0xFF8E8E93)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .border(
                1.dp,
                Brush.verticalGradient(
                    colors = listOf(tint.copy(alpha = if (isDark) 0.3f else 0.5f), Color.Transparent)
                ),
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
            
            Column {
                Text(
                    text = title,
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = subtitle,
                    color = textMuted,
                    fontSize = 10.sp
                )
            }
        }
    }
}
