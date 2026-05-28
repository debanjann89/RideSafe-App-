package com.example.ridesafeautoreply.ui.contacts

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ridesafeautoreply.data.Contact
import com.example.ridesafeautoreply.data.SettingsRepository
import com.example.ridesafeautoreply.theme.*

@Composable
fun ContactsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModelOverride: ContactsViewModel? = null
) {
    val context = LocalContext.current
    val viewModel: ContactsViewModel = viewModelOverride ?: viewModel {
        ContactsViewModel(SettingsRepository(context.applicationContext))
    }
    
    val replyToSelectedOnly by viewModel.isSelectedContactsOnly.collectAsState()
    val whitelistedContacts by viewModel.whitelistedContacts.collectAsState()
    val emergencyContacts by viewModel.emergencyContacts.collectAsState()

    var activeTab by remember { mutableIntStateOf(0) } // 0 = Whitelist, 1 = Emergency Contacts

    // Launcher for Whitelist Picker
    val whitelistPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri ->
        uri?.let {
            resolveContact(context, it)?.let { (name, phone) ->
                viewModel.addWhitelistedContact(name, phone)
            }
        }
    }

    // Launcher for Emergency Picker
    val emergencyPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri ->
        uri?.let {
            resolveContact(context, it)?.let { (name, phone) ->
                viewModel.addEmergencyContact(name, phone)
            }
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
                    text = "REPLY RULES & CONTACTS",
                    color = textColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Navigation Tabs (Whitelist vs Emergency)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isDark) com.example.ridesafeautoreply.theme.CarbonGray else cardBorder, RoundedCornerShape(8.dp))
                    .padding(4.dp)
            ) {
                TabButton(
                    text = "SMS Whitelist",
                    selected = activeTab == 0,
                    modifier = Modifier.weight(1f),
                    onClick = { activeTab = 0 }
                )
                TabButton(
                    text = "Emergency Track",
                    selected = activeTab == 1,
                    modifier = Modifier.weight(1f),
                    onClick = { activeTab = 1 }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (activeTab == 0) {
                // WHITELIST TAB PANEL
                Column(modifier = Modifier.weight(1f)) {
                    // Everyone vs Whitelist switch
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, cardBorder, RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = cardBg)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "REPLY ONLY TO WHITELIST",
                                    color = greenAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Switch(
                                    checked = replyToSelectedOnly,
                                    onCheckedChange = { viewModel.setSelectedContactsOnly(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = if (isDark) Color.Black else Color.White,
                                        checkedTrackColor = greenAccent,
                                        uncheckedThumbColor = textMuted,
                                        uncheckedTrackColor = cardBorderHighlight
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (replyToSelectedOnly) {
                                    "RideSafe will only reply to phone numbers defined in the list below."
                                } else {
                                    "RideSafe will automatically send replies to all incoming calls when riding."
                                },
                                color = textMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "WHITELISTED CONTACTS (${whitelistedContacts.size})",
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        
                        Button(
                            onClick = { whitelistPickerLauncher.launch(null) },
                            colors = ButtonDefaults.buttonColors(containerColor = cardBg, contentColor = greenAccent),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.border(1.dp, cardBorder, RoundedCornerShape(8.dp)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Contact", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Contact", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (whitelistedContacts.isEmpty()) {
                        EmptyListState("No whitelisted contacts added.", Icons.Default.ContactPhone)
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(whitelistedContacts) { contact ->
                                ContactItemCard(
                                    contact = contact,
                                    onDelete = { viewModel.removeWhitelistedContact(contact) }
                                )
                            }
                        }
                    }
                }
            } else {
                // EMERGENCY TRACK PANEL
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "FAMILY EMERGENCY CONTACTS (${emergencyContacts.size})",
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Configure contacts who will receive your live Google Maps location coordinates every 30 minutes when emergency tracking starts.",
                        color = textMuted,
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { emergencyPickerLauncher.launch(null) },
                            colors = ButtonDefaults.buttonColors(containerColor = cardBg, contentColor = greenAccent),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.border(1.dp, cardBorder, RoundedCornerShape(8.dp)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Family", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Family Contact", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (emergencyContacts.isEmpty()) {
                        EmptyListState("No emergency tracking contacts added.", Icons.Default.FamilyRestroom)
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(emergencyContacts) { contact ->
                                ContactItemCard(
                                    contact = contact,
                                    onDelete = { viewModel.removeEmergencyContact(contact) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TabButton(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == com.example.ridesafeautoreply.theme.DeepBlack
    val greenAccent = if (isDark) com.example.ridesafeautoreply.theme.NeonGreen else androidx.compose.ui.graphics.Color(0xFF00A82D)
    val textMuted = if (isDark) com.example.ridesafeautoreply.theme.TextGray else androidx.compose.ui.graphics.Color(0xFF8E8E93)

    Box(
        modifier = modifier
            .background(
                color = if (selected) greenAccent else Color.Transparent,
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) (if (isDark) Color.Black else Color.White) else textMuted,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}

@Composable
fun ContactItemCard(
    contact: Contact,
    onDelete: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == com.example.ridesafeautoreply.theme.DeepBlack
    val cardBg = if (isDark) com.example.ridesafeautoreply.theme.CarbonLight else androidx.compose.ui.graphics.Color.White
    val cardBorder = if (isDark) com.example.ridesafeautoreply.theme.DividerGray else androidx.compose.ui.graphics.Color(0xFFE5E5EA)
    val textColor = if (isDark) com.example.ridesafeautoreply.theme.PureWhite else androidx.compose.ui.graphics.Color(0xFF1C1C1E)
    val textMuted = if (isDark) com.example.ridesafeautoreply.theme.TextGray else androidx.compose.ui.graphics.Color(0xFF8E8E93)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, cardBorder, RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = contact.name,
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = contact.phoneNumber,
                    color = textMuted,
                    fontSize = 12.sp
                )
            }
            
            IconButton(
                onClick = onDelete,
                colors = IconButtonDefaults.iconButtonColors(contentColor = AlertRed.copy(alpha = 0.8f))
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyListState(
    text: String,
    icon: ImageVector
) {
    val isDark = MaterialTheme.colorScheme.background == com.example.ridesafeautoreply.theme.DeepBlack
    val cardBorder = if (isDark) com.example.ridesafeautoreply.theme.DividerGray else androidx.compose.ui.graphics.Color(0xFFE5E5EA)
    val textMuted = if (isDark) com.example.ridesafeautoreply.theme.TextGray else androidx.compose.ui.graphics.Color(0xFF8E8E93)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = cardBorder,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                color = textMuted,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Queries Android database using native ContentResolver
private fun resolveContact(context: Context, uri: Uri): Pair<String, String>? {
    val contentResolver = context.contentResolver
    var name = "Unknown"
    var phoneNumber = ""

    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
            if (nameIndex != -1) {
                name = cursor.getString(nameIndex)
            }

            val idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID)
            if (idIndex != -1) {
                val contactId = cursor.getString(idIndex)
                
                contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                    arrayOf(contactId),
                    null
                )?.use { phoneCursor ->
                    if (phoneCursor.moveToFirst()) {
                        val phoneIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        if (phoneIndex != -1) {
                            phoneNumber = phoneCursor.getString(phoneIndex)
                        }
                    }
                }
            }
        }
    }
    return if (phoneNumber.isNotEmpty()) Pair(name, phoneNumber) else null
}
