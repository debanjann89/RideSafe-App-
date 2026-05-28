package com.example.ridesafeautoreply.ui.common

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.ridesafeautoreply.theme.*

data class PermissionConfig(
    val permission: String,
    val label: String,
    val description: String,
    val icon: ImageVector,
    val apiMin: Int = 0
)

val APP_PERMISSIONS = listOf(
    PermissionConfig(
        permission = Manifest.permission.ACCESS_FINE_LOCATION,
        label = "GPS Location",
        description = "Required to accurately track your bike's riding speed in real-time.",
        icon = Icons.Default.MyLocation
    ),
    PermissionConfig(
        permission = Manifest.permission.SEND_SMS,
        label = "Send SMS",
        description = "Allows RideSafe to send automatic SMS replies to missed callers.",
        icon = Icons.Default.Sms
    ),
    PermissionConfig(
        permission = Manifest.permission.READ_PHONE_STATE,
        label = "Call Interception",
        description = "Required to detect incoming phone calls while you are riding.",
        icon = Icons.Default.Call
    ),
    PermissionConfig(
        permission = Manifest.permission.READ_CALL_LOG,
        label = "Call Logs (Caller Info)",
        description = "Required on Android 9+ to read the phone numbers of incoming callers.",
        icon = Icons.Default.PhoneCallback
    ),
    PermissionConfig(
        permission = Manifest.permission.READ_CONTACTS,
        label = "Access Contacts",
        description = "Required to identify whitelisted contacts and emergency contacts.",
        icon = Icons.Default.Contacts
    ),
    PermissionConfig(
        permission = Manifest.permission.ANSWER_PHONE_CALLS,
        label = "Manage Calls (Auto-Reject)",
        description = "Allows RideSafe to automatically silence or end the incoming call once an SMS reply is sent.",
        icon = Icons.Default.CallEnd
    ),
    PermissionConfig(
        permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.POST_NOTIFICATIONS else "",
        label = "Notifications",
        description = "Required to show the persistent 'Protection Active' notification in status bar.",
        icon = Icons.Default.Notifications,
        apiMin = Build.VERSION_CODES.TIRAMISU
    )
).filter { it.permission.isNotEmpty() }

fun hasAllPermissions(context: Context): Boolean {
    return APP_PERMISSIONS.all { config ->
        if (Build.VERSION.SDK_INT >= config.apiMin) {
            ContextCompat.checkSelfPermission(context, config.permission) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}

@Composable
fun PermissionHandler(
    onDismiss: () -> Unit,
    onPermissionsGranted: () -> Unit
) {
    val context = LocalContext.current
    var permissionsState by remember {
        mutableStateOf(APP_PERMISSIONS.associate { config ->
            val granted = ContextCompat.checkSelfPermission(context, config.permission) == PackageManager.PERMISSION_GRANTED
            config.permission to granted
        })
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val updatedMap = permissionsState.toMutableMap()
        results.forEach { (permission, granted) ->
            updatedMap[permission] = granted
        }
        permissionsState = updatedMap
        
        if (updatedMap.values.all { it }) {
            onPermissionsGranted()
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(1.dp, NeonGreen.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = CarbonGray),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Shield Icon",
                    tint = NeonGreen,
                    modifier = Modifier.size(48.dp)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Permissions Required",
                    color = PureWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "To guarantee auto-reply safety, please grant the following permissions:",
                    color = TextGray,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(APP_PERMISSIONS) { config ->
                        val isGranted = permissionsState[config.permission] ?: false
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CarbonLight, RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = config.icon,
                                contentDescription = config.label,
                                tint = if (isGranted) NeonGreen else TextGray,
                                modifier = Modifier.size(24.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = config.label,
                                    color = PureWhite,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = config.description,
                                    color = TextGray,
                                    fontSize = 11.sp
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            if (isGranted) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Granted",
                                    tint = NeonGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Cancel,
                                    contentDescription = "Denied",
                                    tint = AlertRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Link button to open system settings if permissions are blocked
                TextButton(
                    onClick = {
                        try {
                            val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = android.net.Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // ignore fallback
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = NeonGreenGlow)
                ) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("System Settings: Grant Permissions Manually", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.textButtonColors(contentColor = TextGray)
                    ) {
                        Text("Later", fontWeight = FontWeight.SemiBold)
                    }
                    
                    Button(
                        onClick = {
                            val ungranted = APP_PERMISSIONS
                                .filter { (permissionsState[it.permission] ?: false) == false }
                                .map { it.permission }
                                .toTypedArray()
                            
                            if (ungranted.isNotEmpty()) {
                                launcher.launch(ungranted)
                            } else {
                                onPermissionsGranted()
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1.5f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonGreen,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Grant Permissions", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
