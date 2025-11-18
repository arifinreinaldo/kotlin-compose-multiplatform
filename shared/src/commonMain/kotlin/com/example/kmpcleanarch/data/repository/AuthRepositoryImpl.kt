package com.example.kmpcleanarch.data.repository

import com.example.kmpcleanarch.domain.model.AuthState
import com.example.kmpcleanarch.domain.model.User
import com.example.kmpcleanarch.domain.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Sample implementation of AuthRepository
 * In production, replace with actual API calls and token storage
 */
class AuthRepositoryImpl : AuthRepository {

    private val _authState = MutableStateFlow(AuthState.unauthenticated())

    override suspend fun login(email: String, password: String): Result<AuthState> {
        return try {
            // Simulate network delay
            delay(1000)

            // Sample authentication - In production, call actual API
            // For demo: accept any email with password "password123"
            if (password == "password123") {
                val user = User(
                    id = 1,
                    email = email,
                    name = extractNameFromEmail(email),
                    role = if (email.contains("admin")) "admin" else "user",
                    avatarUrl = null
                )

                val authState = AuthState.authenticated(
                    user = user,
                    token = "sample_token_${System.currentTimeMillis()}"
                )

                _authState.value = authState
                Result.success(authState)
            } else {
                Result.failure(Exception("Invalid credentials"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            delay(500)
            _authState.value = AuthState.unauthenticated()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeAuthState(): Flow<AuthState> {
        return _authState.asStateFlow()
    }

    override suspend fun getCurrentUser(): User? {
        return _authState.value.user
    }

    override suspend fun isAuthenticated(): Boolean {
        return _authState.value.isAuthenticated
    }

    private fun extractNameFromEmail(email: String): String {
        return email.substringBefore("@")
            .split(".")
            .joinToString(" ") { it.capitalize() }
    }

    private fun String.capitalize(): String {
        return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}
