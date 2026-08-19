package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.preferences.UserPreferencesDataStore
import com.example.domain.actions.PermissionManager
import com.example.domain.model.AiProviderType
import com.example.domain.model.AppSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    val preferencesDataStore = UserPreferencesDataStore(context)
    val permissionManager = PermissionManager(context)

    val settings: StateFlow<AppSettings> = preferencesDataStore.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = AppSettings()
        )

    fun updateSettings(newSettings: AppSettings) {
        viewModelScope.launch {
            preferencesDataStore.updateSettings(newSettings)
        }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            preferencesDataStore.updateSettings(settings.value.copy(language = lang))
        }
    }

    fun setAiProvider(provider: AiProviderType) {
        viewModelScope.launch {
            preferencesDataStore.updateSettings(settings.value.copy(aiProvider = provider))
        }
    }

    fun setApiKey(key: String) {
        viewModelScope.launch {
            preferencesDataStore.updateSettings(settings.value.copy(customApiKey = key))
        }
    }

    fun setApiBaseUrl(url: String) {
        viewModelScope.launch {
            preferencesDataStore.updateSettings(settings.value.copy(customApiBaseUrl = url))
        }
    }

    fun setModelName(model: String) {
        viewModelScope.launch {
            preferencesDataStore.updateSettings(settings.value.copy(customModelName = model))
        }
    }

    fun setSpeechRate(rate: Float) {
        viewModelScope.launch {
            preferencesDataStore.updateSettings(settings.value.copy(speechRate = rate))
        }
    }

    fun setSpeechPitch(pitch: Float) {
        viewModelScope.launch {
            preferencesDataStore.updateSettings(settings.value.copy(speechPitch = pitch))
        }
    }

    fun setWakeWordEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesDataStore.updateSettings(settings.value.copy(wakeWordEnabled = enabled))
        }
    }

    fun setOnboardingCompleted(completed: Boolean) {
        viewModelScope.launch {
            preferencesDataStore.setOnboardingCompleted(completed)
        }
    }

    fun setVibrationFeedback(enabled: Boolean) {
        viewModelScope.launch {
            preferencesDataStore.updateSettings(settings.value.copy(vibrationFeedback = enabled))
        }
    }
}
