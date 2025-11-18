package com.example.kmpcleanarch.domain.usecase

import com.example.kmpcleanarch.domain.model.User
import com.example.kmpcleanarch.domain.repository.UserRepository

/**
 * Use case for saving (create/update) a user
 */
class SaveUserUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        id: Long = 0,
        email: String,
        name: String,
        role: String,
        avatarUrl: String? = null
    ): Result<Long> {
        // Validation
        if (email.isBlank()) {
            return Result.failure(IllegalArgumentException("Email is required"))
        }

        if (!isValidEmail(email)) {
            return Result.failure(IllegalArgumentException("Invalid email format"))
        }

        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("Name is required"))
        }

        if (name.length < 2) {
            return Result.failure(IllegalArgumentException("Name must be at least 2 characters"))
        }

        if (role.isBlank()) {
            return Result.failure(IllegalArgumentException("Role is required"))
        }

        val validRoles = listOf("admin", "user", "moderator")
        if (role !in validRoles) {
            return Result.failure(IllegalArgumentException("Invalid role. Must be one of: ${validRoles.joinToString()}"))
        }

        return try {
            val user = User(
                id = id,
                email = email.trim(),
                name = name.trim(),
                role = role.trim().lowercase(),
                avatarUrl = avatarUrl?.trim()
            )

            val userId = userRepository.saveUser(user)
            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return email.matches(emailRegex)
    }
}
