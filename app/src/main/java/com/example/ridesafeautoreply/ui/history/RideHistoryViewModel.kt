package com.example.ridesafeautoreply.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ridesafeautoreply.data.RideSession
import com.example.ridesafeautoreply.data.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RideHistoryViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {

    val rideHistory: StateFlow<List<RideSession>> = settingsRepository.rideHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun clearHistory() {
        viewModelScope.launch {
            settingsRepository.clearRideHistory()
        }
    }
}
