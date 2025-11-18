package com.example.kmpcleanarch.presentation.state

import com.example.kmpcleanarch.domain.model.User

/**
 * UI State for User List Screen
 */
data class UserListUiState(
    val users: List<User> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedUser: User? = null
)
