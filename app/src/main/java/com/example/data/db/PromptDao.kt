package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PromptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PromptDao {
    @Query("SELECT * FROM prompts ORDER BY isFavorite DESC, updatedAt DESC")
    fun getAllPrompts(): Flow<List<PromptEntity>>

    @Query("SELECT * FROM prompts WHERE id = :id")
    suspend fun getPromptById(id: Long): PromptEntity?

    @Query("SELECT * FROM prompts WHERE isFavorite = 1 ORDER BY updatedAt DESC")
    fun getFavoritePrompts(): Flow<List<PromptEntity>>

    @Query("SELECT * FROM prompts WHERE category = :category ORDER BY isFavorite DESC, updatedAt DESC")
    fun getPromptsByCategory(category: String): Flow<List<PromptEntity>>

    @Query("SELECT * FROM prompts WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchPrompts(query: String): Flow<List<PromptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrompt(prompt: PromptEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(prompts: List<PromptEntity>)

    @Update
    suspend fun updatePrompt(prompt: PromptEntity)

    @Delete
    suspend fun deletePrompt(prompt: PromptEntity)

    @Query("SELECT COUNT(*) FROM prompts")
    suspend fun getPromptCount(): Int
}
