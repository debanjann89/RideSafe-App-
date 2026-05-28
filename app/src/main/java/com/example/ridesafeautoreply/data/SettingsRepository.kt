package com.example.ridesafeautoreply.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ridesafe_settings")

class SettingsRepository(private val context: Context) {

    companion object {
        private val KEY_SPEED_THRESHOLD = floatPreferencesKey("speed_threshold")
        private val KEY_AUTO_REPLY_MESSAGE = stringPreferencesKey("auto_reply_message")
        private val KEY_PROTECTION_ACTIVE = booleanPreferencesKey("protection_active")
        private val KEY_SELECTED_CONTACTS_ONLY = booleanPreferencesKey("selected_contacts_only")
        private val KEY_WHITELISTED_CONTACTS = stringPreferencesKey("whitelisted_contacts")
        private val KEY_EMERGENCY_TRACKING_ENABLED = booleanPreferencesKey("emergency_tracking_enabled")
        private val KEY_EMERGENCY_CONTACTS = stringPreferencesKey("emergency_contacts")
        private val KEY_AI_SMART_MODE = booleanPreferencesKey("ai_smart_mode")
        private val KEY_TEST_MODE_ENABLED = booleanPreferencesKey("test_mode_enabled")
        private val KEY_DARK_THEME_ENABLED = booleanPreferencesKey("dark_theme_enabled")
        
        // Ride Statistics keys
        private val KEY_TOTAL_DISTANCE = doublePreferencesKey("total_distance")
        private val KEY_TOTAL_RIDING_HOURS = doublePreferencesKey("total_riding_hours")
        private val KEY_RIDE_HISTORY = stringPreferencesKey("ride_history")
        
        private const val DEFAULT_MESSAGE = "I am currently riding my bike and cannot answer right now. I’ll call you back soon."
    }

    // Safe error handling for DataStore reading
    private val preferencesFlow: Flow<Preferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }

    // Speeds are in km/h
    val speedThreshold: Flow<Float> = preferencesFlow.map { preferences ->
        preferences[KEY_SPEED_THRESHOLD] ?: 15f
    }

    val autoReplyMessage: Flow<String> = preferencesFlow.map { preferences ->
        preferences[KEY_AUTO_REPLY_MESSAGE] ?: DEFAULT_MESSAGE
    }

    val isProtectionActive: Flow<Boolean> = preferencesFlow.map { preferences ->
        preferences[KEY_PROTECTION_ACTIVE] ?: false
    }

    val isSelectedContactsOnly: Flow<Boolean> = preferencesFlow.map { preferences ->
        preferences[KEY_SELECTED_CONTACTS_ONLY] ?: false
    }

    val whitelistedContacts: Flow<List<Contact>> = preferencesFlow.map { preferences ->
        val jsonStr = preferences[KEY_WHITELISTED_CONTACTS] ?: "[]"
        try {
            Json.decodeFromString<List<Contact>>(jsonStr)
        } catch (e: Exception) {
            emptyList()
        }
    }

    val isEmergencyTrackingEnabled: Flow<Boolean> = preferencesFlow.map { preferences ->
        preferences[KEY_EMERGENCY_TRACKING_ENABLED] ?: false
    }

    val emergencyContacts: Flow<List<Contact>> = preferencesFlow.map { preferences ->
        val jsonStr = preferences[KEY_EMERGENCY_CONTACTS] ?: "[]"
        try {
            Json.decodeFromString<List<Contact>>(jsonStr)
        } catch (e: Exception) {
            emptyList()
        }
    }

    val isAiSmartModeEnabled: Flow<Boolean> = preferencesFlow.map { preferences ->
        preferences[KEY_AI_SMART_MODE] ?: false
    }

    val isTestModeEnabled: Flow<Boolean> = preferencesFlow.map { preferences ->
        preferences[KEY_TEST_MODE_ENABLED] ?: false
    }

    val isDarkThemeEnabled: Flow<Boolean> = preferencesFlow.map { preferences ->
        preferences[KEY_DARK_THEME_ENABLED] ?: true
    }

    // Cumulative stats
    val totalDistance: Flow<Double> = preferencesFlow.map { preferences ->
        preferences[KEY_TOTAL_DISTANCE] ?: 0.0
    }

    val totalRidingHours: Flow<Double> = preferencesFlow.map { preferences ->
        preferences[KEY_TOTAL_RIDING_HOURS] ?: 0.0
    }

    val rideHistory: Flow<List<RideSession>> = preferencesFlow.map { preferences ->
        val jsonStr = preferences[KEY_RIDE_HISTORY] ?: "[]"
        try {
            Json.decodeFromString<List<RideSession>>(jsonStr)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Setters
    suspend fun setSpeedThreshold(threshold: Float) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SPEED_THRESHOLD] = threshold
        }
    }

    suspend fun setAutoReplyMessage(message: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_AUTO_REPLY_MESSAGE] = message
        }
    }

    suspend fun setProtectionActive(active: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_PROTECTION_ACTIVE] = active
        }
    }

    suspend fun setSelectedContactsOnly(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SELECTED_CONTACTS_ONLY] = enabled
        }
    }

    suspend fun setWhitelistedContacts(contacts: List<Contact>) {
        val jsonStr = Json.encodeToString(contacts)
        context.dataStore.edit { preferences ->
            preferences[KEY_WHITELISTED_CONTACTS] = jsonStr
        }
    }

    suspend fun setEmergencyTrackingEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_EMERGENCY_TRACKING_ENABLED] = enabled
        }
    }

    suspend fun setEmergencyContacts(contacts: List<Contact>) {
        val jsonStr = Json.encodeToString(contacts)
        context.dataStore.edit { preferences ->
            preferences[KEY_EMERGENCY_CONTACTS] = jsonStr
        }
    }

    suspend fun setAiSmartModeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_AI_SMART_MODE] = enabled
        }
    }

    suspend fun setTestModeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_TEST_MODE_ENABLED] = enabled
        }
    }

    suspend fun setDarkThemeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DARK_THEME_ENABLED] = enabled
        }
    }

    suspend fun addRideStats(distanceDeltaKm: Double, timeDeltaHours: Double) {
        context.dataStore.edit { preferences ->
            val currentDistance = preferences[KEY_TOTAL_DISTANCE] ?: 0.0
            val currentHours = preferences[KEY_TOTAL_RIDING_HOURS] ?: 0.0
            preferences[KEY_TOTAL_DISTANCE] = currentDistance + distanceDeltaKm
            preferences[KEY_TOTAL_RIDING_HOURS] = currentHours + timeDeltaHours
        }
    }

    suspend fun resetRideStats() {
        context.dataStore.edit { preferences ->
            preferences[KEY_TOTAL_DISTANCE] = 0.0
            preferences[KEY_TOTAL_RIDING_HOURS] = 0.0
        }
    }

    suspend fun saveRideSession(session: RideSession) {
        context.dataStore.edit { preferences ->
            val jsonStr = preferences[KEY_RIDE_HISTORY] ?: "[]"
            val currentList = try {
                Json.decodeFromString<List<RideSession>>(jsonStr).toMutableList()
            } catch (e: Exception) {
                mutableListOf()
            }
            currentList.add(0, session) // Add to top of list (newest first)
            preferences[KEY_RIDE_HISTORY] = Json.encodeToString(currentList)
        }
    }

    suspend fun clearRideHistory() {
        context.dataStore.edit { preferences ->
            preferences[KEY_RIDE_HISTORY] = "[]"
        }
    }
}
