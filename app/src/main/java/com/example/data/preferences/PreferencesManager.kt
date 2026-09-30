package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "omnidrop_settings")

class PreferencesManager(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DEVICE_NAME = stringPreferencesKey("device_name")
        val AUTO_ACCEPT_TRUSTED = booleanPreferencesKey("auto_accept_trusted")
        val E2EE_STRICT = booleanPreferencesKey("e2ee_strict")
        val HIGH_SPEED_DIRECT = booleanPreferencesKey("high_speed_direct")
        val WEB_PORTAL_PIN_ENABLED = booleanPreferencesKey("web_portal_pin_enabled")
        val WEB_PORTAL_PIN = stringPreferencesKey("web_portal_pin")
        val CLOUD_AUTO_SYNC = booleanPreferencesKey("cloud_auto_sync")
        val HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val themeModeString = preferences[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.name
            val themeMode = try {
                ThemeMode.valueOf(themeModeString)
            } catch (e: Exception) {
                ThemeMode.SYSTEM
            }

            UserPreferences(
                themeMode = themeMode,
                deviceName = preferences[Keys.DEVICE_NAME] ?: "OmniDrop Device",
                autoAcceptTrusted = preferences[Keys.AUTO_ACCEPT_TRUSTED] ?: true,
                e2eeStrictVerification = preferences[Keys.E2EE_STRICT] ?: true,
                highSpeedDirect = preferences[Keys.HIGH_SPEED_DIRECT] ?: true,
                webPortalPinEnabled = preferences[Keys.WEB_PORTAL_PIN_ENABLED] ?: false,
                webPortalPin = preferences[Keys.WEB_PORTAL_PIN] ?: "1234",
                cloudAutoSync = preferences[Keys.CLOUD_AUTO_SYNC] ?: true,
                hapticFeedback = preferences[Keys.HAPTIC_FEEDBACK] ?: true
            )
        }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[Keys.THEME_MODE] = mode.name
        }
    }

    suspend fun setDeviceName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.DEVICE_NAME] = name
        }
    }

    suspend fun setAutoAcceptTrusted(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.AUTO_ACCEPT_TRUSTED] = enabled
        }
    }

    suspend fun setE2eeStrictVerification(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.E2EE_STRICT] = enabled
        }
    }

    suspend fun setHighSpeedDirect(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.HIGH_SPEED_DIRECT] = enabled
        }
    }

    suspend fun setWebPortalPinEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.WEB_PORTAL_PIN_ENABLED] = enabled
        }
    }

    suspend fun setWebPortalPin(pin: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.WEB_PORTAL_PIN] = pin
        }
    }

    suspend fun setCloudAutoSync(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.CLOUD_AUTO_SYNC] = enabled
        }
    }

    suspend fun setHapticFeedback(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.HAPTIC_FEEDBACK] = enabled
        }
    }
}
