package com.example.domain.ai

import android.util.Log
import com.example.BuildConfig
import com.example.domain.actions.ActionParser
import com.example.domain.model.ActionType
import com.example.domain.model.AssistantAction
import com.example.domain.model.AssistantResponse
import com.example.domain.model.ChatMessage
import com.example.domain.model.MessageRole
import com.example.domain.model.AppSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

interface AiProvider {
    suspend fun generateResponse(
        userPrompt: String,
        history: List<ChatMessage>,
        settings: AppSettings
    ): AssistantResponse
}

class GeminiProvider(
    private val actionParser: ActionParser,
    private val offlineRuleEngine: OfflineRuleEngine
) : AiProvider {

    companion object {
        private const val TAG = "GeminiProvider"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    override suspend fun generateResponse(
        userPrompt: String,
        history: List<ChatMessage>,
        settings: AppSettings
    ): AssistantResponse = withContext(Dispatchers.IO) {
        val apiKey = when {
            settings.customApiKey.isNotBlank() -> settings.customApiKey.trim()
            BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY" -> BuildConfig.GEMINI_API_KEY
            else -> ""
        }

        if (apiKey.isBlank()) {
            Log.w(TAG, "No API key configured. Utilizing local offline rule engine.")
            return@withContext offlineRuleEngine.processCommand(userPrompt, settings.language)
        }

        val model = if (settings.customModelName.isNotBlank()) settings.customModelName else "gemini-3.5-flash"
        val endpoint = "$BASE_URL$model:generateContent?key=$apiKey"

        try {
            val systemPrompt = """
                You are "Shreya", a helpful, polite, and ultra-fast personal AI Android voice & text assistant.
                You understand both Hindi (in Devanagari & Hinglish) and English.
                Respond concisely and naturally in the user's spoken language.
                
                CRITICAL ACTION RULES:
                You MUST return a JSON object with this exact schema:
                {
                  "reply": "Friendly concise speech reply to the user (e.g. YouTube खोल रही हूँ.)",
                  "actions": [
                    {
                      "type": "OPEN_APP | LAUNCH_ACTIVITY | OPEN_SETTINGS | VOLUME_UP | VOLUME_DOWN | SET_VOLUME | PLAY_MEDIA | PAUSE_MEDIA | NEXT_MEDIA | PREVIOUS_MEDIA | SET_BRIGHTNESS | SHOW_NOTIFICATION | SPEAK | ACCESSIBILITY_CLICK | ACCESSIBILITY_SCROLL | ACCESSIBILITY_TYPE_TEXT | ACCESSIBILITY_BACK | ACCESSIBILITY_HOME | ACCESSIBILITY_RECENTS | SEARCH_WEB | MAKE_CALL | SEND_MESSAGE | TAKE_PHOTO",
                      "target": "package name or query (e.g. com.google.android.youtube, or com.android.chrome, or search query)",
                      "value": "volume percentage (0-100) or text to type or null",
                      "description": "Short explanation"
                    }
                  ],
                  "suggestedFollowUps": ["Suggested next query 1", "Suggested next query 2"]
                }
                
                Standard Package Mappings:
                - YouTube -> "com.google.android.youtube"
                - Chrome -> "com.android.chrome"
                - WhatsApp -> "com.whatsapp"
                - Maps -> "com.google.android.apps.maps"
                - Camera -> action type TAKE_PHOTO
                - Settings -> action type OPEN_SETTINGS
                
                For generic conversation where no device action is needed, return an empty actions array: "actions": []
            """.trimIndent()

            val contentsArray = JSONArray()

            // System instructions turn
            val systemContent = JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().put(JSONObject().put("text", "SYSTEM INSTRUCTIONS:\n$systemPrompt")))
            }
            contentsArray.put(systemContent)
            contentsArray.put(
                JSONObject().apply {
                    put("role", "model")
                    put("parts", JSONArray().put(JSONObject().put("text", "Understood. I will act as Shreya AI Assistant and always return the specified JSON format.")))
                }
            )

            // Conversation history (limit to last 4 turns for low latency)
            val recentHistory = history.takeLast(4)
            for (msg in recentHistory) {
                val role = if (msg.role == MessageRole.USER) "user" else "model"
                contentsArray.put(
                    JSONObject().apply {
                        put("role", role)
                        put("parts", JSONArray().put(JSONObject().put("text", msg.text)))
                    }
                )
            }

            // Current user prompt
            contentsArray.put(
                JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().put(JSONObject().put("text", userPrompt)))
                }
            )

            val requestJson = JSONObject().apply {
                put("contents", contentsArray)
                put(
                    "generationConfig",
                    JSONObject().apply {
                        put("temperature", 0.3)
                        put("maxOutputTokens", 500)
                        put("responseMimeType", "application/json")
                    }
                )
            }

            val body = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(endpoint)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseBodyString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "Gemini API error ${response.code}: $responseBodyString")
                // Fall back gracefully to offline rule engine
                val fallback = offlineRuleEngine.processCommand(userPrompt, settings.language)
                return@withContext fallback.copy(
                    reply = "${fallback.reply}\n(Note: Cloud API status ${response.code}, offline mode used)"
                )
            }

            val rootJson = JSONObject(responseBodyString)
            val candidates = rootJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text") ?: ""

            return@withContext actionParser.parseResponse(text)
        } catch (e: Exception) {
            Log.e(TAG, "Gemini request failed", e)
            val fallback = offlineRuleEngine.processCommand(userPrompt, settings.language)
            return@withContext fallback.copy(
                reply = "${fallback.reply}\n(Offline Mode Active)"
            )
        }
    }
}

class OpenAiCompatibleProvider(
    private val actionParser: ActionParser,
    private val offlineRuleEngine: OfflineRuleEngine
) : AiProvider {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    override suspend fun generateResponse(
        userPrompt: String,
        history: List<ChatMessage>,
        settings: AppSettings
    ): AssistantResponse = withContext(Dispatchers.IO) {
        val apiKey = settings.customApiKey.trim()
        val baseUrl = settings.customApiBaseUrl.trim().ifBlank { "https://api.openai.com/v1" }

        if (apiKey.isBlank()) {
            return@withContext offlineRuleEngine.processCommand(userPrompt, settings.language)
        }

        try {
            val url = if (baseUrl.endsWith("/chat/completions")) baseUrl else "$baseUrl/chat/completions"
            val messagesArray = JSONArray()

            messagesArray.put(
                JSONObject().apply {
                    put("role", "system")
                    put(
                        "content",
                        "You are Shreya, an Android AI Assistant. Return ONLY valid JSON with keys: reply, actions (list with type, target, value, description), suggestedFollowUps (list)."
                    )
                }
            )

            for (msg in history.takeLast(4)) {
                messagesArray.put(
                    JSONObject().apply {
                        put("role", if (msg.role == MessageRole.USER) "user" else "assistant")
                        put("content", msg.text)
                    }
                )
            }

            messagesArray.put(
                JSONObject().apply {
                    put("role", "user")
                    put("content", userPrompt)
                }
            )

            val reqObj = JSONObject().apply {
                put("model", if (settings.customModelName.isNotBlank()) settings.customModelName else "gpt-4o-mini")
                put("messages", messagesArray)
                put("temperature", 0.3)
            }

            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $apiKey")
                .post(reqObj.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val resStr = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext offlineRuleEngine.processCommand(userPrompt, settings.language)
            }

            val jsonRes = JSONObject(resStr)
            val content = jsonRes.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content")
            return@withContext actionParser.parseResponse(content)
        } catch (e: Exception) {
            return@withContext offlineRuleEngine.processCommand(userPrompt, settings.language)
        }
    }
}

class MockAiProvider(private val offlineRuleEngine: OfflineRuleEngine) : AiProvider {
    override suspend fun generateResponse(
        userPrompt: String,
        history: List<ChatMessage>,
        settings: AppSettings
    ): AssistantResponse {
        return offlineRuleEngine.processCommand(userPrompt, settings.language)
    }
}
