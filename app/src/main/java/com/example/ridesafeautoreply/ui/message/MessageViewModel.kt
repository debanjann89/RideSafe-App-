package com.example.ridesafeautoreply.ui.message

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ridesafeautoreply.data.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MessageViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {

    val autoReplyMessage: StateFlow<String> = settingsRepository.autoReplyMessage
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    fun updateMessage(message: String) {
        viewModelScope.launch {
            settingsRepository.setAutoReplyMessage(message)
        }
    }
}
