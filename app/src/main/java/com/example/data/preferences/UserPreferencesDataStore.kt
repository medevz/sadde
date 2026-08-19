package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.model.AiProviderType
import com.example.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "shreya_settings")

class UserPreferencesDataStore(private val context: Context) {

    private object PreferencesKeys {
        val ASSISTANT_NAME = stringPreferencesKey("assistant_name")
        val LANGUAGE = stringPreferencesKey("language")
        val SPEECH_RATE = floatPreferencesKey("speech_rate")
        val SPEECH_PITCH = floatPreferencesKey("speech_pitch")
        val AI_PROVIDER = stringPreferencesKey("ai_provider")
        val CUSTOM_API_KEY = stringPreferencesKey("custom_api_key")
        val CUSTOM_API_BASE_URL = stringPreferencesKey("custom_api_base_url")
        val CUSTOM_MODEL_NAME = stringPreferencesKey("custom_model_name")
        val WAKE_WORD_ENABLED = booleanPreferencesKey("wake_word_enabled")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val NOTIFICATION_ACCESS_ENABLED = booleanPreferencesKey("notification_access_enabled")
        val VIBRATION_FEEDBACK = booleanPreferencesKey("vibration_feedback")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        val providerStr = preferences[PreferencesKeys.AI_PROVIDER] ?: AiProviderType.GEMINI.name
        val provider = try {
            AiProviderType.valueOf(providerStr)
        } catch (e: Exception) {
            AiProviderType.GEMINI
        }

        AppSettings(
            assistantName = preferences[PreferencesKeys.ASSISTANT_NAME] ?: "Shreya",
            language = preferences[PreferencesKeys.LANGUAGE] ?: "hi",
            speechRate = preferences[PreferencesKeys.SPEECH_RATE] ?: 1.0f,
            speechPitch = preferences[PreferencesKeys.SPEECH_PITCH] ?: 1.0f,
            aiProvider = provider,
            customApiKey = preferences[PreferencesKeys.CUSTOM_API_KEY] ?: "",
            customApiBaseUrl = preferences[PreferencesKeys.CUSTOM_API_BASE_URL] ?: "",
            customModelName = preferences[PreferencesKeys.CUSTOM_MODEL_NAME] ?: "gemini-3.5-flash",
            wakeWordEnabled = preferences[PreferencesKeys.WAKE_WORD_ENABLED] ?: false,
            onboardingCompleted = preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false,
            notificationAccessEnabled = preferences[PreferencesKeys.NOTIFICATION_ACCESS_ENABLED] ?: false,
            vibrationFeedback = preferences[PreferencesKeys.VIBRATION_FEEDBACK] ?: true
        )
    }

    suspend fun updateSettings(settings: AppSettings) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ASSISTANT_NAME] = settings.assistantName
            preferences[PreferencesKeys.LANGUAGE] = settings.language
            preferences[PreferencesKeys.SPEECH_RATE] = settings.speechRate
            preferences[PreferencesKeys.SPEECH_PITCH] = settings.speechPitch
            preferences[PreferencesKeys.AI_PROVIDER] = settings.aiProvider.name
            preferences[PreferencesKeys.CUSTOM_API_KEY] = settings.customApiKey
            preferences[PreferencesKeys.CUSTOM_API_BASE_URL] = settings.customApiBaseUrl
            preferences[PreferencesKeys.CUSTOM_MODEL_NAME] = settings.customModelName
            preferences[PreferencesKeys.WAKE_WORD_ENABLED] = settings.wakeWordEnabled
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = settings.onboardingCompleted
            preferences[PreferencesKeys.NOTIFICATION_ACCESS_ENABLED] = settings.notificationAccessEnabled
            preferences[PreferencesKeys.VIBRATION_FEEDBACK] = settings.vibrationFeedback
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = completed
        }
    }
}
