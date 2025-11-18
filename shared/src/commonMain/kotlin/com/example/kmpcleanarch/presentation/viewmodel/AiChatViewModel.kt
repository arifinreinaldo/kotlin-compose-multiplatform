package com.example.kmpcleanarch.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kmpcleanarch.domain.model.AiMessage
import com.example.kmpcleanarch.domain.model.MessageRole
import com.example.kmpcleanarch.domain.usecase.GetAvailableAiModelsUseCase
import com.example.kmpcleanarch.domain.usecase.SendAiMessageUseCase
import com.example.kmpcleanarch.presentation.state.AiChatUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for AI Chat Screen
 */
class AiChatViewModel(
    private val sendAiMessageUseCase: SendAiMessageUseCase,
    private val getAvailableModelsUseCase: GetAvailableAiModelsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiChatUiState())
    val uiState: StateFlow<AiChatUiState> = _uiState.asStateFlow()

    // Simulated API key storage (in production, use secure storage)
    private var apiKey: String? = null

    init {
        loadAvailableModels()
    }

    fun onInputChanged(input: String) {
        _uiState.update { it.copy(currentInput = input, error = null) }
    }

    fun sendMessage() {
        val input = _uiState.value.currentInput.trim()
        val model = _uiState.value.selectedModel

        if (input.isBlank() || model == null) return

        // Add user message to chat
        val userMessage = AiMessage(
            content = input,
            role = MessageRole.USER
        )

        _uiState.update {
            it.copy(
                messages = it.messages + userMessage,
                currentInput = "",
                isLoading = true,
                error = null
            )
        }

        // Send to AI
        viewModelScope.launch {
            sendAiMessageUseCase(
                message = input,
                conversationHistory = _uiState.value.messages.dropLast(1), // Exclude the message we just added
                model = model
            )
                .onSuccess { aiResponse ->
                    _uiState.update {
                        it.copy(
                            messages = it.messages + aiResponse,
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    // Remove user message on error and show error
                    _uiState.update {
                        it.copy(
                            messages = it.messages.dropLast(1),
                            currentInput = input, // Restore input
                            isLoading = false,
                            error = error.message ?: "Failed to send message"
                        )
                    }
                }
        }
    }

    fun selectModel(model: com.example.kmpcleanarch.domain.model.AiModel) {
        _uiState.update { it.copy(selectedModel = model) }
    }

    fun setApiKey(key: String) {
        if (key.isNotBlank()) {
            apiKey = key.trim()
            _uiState.update {
                it.copy(
                    isApiKeySet = true,
                    showApiKeyDialog = false,
                    error = null
                )
            }

            // Auto-select first model if none selected
            if (_uiState.value.selectedModel == null && _uiState.value.availableModels.isNotEmpty()) {
                selectModel(_uiState.value.availableModels.first())
            }
        }
    }

    fun showApiKeyDialog() {
        _uiState.update { it.copy(showApiKeyDialog = true) }
    }

    fun hideApiKeyDialog() {
        _uiState.update { it.copy(showApiKeyDialog = false) }
    }

    fun clearChat() {
        _uiState.update { it.copy(messages = emptyList(), error = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun loadAvailableModels() {
        viewModelScope.launch {
            getAvailableModelsUseCase()
                .onSuccess { models ->
                    _uiState.update {
                        it.copy(
                            availableModels = models,
                            selectedModel = models.firstOrNull()
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(error = "Failed to load models: ${error.message}")
                    }
                }
        }
    }
}
