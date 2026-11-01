package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.data.api.GeminiApiService
import com.example.data.model.ChatMessage
import com.example.data.model.GeminiContent
import com.example.data.model.GeminiGenerationConfig
import com.example.data.model.GeminiPart
import com.example.data.model.GeminiRequest
import com.example.data.model.MessageStatus
import com.example.data.model.ToolType
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class ChatRepository(
    private val context: Context,
    private val apiService: GeminiApiService = GeminiApiService.create()
) {
    companion object {
        private const val TAG = "ChatRepository"
        private const val PREFS_NAME = "nova_prefs"
        private const val KEY_CUSTOM_API_KEY = "custom_gemini_api_key"
        private const val HISTORY_FILE_NAME = "nova_chat_history.json"
        
        // Models in preferred order for fast, high-quality responses
        val SUPPORTED_MODELS = listOf(
            "gemini-2.5-flash",
            "gemini-2.0-flash",
            "gemini-1.5-flash"
        )
    }

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val listType = Types.newParameterizedType(List::class.java, ChatMessage::class.java)
    private val messagesAdapter = moshi.adapter<List<ChatMessage>>(listType)

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getApiKey(): String {
        val customKey = prefs.getString(KEY_CUSTOM_API_KEY, "")?.trim().orEmpty()
        if (customKey.isNotBlank()) return customKey
        
        val buildConfigKey = BuildConfig.GEMINI_API_KEY.trim()
        return if (buildConfigKey.isNotBlank() && buildConfigKey != "MY_GEMINI_API_KEY") {
            buildConfigKey
        } else {
            ""
        }
    }

    fun setCustomApiKey(key: String) {
        prefs.edit().putString(KEY_CUSTOM_API_KEY, key.trim()).apply()
    }

    fun isApiKeyConfigured(): Boolean {
        return getApiKey().isNotBlank()
    }

    suspend fun loadMessages(): List<ChatMessage> = withContext(Dispatchers.IO) {
        try {
            val file = File(context.filesDir, HISTORY_FILE_NAME)
            if (file.exists()) {
                val json = file.readText()
                if (json.isNotBlank()) {
                    return@withContext messagesAdapter.fromJson(json) ?: emptyList()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load chat history", e)
        }
        emptyList()
    }

    suspend fun saveMessages(messages: List<ChatMessage>) = withContext(Dispatchers.IO) {
        try {
            val file = File(context.filesDir, HISTORY_FILE_NAME)
            val json = messagesAdapter.toJson(messages.takeLast(100))
            file.writeText(json)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save chat history", e)
        }
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        try {
            val file = File(context.filesDir, HISTORY_FILE_NAME)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear chat history", e)
        }
    }

    suspend fun sendChatMessage(
        userMessage: String,
        history: List<ChatMessage>
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured. Please add your GEMINI_API_KEY in the Secrets panel in AI Studio or Settings.")
            )
        }

        // Build multi-turn context (last 10 turns)
        val contents = mutableListOf<GeminiContent>()
        val recentHistory = history.filter { it.status == MessageStatus.SENT }.takeLast(10)
        for (msg in recentHistory) {
            contents.add(
                GeminiContent(
                    role = if (msg.isUser) "user" else "model",
                    parts = listOf(GeminiPart(text = msg.text))
                )
            )
        }
        contents.add(
            GeminiContent(
                role = "user",
                parts = listOf(GeminiPart(text = userMessage))
            )
        )

        val systemInstruction = GeminiContent(
            role = "user",
            parts = listOf(
                GeminiPart(
                    text = "You are NOVA AI, an intelligent, helpful, articulate, and friendly next-generation AI assistant. " +
                            "Provide concise, well-structured answers using clean markdown formatting (bullet points, bold highlights, code blocks) where helpful."
                )
            )
        )

        val request = GeminiRequest(
            contents = contents,
            generationConfig = GeminiGenerationConfig(temperature = 0.7, maxOutputTokens = 2048),
            systemInstruction = systemInstruction
        )

        callWithFallback(apiKey, request)
    }

    suspend fun executeTool(
        toolType: ToolType,
        prompt: String,
        systemPrompt: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured. Please configure GEMINI_API_KEY in the Secrets panel in AI Studio or Settings.")
            )
        }

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = prompt))
                )
            ),
            generationConfig = GeminiGenerationConfig(temperature = 0.6, maxOutputTokens = 2500),
            systemInstruction = GeminiContent(
                role = "user",
                parts = listOf(GeminiPart(text = systemPrompt))
            )
        )

        callWithFallback(apiKey, request)
    }

    private suspend fun callWithFallback(
        apiKey: String,
        request: GeminiRequest
    ): Result<String> {
        var lastError: Exception? = null

        for (model in SUPPORTED_MODELS) {
            try {
                Log.d(TAG, "Invoking Gemini model: $model")
                val response = apiService.generateContent(
                    model = model,
                    apiKey = apiKey,
                    request = request
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    val candidate = body?.candidates?.firstOrNull()
                    val text = candidate?.content?.parts?.joinToString("\n") { it.text }?.trim()

                    if (!text.isNullOrBlank()) {
                        return Result.success(text)
                    } else if (body?.error != null) {
                        lastError = RuntimeException(body.error.message ?: "Gemini returned an error: ${body.error.status}")
                    } else {
                        lastError = RuntimeException("Model returned empty response.")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.w(TAG, "Model $model returned HTTP ${response.code()}: $errorBody")
                    lastError = RuntimeException("HTTP ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Request to model $model failed: ${e.message}")
                lastError = e
            }
        }

        return Result.failure(lastError ?: RuntimeException("Unable to contact Gemini AI service."))
    }
}
