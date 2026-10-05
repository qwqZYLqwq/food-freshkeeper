package com.food.freshkeeper.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.food.freshkeeper.util.ImageSaver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "fresh_settings")

class SettingsRepository(private val context: Context) {

    companion object {
        val KEY_SERVER_URL = stringPreferencesKey("server_url")
        val KEY_AUTO_SYNC_ENABLED = booleanPreferencesKey("auto_sync_enabled")
        val KEY_AUTO_SYNC_UPLOAD = booleanPreferencesKey("auto_sync_upload")
        val KEY_AUTO_SYNC_DOWNLOAD = booleanPreferencesKey("auto_sync_download")
        val KEY_LAST_SYNC_TIME_MS = longPreferencesKey("last_sync_time_ms")
        val KEY_DEFAULT_REMINDER_DAYS = intPreferencesKey("default_reminder_days")
        val KEY_NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")
        val KEY_IMAGE_SAVE_PATH = stringPreferencesKey("image_save_path")
        const val DEFAULT_IMAGE_SAVE_PATH = "Download/food"
    }

    val imageSavePathFlow: Flow<String> = context.settingsDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_IMAGE_SAVE_PATH]?.takeIf { it.isNotBlank() } ?: DEFAULT_IMAGE_SAVE_PATH
        }

    val imageSavePath: Flow<String> get() = imageSavePathFlow

    val serverUrl: Flow<String> = context.settingsDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_SERVER_URL] ?: ""
        }

    val autoSyncEnabled: Flow<Boolean> = context.settingsDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_AUTO_SYNC_ENABLED] ?: false
        }

    val autoSyncUpload: Flow<Boolean> = context.settingsDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_AUTO_SYNC_UPLOAD] ?: true
        }

    val autoSyncDownload: Flow<Boolean> = context.settingsDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_AUTO_SYNC_DOWNLOAD] ?: true
        }

    val lastSyncTimeMs: Flow<Long> = context.settingsDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_LAST_SYNC_TIME_MS] ?: 0L
        }

    val defaultReminderDays: Flow<Int> = context.settingsDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_DEFAULT_REMINDER_DAYS] ?: 3
        }

    val notificationEnabled: Flow<Boolean> = context.settingsDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_NOTIFICATION_ENABLED] ?: true
        }

    suspend fun setServerUrl(url: String) {
        val trimmed = url.trim().removeSuffix("/")
        val normalized = when {
            trimmed.isEmpty() -> ""
            trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true) -> trimmed
            else -> "http://$trimmed"
        }
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_SERVER_URL] = normalized
        }
    }

    suspend fun saveServerUrl(url: String) = setServerUrl(url)

    suspend fun setAutoSyncEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_AUTO_SYNC_ENABLED] = enabled
        }
    }

    suspend fun setAutoSyncOptions(upload: Boolean, download: Boolean) {
        val finalUpload = if (!upload && !download) true else upload
        val finalDownload = if (!upload && !download) false else download
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_AUTO_SYNC_UPLOAD] = finalUpload
            preferences[KEY_AUTO_SYNC_DOWNLOAD] = finalDownload
        }
    }

    suspend fun setLastSyncTime(timeMs: Long = System.currentTimeMillis()) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_LAST_SYNC_TIME_MS] = timeMs
        }
    }

    suspend fun recordSyncSuccess(timestampMs: Long = System.currentTimeMillis()) = setLastSyncTime(timestampMs)

    suspend fun setDefaultReminderDays(days: Int) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_DEFAULT_REMINDER_DAYS] = days
        }
    }

    suspend fun saveReminderDays(days: Int) = setDefaultReminderDays(days)

    suspend fun setNotificationEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_NOTIFICATION_ENABLED] = enabled
        }
    }

    suspend fun setImageSavePath(path: String) {
        val sanitized = ImageSaver.sanitizeImageSavePath(path)
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_IMAGE_SAVE_PATH] = sanitized
        }
    }

    suspend fun resetImageSavePath() {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_IMAGE_SAVE_PATH] = DEFAULT_IMAGE_SAVE_PATH
        }
    }
}
