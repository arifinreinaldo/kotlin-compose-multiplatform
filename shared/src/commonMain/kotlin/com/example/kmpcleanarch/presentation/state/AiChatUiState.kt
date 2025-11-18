package com.example.kmpcleanarch.presentation.state

import com.example.kmpcleanarch.domain.model.AiMessage
import com.example.kmpcleanarch.domain.model.AiModel

/**
 * UI State for AI Chat Screen
 */
data class AiChatUiState(
    val messages: List<AiMessage> = emptyList(),
    val currentInput: String = "",
    val isLoading: Boolean = false,
    val isTyping: Boolean = false,
    val error: String? = null,
    val selectedModel: AiModel? = null,
    val availableModels: List<AiModel> = emptyList(),
    val isApiKeySet: Boolean = false,
    val showApiKeyDialog: Boolean = false
) {
    val canSendMessage: Boolean
        get() = currentInput.isNotBlank() && !isLoading && isApiKeySet && selectedModel != null
}
