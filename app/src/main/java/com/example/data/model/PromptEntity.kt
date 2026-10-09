package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prompts")
data class PromptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val systemInstruction: String,
    val samplePrompt: String,
    val modelId: String = "gemini-3.5-flash",
    val temperature: Float = 0.7f,
    val topP: Float = 0.95f,
    val topK: Int = 40,
    val maxOutputTokens: Int = 2048,
    val isJsonOutput: Boolean = false,
    val category: String = "General",
    val isFavorite: Boolean = false,
    val isTemplate: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
