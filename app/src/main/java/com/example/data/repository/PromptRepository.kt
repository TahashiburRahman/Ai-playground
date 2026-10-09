package com.example.data.repository

import com.example.data.db.DefaultTemplates
import com.example.data.db.PromptDao
import com.example.data.model.PromptEntity
import kotlinx.coroutines.flow.Flow

class PromptRepository(private val promptDao: PromptDao) {

    fun getAllPrompts(): Flow<List<PromptEntity>> = promptDao.getAllPrompts()

    fun getFavoritePrompts(): Flow<List<PromptEntity>> = promptDao.getFavoritePrompts()

    fun getPromptsByCategory(category: String): Flow<List<PromptEntity>> =
        if (category == "All") promptDao.getAllPrompts() else promptDao.getPromptsByCategory(category)

    fun searchPrompts(query: String): Flow<List<PromptEntity>> = promptDao.searchPrompts(query)

    suspend fun insertPrompt(prompt: PromptEntity): Long = promptDao.insertPrompt(prompt)

    suspend fun updatePrompt(prompt: PromptEntity) = promptDao.updatePrompt(prompt)

    suspend fun deletePrompt(prompt: PromptEntity) = promptDao.deletePrompt(prompt)

    suspend fun toggleFavorite(prompt: PromptEntity) {
        val updated = prompt.copy(isFavorite = !prompt.isFavorite, updatedAt = System.currentTimeMillis())
        promptDao.updatePrompt(updated)
    }

    suspend fun ensureDefaultTemplates() {
        val count = promptDao.getPromptCount()
        if (count == 0) {
            promptDao.insertAll(DefaultTemplates.TEMPLATES)
        }
    }
}
