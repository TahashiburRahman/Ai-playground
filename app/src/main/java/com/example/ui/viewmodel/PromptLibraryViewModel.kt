package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.PromptEntity
import com.example.data.repository.PromptRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PromptLibraryUiState(
    val prompts: List<PromptEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String = "All",
    val categories: List<String> = listOf("All", "Favorites", "Development", "Data Extraction", "Documentation", "Vision & UI", "Support & Tone")
)

class PromptLibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val repository = PromptRepository(database.promptDao())

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow("All")

    val uiState: StateFlow<PromptLibraryUiState> = combine(
        repository.getAllPrompts(),
        _searchQuery,
        _selectedCategory
    ) { allPrompts, query, category ->
        val filtered = allPrompts.filter { prompt ->
            val matchesCategory = when (category) {
                "All" -> true
                "Favorites" -> prompt.isFavorite
                else -> prompt.category.equals(category, ignoreCase = true)
            }
            val matchesQuery = if (query.isBlank()) true else {
                prompt.title.contains(query, ignoreCase = true) ||
                prompt.description.contains(query, ignoreCase = true) ||
                prompt.systemInstruction.contains(query, ignoreCase = true)
            }
            matchesCategory && matchesQuery
        }

        PromptLibraryUiState(
            prompts = filtered,
            searchQuery = query,
            selectedCategory = category
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PromptLibraryUiState()
    )

    init {
        viewModelScope.launch {
            repository.ensureDefaultTemplates()
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(category: String) {
        _selectedCategory.value = category
    }

    fun toggleFavorite(prompt: PromptEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(prompt)
        }
    }

    fun deletePrompt(prompt: PromptEntity) {
        viewModelScope.launch {
            repository.deletePrompt(prompt)
        }
    }

    fun createPrompt(prompt: PromptEntity) {
        viewModelScope.launch {
            repository.insertPrompt(prompt)
        }
    }
}
