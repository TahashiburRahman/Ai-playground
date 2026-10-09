package com.example.data.repository

import com.example.data.api.ApiClient
import com.example.data.model.ApiErrorDetail
import com.example.data.model.Content
import com.example.data.model.GenerateContentRequest
import com.example.data.model.GenerationConfig
import com.example.data.model.InlineData
import com.example.data.model.Part
import com.example.data.model.PromptConfig
import com.example.data.model.ThinkingConfig
import com.example.data.model.UsageMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

sealed class GenerationResult {
    data class Success(
        val text: String,
        val latencyMs: Long,
        val usageMetadata: UsageMetadata?,
        val finishReason: String?
    ) : GenerationResult()

    data class Error(
        val message: String,
        val errorCode: Int? = null,
        val isAuthError: Boolean = false
    ) : GenerationResult()
}

class GeminiRepository(private val settingsRepository: SettingsRepository) {

    suspend fun generateContent(
        prompt: String,
        config: PromptConfig,
        chatHistory: List<com.example.data.model.ChatMessage> = emptyList(),
        imageBase64: String? = null,
        imageMimeType: String = "image/jpeg"
    ): GenerationResult = withContext(Dispatchers.IO) {
        val apiKey = settingsRepository.getEffectiveApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext GenerationResult.Error(
                message = "Gemini API key is not configured. Please set your API key in Settings or configure GEMINI_API_KEY in the AI Studio Secrets panel.",
                errorCode = 401,
                isAuthError = true
            )
        }

        val startTime = System.currentTimeMillis()

        try {
            // Build contents list
            val contents = mutableListOf<Content>()

            // Add previous conversation turns if in chat mode
            chatHistory.filter { !it.isError }.takeLast(8).forEach { msg ->
                val role = if (msg.isUser) "user" else "model"
                contents.add(
                    Content(
                        role = role,
                        parts = listOf(Part(text = msg.text))
                    )
                )
            }

            // Build current turn parts
            val currentParts = mutableListOf<Part>()
            if (!imageBase64.isNullOrBlank()) {
                currentParts.add(
                    Part(
                        inlineData = InlineData(
                            mimeType = imageMimeType,
                            data = imageBase64
                        )
                    )
                )
            }
            if (prompt.isNotBlank()) {
                currentParts.add(Part(text = prompt))
            }

            contents.add(Content(role = "user", parts = currentParts))

            // Build system instruction if provided
            val systemInstruction = if (config.systemInstruction.isNotBlank()) {
                Content(parts = listOf(Part(text = config.systemInstruction)))
            } else null

            // Build generation config
            val thinkingConfig = if (config.thinkingLevel != "none" && config.modelId.contains("pro")) {
                ThinkingConfig(thinkingLevel = config.thinkingLevel)
            } else null

            val genConfig = GenerationConfig(
                temperature = config.temperature,
                topP = config.topP,
                topK = config.topK,
                maxOutputTokens = config.maxOutputTokens,
                responseMimeType = if (config.isJsonOutput) "application/json" else null,
                thinkingConfig = thinkingConfig
            )

            val request = GenerateContentRequest(
                contents = contents,
                generationConfig = genConfig,
                systemInstruction = systemInstruction
            )

            val response = ApiClient.service.generateContent(
                model = config.modelId,
                apiKey = apiKey,
                request = request
            )

            val latency = System.currentTimeMillis() - startTime

            if (response.isSuccessful) {
                val body = response.body()
                val candidate = body?.candidates?.firstOrNull()
                val text = candidate?.content?.parts?.joinToString("\n") { it.text ?: "" } ?: ""

                if (text.isNotBlank()) {
                    GenerationResult.Success(
                        text = text,
                        latencyMs = latency,
                        usageMetadata = body?.usageMetadata,
                        finishReason = candidate?.finishReason
                    )
                } else {
                    GenerationResult.Error(
                        message = "Model returned an empty response. Finish reason: ${candidate?.finishReason ?: "UNKNOWN"}"
                    )
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: ""
                val parsedMessage = extractErrorMessage(errorBody, response.code())
                val isAuth = response.code() == 400 || response.code() == 401 || response.code() == 403
                GenerationResult.Error(
                    message = parsedMessage,
                    errorCode = response.code(),
                    isAuthError = isAuth
                )
            }
        } catch (e: Exception) {
            GenerationResult.Error(
                message = "Network error: ${e.localizedMessage ?: e.message ?: "Failed to connect to Gemini API"}"
            )
        }
    }

    suspend fun testConnection(apiKey: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Pair(false, "API Key is empty")
        }

        try {
            val request = GenerateContentRequest(
                contents = listOf(
                    Content(
                        role = "user",
                        parts = listOf(Part(text = "Hello! Reply with 'OK' only."))
                    )
                ),
                generationConfig = GenerationConfig(maxOutputTokens = 10)
            )

            val startTime = System.currentTimeMillis()
            val response = ApiClient.service.generateContent(
                model = "gemini-3.5-flash",
                apiKey = apiKey.trim(),
                request = request
            )
            val duration = System.currentTimeMillis() - startTime

            if (response.isSuccessful) {
                Pair(true, "Connected successfully in ${duration}ms! API key is valid.")
            } else {
                val errorBody = response.errorBody()?.string() ?: ""
                val msg = extractErrorMessage(errorBody, response.code())
                Pair(false, "Verification failed (${response.code()}): $msg")
            }
        } catch (e: Exception) {
            Pair(false, "Connection error: ${e.localizedMessage ?: e.message}")
        }
    }

    private fun extractErrorMessage(errorBody: String, code: Int): String {
        return try {
            val json = JSONObject(errorBody)
            if (json.has("error")) {
                val errorObj = json.getJSONObject("error")
                val message = errorObj.optString("message", "")
                val status = errorObj.optString("status", "")
                if (message.isNotBlank()) {
                    "$message ($status)"
                } else "HTTP $code"
            } else {
                "HTTP $code: $errorBody"
            }
        } catch (e: Exception) {
            if (code == 403 || code == 400) {
                "Invalid API key or insufficient permissions (HTTP $code)"
            } else if (code == 429) {
                "Rate limit exceeded (HTTP 429). Please wait a moment."
            } else {
                "Request failed with status code $code"
            }
        }
    }
}
