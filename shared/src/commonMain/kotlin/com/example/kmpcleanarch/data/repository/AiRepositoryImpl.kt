package com.example.kmpcleanarch.data.repository

import com.example.kmpcleanarch.data.remote.api.AiApi
import com.example.kmpcleanarch.data.remote.dto.ChatCompletionRequest
import com.example.kmpcleanarch.data.remote.dto.ChatMessage
import com.example.kmpcleanarch.domain.model.AiMessage
import com.example.kmpcleanarch.domain.model.AiModel
import com.example.kmpcleanarch.domain.model.AiProvider
import com.example.kmpcleanarch.domain.model.MessageRole
import com.example.kmpcleanarch.domain.repository.AiRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Implementation of AiRepository using OpenAI-compatible API
 */
class AiRepositoryImpl(
    private val aiApi: AiApi
) : AiRepository {

    override suspend fun sendMessage(
        message: String,
        conversationHistory: List<AiMessage>,
        model: AiModel
    ): Result<AiMessage> {
        return try {
            // Convert conversation history to API format
            val messages = conversationHistory.map { it.toChatMessage() } +
                    ChatMessage(role = "user", content = message)

            // Make API request
            val request = ChatCompletionRequest(
                model = model.id,
                messages = messages,
                temperature = model.temperature,
                maxTokens = model.maxTokens
            )

            aiApi.chatCompletion(request)
                .map { response ->
                    val assistantMessage = response.choices.firstOrNull()?.message
                        ?: throw Exception("No response from AI")

                    AiMessage(
                        content = assistantMessage.content,
                        role = MessageRole.ASSISTANT,
                        timestamp = System.currentTimeMillis()
                    )
                }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun streamMessage(
        message: String,
        conversationHistory: List<AiMessage>,
        model: AiModel
    ): Flow<String> = flow {
        // For streaming, you would use Server-Sent Events (SSE)
        // This is a simplified version that emits the full response
        // In production, implement proper SSE streaming

        val result = sendMessage(message, conversationHistory, model)
        result.onSuccess { aiMessage ->
            emit(aiMessage.content)
        }.onFailure { error ->
            throw error
        }
    }

    override suspend fun getAvailableModels(): Result<List<AiModel>> {
        // Return predefined models
        // In production, you might fetch this from the API
        return Result.success(
            listOf(
                AiModel(
                    id = "gpt-3.5-turbo",
                    name = "GPT-3.5 Turbo",
                    provider = AiProvider.OPENAI,
                    maxTokens = 4000,
                    temperature = 0.7f
                ),
                AiModel(
                    id = "gpt-4",
                    name = "GPT-4",
                    provider = AiProvider.OPENAI,
                    maxTokens = 8000,
                    temperature = 0.7f
                ),
                AiModel(
                    id = "gpt-4-turbo-preview",
                    name = "GPT-4 Turbo",
                    provider = AiProvider.OPENAI,
                    maxTokens = 4000,
                    temperature = 0.7f
                )
            )
        )
    }

    override suspend fun validateApiKey(apiKey: String): Result<Boolean> {
        return aiApi.validateApiKey()
    }

    private fun AiMessage.toChatMessage(): ChatMessage {
        return ChatMessage(
            role = when (role) {
                MessageRole.USER -> "user"
                MessageRole.ASSISTANT -> "assistant"
                MessageRole.SYSTEM -> "system"
            },
            content = content
        )
    }
}
