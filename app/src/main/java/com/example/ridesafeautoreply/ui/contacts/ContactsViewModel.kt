package com.example.ridesafeautoreply.ui.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ridesafeautoreply.data.Contact
import com.example.ridesafeautoreply.data.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ContactsViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {

    val isSelectedContactsOnly: StateFlow<Boolean> = settingsRepository.isSelectedContactsOnly
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val whitelistedContacts: StateFlow<List<Contact>> = settingsRepository.whitelistedContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val emergencyContacts: StateFlow<List<Contact>> = settingsRepository.emergencyContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSelectedContactsOnly(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setSelectedContactsOnly(enabled)
        }
    }

    fun addWhitelistedContact(name: String, phone: String) {
        viewModelScope.launch {
            val normalizedPhone = phone.replace(" ", "").replace("-", "")
            val current = whitelistedContacts.value.toMutableList()
            if (current.none { it.phoneNumber == normalizedPhone }) {
                current.add(Contact(name, normalizedPhone))
                settingsRepository.setWhitelistedContacts(current)
            }
        }
    }

    fun removeWhitelistedContact(contact: Contact) {
        viewModelScope.launch {
            val current = whitelistedContacts.value.toMutableList()
            current.remove(contact)
            settingsRepository.setWhitelistedContacts(current)
        }
    }

    fun addEmergencyContact(name: String, phone: String) {
        viewModelScope.launch {
            val normalizedPhone = phone.replace(" ", "").replace("-", "")
            val current = emergencyContacts.value.toMutableList()
            if (current.none { it.phoneNumber == normalizedPhone }) {
                current.add(Contact(name, normalizedPhone))
                settingsRepository.setEmergencyContacts(current)
            }
        }
    }

    fun removeEmergencyContact(contact: Contact) {
        viewModelScope.launch {
            val current = emergencyContacts.value.toMutableList()
            current.remove(contact)
            settingsRepository.setEmergencyContacts(current)
        }
    }
}
