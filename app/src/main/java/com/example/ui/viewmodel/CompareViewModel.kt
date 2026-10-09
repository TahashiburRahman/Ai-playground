package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.PromptConfig
import com.example.data.repository.GeminiRepository
import com.example.data.repository.GenerationResult
import com.example.data.repository.SettingsRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ModelResult(
    val modelId: String,
    val text: String? = null,
    val latencyMs: Long? = null,
    val tokenCount: Int? = null,
    val error: String? = null,
    val isLoading: Boolean = false
)

data class CompareUiState(
    val promptText: String = "Explain the difference between mutableStateOf and remember in Jetpack Compose in 3 bullet points with code.",
    val systemInstruction: String = "Be direct, precise, and concise.",
    val modelAId: String = "gemini-3.5-flash",
    val modelBId: String = "gemini-3.1-pro-preview",
    val temperature: Float = 0.5f,
    val resultA: ModelResult = ModelResult(modelId = "gemini-3.5-flash"),
    val resultB: ModelResult = ModelResult(modelId = "gemini-3.1-pro-preview"),
    val isComparing: Boolean = false
)

class CompareViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application)
    private val geminiRepository = GeminiRepository(settingsRepository)

    private val _uiState = MutableStateFlow(CompareUiState())
    val uiState: StateFlow<CompareUiState> = _uiState.asStateFlow()

    fun updatePrompt(prompt: String) {
        _uiState.value = _uiState.value.copy(promptText = prompt)
    }

    fun updateSystemInstruction(sys: String) {
        _uiState.value = _uiState.value.copy(systemInstruction = sys)
    }

    fun updateModelA(modelId: String) {
        _uiState.value = _uiState.value.copy(
            modelAId = modelId,
            resultA = _uiState.value.resultA.copy(modelId = modelId)
        )
    }

    fun updateModelB(modelId: String) {
        _uiState.value = _uiState.value.copy(
            modelBId = modelId,
            resultB = _uiState.value.resultB.copy(modelId = modelId)
        )
    }

    fun updateTemperature(temp: Float) {
        _uiState.value = _uiState.value.copy(temperature = temp)
    }

    fun runComparison() {
        val prompt = _uiState.value.promptText.trim()
        if (prompt.isBlank() || _uiState.value.isComparing) return

        val state = _uiState.value
        _uiState.value = state.copy(
            isComparing = true,
            resultA = ModelResult(modelId = state.modelAId, isLoading = true),
            resultB = ModelResult(modelId = state.modelBId, isLoading = true)
        )

        viewModelScope.launch {
            val deferredA = async {
                val config = PromptConfig(
                    modelId = state.modelAId,
                    systemInstruction = state.systemInstruction,
                    temperature = state.temperature
                )
                geminiRepository.generateContent(prompt = prompt, config = config)
            }

            val deferredB = async {
                val config = PromptConfig(
                    modelId = state.modelBId,
                    systemInstruction = state.systemInstruction,
                    temperature = state.temperature
                )
                geminiRepository.generateContent(prompt = prompt, config = config)
            }

            val resA = deferredA.await()
            val resB = deferredB.await()

            val modelResultA = when (resA) {
                is GenerationResult.Success -> ModelResult(
                    modelId = state.modelAId,
                    text = resA.text,
                    latencyMs = resA.latencyMs,
                    tokenCount = resA.usageMetadata?.candidatesTokenCount
                )
                is GenerationResult.Error -> ModelResult(
                    modelId = state.modelAId,
                    error = resA.message
                )
            }

            val modelResultB = when (resB) {
                is GenerationResult.Success -> ModelResult(
                    modelId = state.modelBId,
                    text = resB.text,
                    latencyMs = resB.latencyMs,
                    tokenCount = resB.usageMetadata?.candidatesTokenCount
                )
                is GenerationResult.Error -> ModelResult(
                    modelId = state.modelBId,
                    error = resB.message
                )
            }

            _uiState.value = _uiState.value.copy(
                resultA = modelResultA,
                resultB = modelResultB,
                isComparing = false
            )
        }
    }
}
