package com.example.kmpcleanarch.domain.usecase

import com.example.kmpcleanarch.domain.model.AuthState
import com.example.kmpcleanarch.domain.repository.AuthRepository

/**
 * Use case for user login
 */
class LoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<AuthState> {
        // Validate input
        if (email.isBlank()) {
            return Result.failure(IllegalArgumentException("Email is required"))
        }

        if (!isValidEmail(email)) {
            return Result.failure(IllegalArgumentException("Invalid email format"))
        }

        if (password.isBlank()) {
            return Result.failure(IllegalArgumentException("Password is required"))
        }

        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters"))
        }

        // Perform login
        return authRepository.login(email.trim(), password)
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return email.matches(emailRegex)
    }
}
