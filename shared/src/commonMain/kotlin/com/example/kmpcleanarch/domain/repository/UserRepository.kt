package com.example.kmpcleanarch.domain.repository

import com.example.kmpcleanarch.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for User operations
 */
interface UserRepository {
    /**
     * Observe all users
     */
    fun observeAllUsers(): Flow<List<User>>

    /**
     * Get all users
     */
    suspend fun getAllUsers(): List<User>

    /**
     * Get user by ID
     */
    suspend fun getUserById(id: Long): User?

    /**
     * Insert or update user
     */
    suspend fun saveUser(user: User): Long

    /**
     * Delete user
     */
    suspend fun deleteUser(id: Long)

    /**
     * Search users by name or email
     */
    suspend fun searchUsers(query: String): List<User>
}
