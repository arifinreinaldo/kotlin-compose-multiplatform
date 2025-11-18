package com.example.kmpcleanarch.data.repository

import com.example.kmpcleanarch.domain.model.User
import com.example.kmpcleanarch.domain.repository.UserRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/**
 * Sample implementation of UserRepository using in-memory storage
 * In production, replace with SQLDelight database or network + database
 */
class UserRepositoryImpl : UserRepository {

    private val _users = MutableStateFlow(generateSampleUsers())

    override fun observeAllUsers(): Flow<List<User>> {
        return _users.asStateFlow()
    }

    override suspend fun getAllUsers(): List<User> {
        delay(500) // Simulate database query
        return _users.value
    }

    override suspend fun getUserById(id: Long): User? {
        delay(300)
        return _users.value.find { it.id == id }
    }

    override suspend fun saveUser(user: User): Long {
        delay(500) // Simulate database operation

        val users = _users.value.toMutableList()

        if (user.id == 0L) {
            // Insert new user
            val newId = (users.maxOfOrNull { it.id } ?: 0) + 1
            val newUser = user.copy(id = newId)
            users.add(newUser)
            _users.value = users
            return newId
        } else {
            // Update existing user
            val index = users.indexOfFirst { it.id == user.id }
            if (index != -1) {
                users[index] = user
                _users.value = users
            }
            return user.id
        }
    }

    override suspend fun deleteUser(id: Long) {
        delay(300)
        _users.value = _users.value.filter { it.id != id }
    }

    override suspend fun searchUsers(query: String): List<User> {
        delay(200)
        return _users.value.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.email.contains(query, ignoreCase = true)
        }
    }

    private fun generateSampleUsers(): List<User> {
        return listOf(
            User(
                id = 1,
                email = "john.doe@example.com",
                name = "John Doe",
                role = "admin",
                avatarUrl = null,
                createdAt = System.currentTimeMillis() - 86400000 * 30
            ),
            User(
                id = 2,
                email = "jane.smith@example.com",
                name = "Jane Smith",
                role = "user",
                avatarUrl = null,
                createdAt = System.currentTimeMillis() - 86400000 * 20
            ),
            User(
                id = 3,
                email = "bob.wilson@example.com",
                name = "Bob Wilson",
                role = "moderator",
                avatarUrl = null,
                createdAt = System.currentTimeMillis() - 86400000 * 10
            ),
            User(
                id = 4,
                email = "alice.brown@example.com",
                name = "Alice Brown",
                role = "user",
                avatarUrl = null,
                createdAt = System.currentTimeMillis() - 86400000 * 5
            ),
            User(
                id = 5,
                email = "charlie.davis@example.com",
                name = "Charlie Davis",
                role = "user",
                avatarUrl = null,
                createdAt = System.currentTimeMillis() - 86400000 * 2
            )
        )
    }
}
