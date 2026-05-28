package com.example.ridesafeautoreply.ui.settings

import android.content.Context
import android.os.PowerManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ridesafeautoreply.data.SettingsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {

    val speedThreshold: StateFlow<Float> = settingsRepository.speedThreshold
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 15f)

    val isAiSmartModeEnabled: StateFlow<Boolean> = settingsRepository.isAiSmartModeEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isEmergencyTrackingEnabled: StateFlow<Boolean> = settingsRepository.isEmergencyTrackingEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isTestModeEnabled: StateFlow<Boolean> = settingsRepository.isTestModeEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isDarkThemeEnabled: StateFlow<Boolean> = settingsRepository.isDarkThemeEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun setSpeedThreshold(threshold: Float) {
        viewModelScope.launch {
            settingsRepository.setSpeedThreshold(threshold)
        }
    }

    fun setAiSmartModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAiSmartModeEnabled(enabled)
        }
    }

    fun setEmergencyTrackingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setEmergencyTrackingEnabled(enabled)
        }
    }

    fun setTestModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setTestModeEnabled(enabled)
        }
    }

    fun setDarkThemeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDarkThemeEnabled(enabled)
        }
    }

    fun isBatteryOptimizedExcluded(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return false
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }
}
