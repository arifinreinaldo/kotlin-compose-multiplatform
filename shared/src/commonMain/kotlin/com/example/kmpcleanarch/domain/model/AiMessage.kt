package com.example.kmpcleanarch.domain.model

/**
 * Domain entity representing an AI chat message
 */
data class AiMessage(
    val id: String = generateId(),
    val content: String,
    val role: MessageRole,
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = false
) {
    companion object {
        private fun generateId(): String = System.currentTimeMillis().toString()
    }
}

/**
 * Role of the message sender
 */
enum class MessageRole {
    USER,    // Message from the user
    ASSISTANT, // Message from AI
    SYSTEM   // System message (optional)
}

/**
 * AI chat session
 */
data class AiChatSession(
    val id: String = generateSessionId(),
    val title: String,
    val messages: List<AiMessage> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        private fun generateSessionId(): String = "session_${System.currentTimeMillis()}"
    }

    fun addMessage(message: AiMessage): AiChatSession {
        return copy(
            messages = messages + message,
            updatedAt = System.currentTimeMillis()
        )
    }

    fun getLastUserMessage(): AiMessage? {
        return messages.lastOrNull { it.role == MessageRole.USER }
    }
}

/**
 * AI model configuration
 */
data class AiModel(
    val id: String,
    val name: String,
    val provider: AiProvider,
    val maxTokens: Int = 2000,
    val temperature: Float = 0.7f
)

/**
 * Supported AI providers
 */
enum class AiProvider {
    OPENAI,      // OpenAI (GPT-3.5, GPT-4)
    ANTHROPIC,   // Anthropic (Claude)
    GOOGLE,      // Google (Gemini)
    CUSTOM       // Custom/Local API
}
