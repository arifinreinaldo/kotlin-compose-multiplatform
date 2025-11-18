package com.example.kmpcleanarch.domain.repository

import com.example.kmpcleanarch.domain.model.AiChatSession
import com.example.kmpcleanarch.domain.model.AiMessage
import com.example.kmpcleanarch.domain.model.AiModel
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for AI operations
 */
interface AiRepository {
    /**
     * Send a message to AI and get response
     */
    suspend fun sendMessage(
        message: String,
        conversationHistory: List<AiMessage> = emptyList(),
        model: AiModel
    ): Result<AiMessage>

    /**
     * Stream AI response (for real-time typing effect)
     * Returns a Flow of partial responses
     */
    fun streamMessage(
        message: String,
        conversationHistory: List<AiMessage> = emptyList(),
        model: AiModel
    ): Flow<String>

    /**
     * Get available AI models
     */
    suspend fun getAvailableModels(): Result<List<AiModel>>

    /**
     * Validate API key
     */
    suspend fun validateApiKey(apiKey: String): Result<Boolean>
}
