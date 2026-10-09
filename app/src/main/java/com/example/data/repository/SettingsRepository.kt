package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("ai_studio_settings", Context.MODE_PRIVATE)

    private val _apiKeyFlow = MutableStateFlow(getCustomApiKey())
    val apiKeyFlow: StateFlow<String> = _apiKeyFlow.asStateFlow()

    fun getCustomApiKey(): String {
        return prefs.getString(KEY_CUSTOM_API_KEY, "") ?: ""
    }

    fun setCustomApiKey(key: String) {
        prefs.edit().putString(KEY_CUSTOM_API_KEY, key.trim()).apply()
        _apiKeyFlow.value = key.trim()
    }

    /**
     * Resolves the active API Key:
     * 1. User's manually entered custom key if non-empty
     * 2. Otherwise BuildConfig.GEMINI_API_KEY if injected
     */
    fun getEffectiveApiKey(): String {
        val customKey = getCustomApiKey()
        if (customKey.isNotBlank()) {
            return customKey
        }
        val buildKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
        return if (buildKey != "MY_GEMINI_API_KEY" && buildKey.isNotBlank()) buildKey else ""
    }

    fun hasValidApiKeyConfigured(): Boolean {
        val key = getEffectiveApiKey()
        return key.isNotBlank() && key != "MY_GEMINI_API_KEY"
    }

    fun getDefaultModel(): String {
        return prefs.getString(KEY_DEFAULT_MODEL, "gemini-3.5-flash") ?: "gemini-3.5-flash"
    }

    fun setDefaultModel(modelId: String) {
        prefs.edit().putString(KEY_DEFAULT_MODEL, modelId).apply()
    }

    companion object {
        private const val KEY_CUSTOM_API_KEY = "custom_gemini_api_key"
        private const val KEY_DEFAULT_MODEL = "default_model_id"
    }
}
