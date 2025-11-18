package com.example.kmpcleanarch.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kmpcleanarch.domain.repository.UserRepository
import com.example.kmpcleanarch.domain.usecase.SaveUserUseCase
import com.example.kmpcleanarch.presentation.state.UserFormUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for User Form Screen (Create/Edit)
 */
class UserFormViewModel(
    private val saveUserUseCase: SaveUserUseCase,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserFormUiState())
    val uiState: StateFlow<UserFormUiState> = _uiState.asStateFlow()

    fun loadUser(userId: Long) {
        if (userId == 0L) return

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val user = userRepository.getUserById(userId)
            if (user != null) {
                _uiState.update {
                    it.copy(
                        userId = user.id,
                        email = user.email,
                        name = user.name,
                        role = user.role,
                        avatarUrl = user.avatarUrl ?: "",
                        isLoading = false
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "User not found"
                    )
                }
            }
        }
    }

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, error = null) }
    }

    fun onNameChanged(name: String) {
        _uiState.update { it.copy(name = name, error = null) }
    }

    fun onRoleChanged(role: String) {
        _uiState.update { it.copy(role = role, error = null) }
    }

    fun onAvatarUrlChanged(avatarUrl: String) {
        _uiState.update { it.copy(avatarUrl = avatarUrl, error = null) }
    }

    fun saveUser() {
        val state = _uiState.value

        _uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            saveUserUseCase(
                id = state.userId,
                email = state.email,
                name = state.name,
                role = state.role,
                avatarUrl = state.avatarUrl.ifBlank { null }
            )
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            isSaveSuccessful = true,
                            error = null
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = exception.message ?: "Failed to save user"
                        )
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun resetSaveSuccess() {
        _uiState.update { it.copy(isSaveSuccessful = false) }
    }
}
