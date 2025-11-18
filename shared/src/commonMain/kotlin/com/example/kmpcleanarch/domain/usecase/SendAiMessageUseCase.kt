package com.example.kmpcleanarch.domain.usecase

import com.example.kmpcleanarch.domain.model.AiMessage
import com.example.kmpcleanarch.domain.model.AiModel
import com.example.kmpcleanarch.domain.repository.AiRepository

/**
 * Use case for sending a message to AI
 */
class SendAiMessageUseCase(
    private val aiRepository: AiRepository
) {
    suspend operator fun invoke(
        message: String,
        conversationHistory: List<AiMessage> = emptyList(),
        model: AiModel
    ): Result<AiMessage> {
        // Validation
        if (message.isBlank()) {
            return Result.failure(IllegalArgumentException("Message cannot be empty"))
        }

        if (message.length > 10000) {
            return Result.failure(IllegalArgumentException("Message is too long (max 10000 characters)"))
        }

        // Send to AI
        return aiRepository.sendMessage(
            message = message.trim(),
            conversationHistory = conversationHistory,
            model = model
        )
    }
}
