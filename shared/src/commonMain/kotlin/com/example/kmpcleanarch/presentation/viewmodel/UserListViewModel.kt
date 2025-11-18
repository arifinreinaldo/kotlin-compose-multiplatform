package com.example.kmpcleanarch.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kmpcleanarch.domain.usecase.DeleteUserUseCase
import com.example.kmpcleanarch.domain.usecase.GetAllUsersUseCase
import com.example.kmpcleanarch.domain.usecase.LogoutUseCase
import com.example.kmpcleanarch.presentation.state.UserListUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for User List Screen
 */
class UserListViewModel(
    private val getAllUsersUseCase: GetAllUsersUseCase,
    private val deleteUserUseCase: DeleteUserUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserListUiState(isLoading = true))
    val uiState: StateFlow<UserListUiState> = _uiState.asStateFlow()

    init {
        loadUsers()
    }

    private fun loadUsers() {
        viewModelScope.launch {
            getAllUsersUseCase()
                .catch { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to load users"
                        )
                    }
                }
                .collect { users ->
                    _uiState.update {
                        it.copy(
                            users = filterUsers(users, it.searchQuery),
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { currentState ->
            currentState.copy(
                searchQuery = query,
                users = filterUsers(
                    getAllUsersFromState(),
                    query
                )
            )
        }
    }

    fun deleteUser(userId: Long) {
        viewModelScope.launch {
            deleteUserUseCase(userId)
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(error = exception.message ?: "Failed to delete user")
                    }
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun filterUsers(users: List<com.example.kmpcleanarch.domain.model.User>, query: String): List<com.example.kmpcleanarch.domain.model.User> {
        if (query.isBlank()) return users

        return users.filter { user ->
            user.name.contains(query, ignoreCase = true) ||
            user.email.contains(query, ignoreCase = true) ||
            user.role.contains(query, ignoreCase = true)
        }
    }

    private fun getAllUsersFromState(): List<com.example.kmpcleanarch.domain.model.User> {
        // In a real app, you might want to cache the unfiltered list
        return _uiState.value.users
    }
}
