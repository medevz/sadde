package com.example.ui.viewmodel

import android.app.Application
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.preferences.UserPreferencesDataStore
import com.example.data.repository.ChatRepository
import com.example.domain.actions.ActionEngine
import com.example.domain.actions.ActionParser
import com.example.domain.actions.ActionValidator
import com.example.domain.actions.PermissionManager
import com.example.domain.ai.AiProvider
import com.example.domain.ai.GeminiProvider
import com.example.domain.ai.MockAiProvider
import com.example.domain.ai.OfflineRuleEngine
import com.example.domain.ai.OpenAiCompatibleProvider
import com.example.domain.model.ActionExecutionStatus
import com.example.domain.model.AiProviderType
import com.example.domain.model.AppSettings
import com.example.domain.model.AssistantAction
import com.example.domain.model.AssistantState
import com.example.domain.model.ChatMessage
import com.example.domain.model.MessageRole
import com.example.service.SpeechService
import com.example.service.TtsService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AssistantViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "AssistantViewModel"
    }

    private val context = application.applicationContext
    private val database = AppDatabase.getInstance(context)
    val chatRepository = ChatRepository(database.chatDao())
    val preferencesDataStore = UserPreferencesDataStore(context)
    val permissionManager = PermissionManager(context)

    val speechService = SpeechService(context)
    val ttsService = TtsService(context)
    val actionValidator = ActionValidator()
    val actionEngine = ActionEngine(context, permissionManager, ttsService)
    val actionParser = ActionParser()
    val offlineRuleEngine = OfflineRuleEngine()

    private val vibrator = context.getSystemService(Application.VIBRATOR_SERVICE) as? Vibrator

    val settings: StateFlow<AppSettings> = preferencesDataStore.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = AppSettings()
        )

    val messages: StateFlow<List<ChatMessage>> = chatRepository.messagesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _assistantState = MutableStateFlow<AssistantState>(AssistantState.Idle)
    val assistantState: StateFlow<AssistantState> = _assistantState.asStateFlow()

    private val _liveTranscript = MutableStateFlow("")
    val liveTranscript: StateFlow<String> = _liveTranscript.asStateFlow()

    private val _suggestedPrompts = MutableStateFlow(
        listOf(
            "YouTube kholo",
            "Chrome open karo",
            "Settings kholo",
            "Volume badhao",
            "Music pause karo",
            "Back jao"
        )
    )
    val suggestedPrompts: StateFlow<List<String>> = _suggestedPrompts.asStateFlow()

    val rmsVolume: StateFlow<Float> = speechService.rmsVolume

    init {
        // Synchronize TTS settings whenever settings change
        viewModelScope.launch {
            settings.collect { currentSettings ->
                ttsService.setLanguage(currentSettings.language)
                ttsService.setSpeechRate(currentSettings.speechRate)
                ttsService.setPitch(currentSettings.speechPitch)
            }
        }

        // Track TTS speaking state
        viewModelScope.launch {
            ttsService.isSpeaking.collect { isSpeaking ->
                if (isSpeaking && _assistantState.value !is AssistantState.Listening) {
                    _assistantState.value = AssistantState.Speaking(ttsService.toString())
                } else if (!isSpeaking && _assistantState.value is AssistantState.Speaking) {
                    _assistantState.value = AssistantState.Idle
                }
            }
        }
    }

    private fun getProvider(type: AiProviderType): AiProvider {
        return when (type) {
            AiProviderType.GEMINI -> GeminiProvider(actionParser, offlineRuleEngine)
            AiProviderType.OPENAI_COMPATIBLE -> OpenAiCompatibleProvider(actionParser, offlineRuleEngine)
            AiProviderType.OFFLINE_RULE_ENGINE -> object : AiProvider {
                override suspend fun generateResponse(
                    userPrompt: String,
                    history: List<ChatMessage>,
                    settings: AppSettings
                ) = offlineRuleEngine.processCommand(userPrompt, settings.language)
            }
            AiProviderType.MOCK -> MockAiProvider(offlineRuleEngine)
        }
    }

    fun startListening() {
        if (!permissionManager.hasRecordAudioPermission()) {
            _assistantState.value = AssistantState.Error(
                if (settings.value.language.startsWith("hi")) {
                    "Microphone permission chahiye voice commands ke liye."
                } else {
                    "Microphone permission is required to listen."
                }
            )
            return
        }

        vibrate()
        ttsService.stop()
        _liveTranscript.value = ""
        _assistantState.value = AssistantState.Listening()

        val langCode = if (settings.value.language.startsWith("hi")) "hi-IN" else "en-US"
        speechService.startListening(
            languageCode = langCode,
            onResult = { recognizedText ->
                _liveTranscript.value = recognizedText
                sendMessage(recognizedText)
            },
            onError = { error ->
                _assistantState.value = AssistantState.Error(error)
            }
        )
    }

    fun stopListening() {
        speechService.stopListening()
        if (_assistantState.value is AssistantState.Listening) {
            _assistantState.value = AssistantState.Idle
        }
    }

    fun toggleListening() {
        if (_assistantState.value is AssistantState.Listening) {
            stopListening()
        } else {
            startListening()
        }
    }

    fun sendMessage(prompt: String) {
        val trimmed = prompt.trim()
        if (trimmed.isBlank()) return

        stopListening()
        ttsService.stop()
        vibrate()

        val currentSettings = settings.value
        _assistantState.value = AssistantState.Processing

        viewModelScope.launch {
            // 1. Insert User Message
            val userMsg = ChatMessage(
                role = MessageRole.USER,
                text = trimmed,
                timestamp = System.currentTimeMillis()
            )
            chatRepository.insertMessage(userMsg)

            // 2. Fetch AI / Engine Response
            val provider = getProvider(currentSettings.aiProvider)
            val history = messages.value.takeLast(6)

            val assistantResponse = try {
                provider.generateResponse(trimmed, history, currentSettings)
            } catch (e: Exception) {
                Log.e(TAG, "Error generating response", e)
                offlineRuleEngine.processCommand(trimmed, currentSettings.language)
            }

            // 3. Execute Validated Actions
            val executedActions = mutableListOf<AssistantAction>()
            for (action in assistantResponse.actions) {
                val validation = actionValidator.validate(action)
                if (validation is ActionValidator.ValidationResult.Valid) {
                    val resultAction = actionEngine.executeAction(action, currentSettings.language)
                    executedActions.add(resultAction)
                } else if (validation is ActionValidator.ValidationResult.Invalid) {
                    executedActions.add(
                        action.copy(
                            status = ActionExecutionStatus.FAILED,
                            executionMessage = validation.reason
                        )
                    )
                }
            }

            // 4. Insert Assistant Message
            val assistantMsg = ChatMessage(
                role = MessageRole.ASSISTANT,
                text = assistantResponse.reply,
                timestamp = System.currentTimeMillis(),
                actions = executedActions,
                isOffline = currentSettings.aiProvider == AiProviderType.OFFLINE_RULE_ENGINE
            )
            chatRepository.insertMessage(assistantMsg)

            // 5. Update Suggested Follow-ups
            if (assistantResponse.suggestedFollowUps.isNotEmpty()) {
                _suggestedPrompts.value = assistantResponse.suggestedFollowUps
            }

            // 6. Speak Response via TTS
            _assistantState.value = AssistantState.Speaking(assistantResponse.reply)
            ttsService.speak(assistantResponse.reply)
        }
    }

    fun stopSpeaking() {
        ttsService.stop()
        if (_assistantState.value is AssistantState.Speaking) {
            _assistantState.value = AssistantState.Idle
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            chatRepository.clearHistory()
        }
    }

    fun deleteMessage(id: Long) {
        viewModelScope.launch {
            chatRepository.deleteMessage(id)
        }
    }

    fun executeSingleAction(action: AssistantAction) {
        viewModelScope.launch {
            actionEngine.executeAction(action, settings.value.language)
        }
    }

    private fun vibrate() {
        if (!settings.value.vibrationFeedback) return
        try {
            vibrator?.let {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    it.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    it.vibrate(35)
                }
            }
        } catch (e: Exception) {
            // Ignore vibration errors on unsupported devices
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechService.stopListening()
        ttsService.shutdown()
    }
}
