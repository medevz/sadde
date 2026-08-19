package com.example.domain.model

enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM
}

data class ChatMessage(
    val id: Long = 0,
    val role: MessageRole,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actions: List<AssistantAction> = emptyList(),
    val isError: Boolean = false,
    val isOffline: Boolean = false
)

data class AssistantResponse(
    val reply: String,
    val actions: List<AssistantAction> = emptyList(),
    val suggestedFollowUps: List<String> = emptyList()
)

enum class AiProviderType(val displayName: String) {
    GEMINI("Gemini 3.5 Flash (Cloud)"),
    OPENAI_COMPATIBLE("Custom API (OpenAI/Compatible)"),
    OFFLINE_RULE_ENGINE("Offline Fast Engine (Local)"),
    MOCK("Mock / Testing Engine")
}

data class AppSettings(
    val assistantName: String = "Shreya",
    val language: String = "hi", // "hi" or "en"
    val speechRate: Float = 1.0f,
    val speechPitch: Float = 1.0f,
    val aiProvider: AiProviderType = AiProviderType.GEMINI,
    val customApiKey: String = "",
    val customApiBaseUrl: String = "",
    val customModelName: String = "gemini-3.5-flash",
    val wakeWordEnabled: Boolean = false,
    val onboardingCompleted: Boolean = false,
    val notificationAccessEnabled: Boolean = false,
    val vibrationFeedback: Boolean = true
)

sealed interface AssistantState {
    object Idle : AssistantState
    data class Listening(val volumeLevel: Float = 0f, val partialText: String = "") : AssistantState
    object Processing : AssistantState
    data class Speaking(val text: String) : AssistantState
    data class Error(val message: String, val canRetry: Boolean = true) : AssistantState
}
