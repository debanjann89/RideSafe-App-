package com.example.ridesafeautoreply.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.ridesafeautoreply.data.SettingsRepository
import com.example.ridesafeautoreply.service.RideSafeService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(TAG, "onReceive: Received broadcast: $action")

        if (action == Intent.ACTION_BOOT_COMPLETED || action == "android.intent.action.QUICKBOOT_POWERON") {
            val settingsRepository = SettingsRepository(context.applicationContext)
            
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val isProtectionActive = settingsRepository.isProtectionActive.first()
                    Log.d(TAG, "onReceive: Is protection active: $isProtectionActive")

                    if (isProtectionActive) {
                        val startIntent = Intent(context, RideSafeService::class.java)
                        
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context.startForegroundService(startIntent)
                        } else {
                            context.startService(startIntent)
                        }
                        Log.d(TAG, "onReceive: RideSafeService automatically started after system boot.")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "onReceive: Failed to launch service on boot: ${e.message}")
                }
            }
        }
    }
}
