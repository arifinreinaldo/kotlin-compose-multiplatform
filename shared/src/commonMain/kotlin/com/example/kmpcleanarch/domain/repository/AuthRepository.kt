package com.example.kmpcleanarch.domain.repository

import com.example.kmpcleanarch.domain.model.AuthState
import com.example.kmpcleanarch.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for authentication operations
 */
interface AuthRepository {
    /**
     * Login with email and password
     */
    suspend fun login(email: String, password: String): Result<AuthState>

    /**
     * Logout current user
     */
    suspend fun logout(): Result<Unit>

    /**
     * Get current authentication state
     */
    fun observeAuthState(): Flow<AuthState>

    /**
     * Get current authenticated user
     */
    suspend fun getCurrentUser(): User?

    /**
     * Check if user is authenticated
     */
    suspend fun isAuthenticated(): Boolean
}
