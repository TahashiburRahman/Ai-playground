package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.ChatMessage
import com.example.data.model.PromptConfig
import com.example.data.model.PromptEntity
import com.example.data.repository.GeminiRepository
import com.example.data.repository.GenerationResult
import com.example.data.repository.PromptRepository
import com.example.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

data class PlaygroundUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isGenerating: Boolean = false,
    val attachedImageBase64: String? = null,
    val attachedImageUri: Uri? = null,
    val config: PromptConfig = PromptConfig(),
    val totalInputTokens: Int = 0,
    val errorBanner: String? = null,
    val lastPromptSent: String = ""
)

class PlaygroundViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application)
    private val database = AppDatabase.getInstance(application)
    private val promptRepository = PromptRepository(database.promptDao())
    private val geminiRepository = GeminiRepository(settingsRepository)

    private val _uiState = MutableStateFlow(PlaygroundUiState())
    val uiState: StateFlow<PlaygroundUiState> = _uiState.asStateFlow()

    init {
        // Initialize with default model preference
        val defaultModel = settingsRepository.getDefaultModel()
        _uiState.value = _uiState.value.copy(
            config = _uiState.value.config.copy(modelId = defaultModel)
        )
        viewModelScope.launch {
            promptRepository.ensureDefaultTemplates()
        }
    }

    fun updateConfig(newConfig: PromptConfig) {
        _uiState.value = _uiState.value.copy(config = newConfig)
        recalculateTokenEstimate()
    }

    fun attachImage(uri: Uri) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (bitmap != null) {
                    val stream = ByteArrayOutputStream()
                    // Compress to max 1024x1024 to keep payload agile
                    val scaled = if (bitmap.width > 1024 || bitmap.height > 1024) {
                        val factor = 1024f / maxOf(bitmap.width, bitmap.height)
                        Bitmap.createScaledBitmap(bitmap, (bitmap.width * factor).toInt(), (bitmap.height * factor).toInt(), true)
                    } else bitmap
                    scaled.compress(Bitmap.CompressFormat.JPEG, 85, stream)
                    val base64 = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
                    _uiState.value = _uiState.value.copy(
                        attachedImageBase64 = base64,
                        attachedImageUri = uri
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorBanner = "Failed to load image: ${e.message}")
            }
        }
    }

    fun clearAttachedImage() {
        _uiState.value = _uiState.value.copy(
            attachedImageBase64 = null,
            attachedImageUri = null
        )
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(errorBanner = null)
    }

    fun clearConversation() {
        _uiState.value = _uiState.value.copy(
            messages = emptyList(),
            attachedImageBase64 = null,
            attachedImageUri = null,
            errorBanner = null
        )
        recalculateTokenEstimate()
    }

    fun loadPrompt(prompt: PromptEntity) {
        _uiState.value = _uiState.value.copy(
            config = PromptConfig(
                modelId = prompt.modelId,
                systemInstruction = prompt.systemInstruction,
                temperature = prompt.temperature,
                topP = prompt.topP,
                topK = prompt.topK,
                maxOutputTokens = prompt.maxOutputTokens,
                isJsonOutput = prompt.isJsonOutput
            ),
            lastPromptSent = prompt.samplePrompt
        )
        recalculateTokenEstimate()
    }

    fun saveCurrentAsPrompt(title: String, description: String, category: String) {
        viewModelScope.launch {
            val lastUserMsg = _uiState.value.messages.lastOrNull { it.isUser }?.text ?: _uiState.value.lastPromptSent
            val entity = PromptEntity(
                title = title.ifBlank { "Custom Prompt" },
                description = description.ifBlank { "Saved from Playground" },
                systemInstruction = _uiState.value.config.systemInstruction,
                samplePrompt = lastUserMsg,
                modelId = _uiState.value.config.modelId,
                temperature = _uiState.value.config.temperature,
                topP = _uiState.value.config.topP,
                topK = _uiState.value.config.topK,
                maxOutputTokens = _uiState.value.config.maxOutputTokens,
                isJsonOutput = _uiState.value.config.isJsonOutput,
                category = category,
                isTemplate = false
            )
            promptRepository.insertPrompt(entity)
        }
    }

    fun sendMessage(promptText: String) {
        if (promptText.isBlank() && _uiState.value.attachedImageBase64 == null) return
        if (_uiState.value.isGenerating) return

        val userMessage = ChatMessage(
            isUser = true,
            text = promptText.trim(),
            imageBase64 = _uiState.value.attachedImageBase64
        )

        val updatedMessages = _uiState.value.messages + userMessage
        val imageToSend = _uiState.value.attachedImageBase64
        val configToSend = _uiState.value.config

        _uiState.value = _uiState.value.copy(
            messages = updatedMessages,
            isGenerating = true,
            attachedImageBase64 = null, // Consumed into message
            attachedImageUri = null,
            errorBanner = null,
            lastPromptSent = promptText.trim()
        )

        recalculateTokenEstimate()

        viewModelScope.launch {
            val result = geminiRepository.generateContent(
                prompt = promptText.trim(),
                config = configToSend,
                chatHistory = updatedMessages.dropLast(1),
                imageBase64 = imageToSend
            )

            when (result) {
                is GenerationResult.Success -> {
                    val assistantMessage = ChatMessage(
                        isUser = false,
                        text = result.text,
                        latencyMs = result.latencyMs,
                        tokenCount = result.usageMetadata?.candidatesTokenCount,
                        modelUsed = configToSend.modelId
                    )
                    _uiState.value = _uiState.value.copy(
                        messages = _uiState.value.messages + assistantMessage,
                        isGenerating = false
                    )
                }
                is GenerationResult.Error -> {
                    val errorAssistantMessage = ChatMessage(
                        isUser = false,
                        text = result.message,
                        isError = true,
                        modelUsed = configToSend.modelId
                    )
                    _uiState.value = _uiState.value.copy(
                        messages = _uiState.value.messages + errorAssistantMessage,
                        isGenerating = false,
                        errorBanner = if (result.isAuthError) result.message else null
                    )
                }
            }
        }
    }

    fun recalculateTokenEstimate() {
        val sysChars = _uiState.value.config.systemInstruction.length
        val msgChars = _uiState.value.messages.sumOf { it.text.length }
        // Rough token estimation: ~4 chars per token in English
        val estimated = (sysChars + msgChars) / 4
        _uiState.value = _uiState.value.copy(totalInputTokens = estimated)
    }
}
