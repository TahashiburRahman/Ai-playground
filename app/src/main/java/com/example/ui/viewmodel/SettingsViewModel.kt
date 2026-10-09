package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.repository.GeminiRepository
import com.example.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val customApiKeyInput: String = "",
    val effectiveApiKeyMasked: String = "",
    val hasValidConfiguredKey: Boolean = false,
    val isTestingConnection: Boolean = false,
    val testConnectionResult: String? = null,
    val isTestSuccess: Boolean? = null,
    val defaultModel: String = "gemini-3.5-flash",
    val buildConfigKeyAvailable: Boolean = false
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application)
    private val geminiRepository = GeminiRepository(settingsRepository)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        val customKey = settingsRepository.getCustomApiKey()
        val effective = settingsRepository.getEffectiveApiKey()
        val buildKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }
        val hasBuildKey = buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY"

        val masked = if (effective.length > 8) {
            "${effective.take(4)}••••••••${effective.takeLast(4)}"
        } else if (effective.isNotBlank()) {
            "••••••••"
        } else {
            "Not configured"
        }

        _uiState.value = SettingsUiState(
            customApiKeyInput = customKey,
            effectiveApiKeyMasked = masked,
            hasValidConfiguredKey = settingsRepository.hasValidApiKeyConfigured(),
            defaultModel = settingsRepository.getDefaultModel(),
            buildConfigKeyAvailable = hasBuildKey
        )
    }

    fun onCustomApiKeyChanged(newKey: String) {
        _uiState.value = _uiState.value.copy(customApiKeyInput = newKey)
    }

    fun saveApiKey() {
        settingsRepository.setCustomApiKey(_uiState.value.customApiKeyInput)
        loadSettings()
    }

    fun clearCustomApiKey() {
        settingsRepository.setCustomApiKey("")
        loadSettings()
    }

    fun onDefaultModelChanged(modelId: String) {
        settingsRepository.setDefaultModel(modelId)
        _uiState.value = _uiState.value.copy(defaultModel = modelId)
    }

    fun testConnection() {
        val key = _uiState.value.customApiKeyInput.ifBlank {
            settingsRepository.getEffectiveApiKey()
        }
        if (key.isBlank() || key == "MY_GEMINI_API_KEY") {
            _uiState.value = _uiState.value.copy(
                testConnectionResult = "Please provide an API key before testing.",
                isTestSuccess = false
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            isTestingConnection = true,
            testConnectionResult = null,
            isTestSuccess = null
        )

        viewModelScope.launch {
            val (success, message) = geminiRepository.testConnection(key)
            _uiState.value = _uiState.value.copy(
                isTestingConnection = false,
                testConnectionResult = message,
                isTestSuccess = success
            )
        }
    }
}
