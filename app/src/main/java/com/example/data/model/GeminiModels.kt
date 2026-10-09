package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Supported Gemini Models according to Google AI Studio guidelines.
 */
data class ModelInfo(
    val id: String,
    val displayName: String,
    val description: String,
    val badge: String,
    val isRecommended: Boolean = false,
    val supportsThinking: Boolean = false,
    val supportsVision: Boolean = true
)

object AvailableModels {
    val GEMINI_3_5_FLASH = ModelInfo(
        id = "gemini-3.5-flash",
        displayName = "Gemini 3.5 Flash",
        description = "Fastest, multimodal, cost-effective model for high-frequency tasks and quick answers.",
        badge = "Default",
        isRecommended = true,
        supportsVision = true
    )

    val GEMINI_3_1_PRO = ModelInfo(
        id = "gemini-3.1-pro-preview",
        displayName = "Gemini 3.1 Pro Preview",
        description = "Highest intelligence model for complex reasoning, multi-step coding, and STEM.",
        badge = "Advanced",
        supportsThinking = true,
        supportsVision = true
    )

    val GEMINI_3_1_FLASH_LITE = ModelInfo(
        id = "gemini-3.1-flash-lite-preview",
        displayName = "Gemini 3.1 Flash Lite",
        description = "Ultra-low latency model engineered for speed and high throughput.",
        badge = "Fast & Lite",
        supportsVision = true
    )

    val GEMINI_2_5_FLASH_IMAGE = ModelInfo(
        id = "gemini-2.5-flash-image",
        displayName = "Gemini 2.5 Flash Image",
        description = "Optimized for visual understanding and image editing tasks.",
        badge = "Vision",
        supportsVision = true
    )

    val ALL = listOf(
        GEMINI_3_5_FLASH,
        GEMINI_3_1_PRO,
        GEMINI_3_1_FLASH_LITE,
        GEMINI_2_5_FLASH_IMAGE
    )

    fun getById(id: String): ModelInfo {
        return ALL.firstOrNull { it.id == id } ?: GEMINI_3_5_FLASH
    }
}

// Request Models
@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val role: String? = null,
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String? = null,
    val inlineData: InlineData? = null
)

@JsonClass(generateAdapter = true)
data class InlineData(
    val mimeType: String,
    val data: String
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val temperature: Float? = null,
    val topP: Float? = null,
    val topK: Int? = null,
    val maxOutputTokens: Int? = null,
    val responseMimeType: String? = null,
    val thinkingConfig: ThinkingConfig? = null
)

@JsonClass(generateAdapter = true)
data class ThinkingConfig(
    val thinkingLevel: String? = null
)

// Response Models
@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>? = null,
    val usageMetadata: UsageMetadata? = null,
    val error: ApiErrorDetail? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content? = null,
    val finishReason: String? = null,
    val index: Int? = null
)

@JsonClass(generateAdapter = true)
data class UsageMetadata(
    val promptTokenCount: Int? = null,
    val candidatesTokenCount: Int? = null,
    val totalTokenCount: Int? = null
)

@JsonClass(generateAdapter = true)
data class ApiErrorDetail(
    val code: Int? = null,
    val message: String? = null,
    val status: String? = null
)

// App UI Message representations
data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val imageBase64: String? = null,
    val latencyMs: Long? = null,
    val tokenCount: Int? = null,
    val modelUsed: String? = null,
    val isError: Boolean = false
)

data class PromptConfig(
    val modelId: String = "gemini-3.5-flash",
    val systemInstruction: String = "",
    val temperature: Float = 0.7f,
    val topP: Float = 0.95f,
    val topK: Int = 40,
    val maxOutputTokens: Int = 2048,
    val isJsonOutput: Boolean = false,
    val thinkingLevel: String = "none" // "none", "low", "high"
)
