package com.example.kmpcleanarch.presentation.state

/**
 * UI State for Login Screen
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isPasswordVisible: Boolean = false,
    val isLoginSuccessful: Boolean = false
)
