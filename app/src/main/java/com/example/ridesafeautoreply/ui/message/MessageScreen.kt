package com.example.ridesafeautoreply.ui.message

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ridesafeautoreply.data.SettingsRepository
import com.example.ridesafeautoreply.theme.*

@Composable
fun MessageScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModelOverride: MessageViewModel? = null
) {
    val context = LocalContext.current
    val viewModel: MessageViewModel = viewModelOverride ?: viewModel {
        MessageViewModel(SettingsRepository(context.applicationContext))
    }

    val autoReplyMessage by viewModel.autoReplyMessage.collectAsState()
    var currentText by remember(autoReplyMessage) { mutableStateOf(autoReplyMessage) }

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
                    text = "SMS REPLY MESSAGE",
                    color = textColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main template text editor card
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
                        text = "COMPOSE TEMPLATE",
                        color = greenAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = currentText,
                        onValueChange = {
                            currentText = it
                            viewModel.updateMessage(it)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .background(cardBorderHighlight, RoundedCornerShape(8.dp)),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = greenAccent,
                            unfocusedBorderColor = cardBorder,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            cursorColor = greenAccent
                        ),
                        shape = RoundedCornerShape(8.dp),
                        placeholder = { Text("Compose message here...", color = textMuted, fontSize = 13.sp) }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Limit Info",
                                tint = textMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "1 SMS = 160 characters",
                                color = textMuted,
                                fontSize = 11.sp
                            )
                        }
                        Text(
                            text = "${currentText.length} chars",
                            color = if (currentText.length > 160) AlertRed else greenGlow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Realistic visual chat bubbles preview
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "VISUAL PREVIEW",
                    color = textMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, cardBorder, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Phone Status/Contact header inside preview
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(cardBorder, RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("J", color = textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("John Doe (Caller)", color = textColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Mobile", color = textMuted, fontSize = 10.sp)
                            }
                        }

                        // Horizontal Divider inside card
                        HorizontalDivider(color = cardBorder, modifier = Modifier.padding(bottom = 12.dp))

                        // Incoming Text bubble (left)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(cardBorderHighlight, RoundedCornerShape(12.dp, 12.dp, 12.dp, 0.dp))
                                    .padding(12.dp)
                                    .widthIn(max = 240.dp)
                            ) {
                                Text(
                                    text = "Hey, are you free to talk right now? I tried calling you.",
                                    color = textColor,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Outgoing auto-reply text bubble (right)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Column(
                                horizontalAlignment = Alignment.End,
                                modifier = Modifier.widthIn(max = 240.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(greenAccent, RoundedCornerShape(12.dp, 12.dp, 0.dp, 12.dp))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = currentText.ifEmpty { "[SMS Message Template is empty]" },
                                        color = if (isDark) Color.Black else Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Auto-replied via RideSafe • Just now",
                                    color = textMuted,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
