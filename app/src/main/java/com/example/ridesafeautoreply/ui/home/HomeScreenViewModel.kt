package com.example.ridesafeautoreply.ui.home

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ridesafeautoreply.data.SettingsRepository
import com.example.ridesafeautoreply.service.RideSafeService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeScreenViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {

    val isProtectionActive: StateFlow<Boolean> = settingsRepository.isProtectionActive
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val totalDistance: StateFlow<Double> = settingsRepository.totalDistance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalRidingHours: StateFlow<Double> = settingsRepository.totalRidingHours
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Real-time states bridged directly from the active service flows
    val currentSpeed: StateFlow<Float> = RideSafeService.currentSpeedFlow
    val isCurrentlyRiding: StateFlow<Boolean> = RideSafeService.isRidingFlow
    val isServiceRunning: StateFlow<Boolean> = RideSafeService.isServiceRunningFlow

    fun toggleProtection(context: Context) {
        viewModelScope.launch {
            val nextState = !isProtectionActive.value
            settingsRepository.setProtectionActive(nextState)
            
            val intent = Intent(context, RideSafeService::class.java)
            if (nextState) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } else {
                context.stopService(intent)
            }
        }
    }

    fun resetStats() {
        viewModelScope.launch {
            settingsRepository.resetRideStats()
        }
    }
}
