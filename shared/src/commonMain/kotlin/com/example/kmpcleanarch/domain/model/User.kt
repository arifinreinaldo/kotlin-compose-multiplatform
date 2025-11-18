package com.example.kmpcleanarch.domain.model

/**
 * Domain entity representing a User
 */
data class User(
    val id: Long = 0,
    val email: String,
    val name: String,
    val role: String,
    val avatarUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun isAdmin(): Boolean = role == "admin"

    fun getInitials(): String {
        return name.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .map { it.first().uppercase() }
            .joinToString("")
    }
}

/**
 * Authentication state
 */
data class AuthState(
    val user: User? = null,
    val token: String? = null,
    val isAuthenticated: Boolean = false
) {
    companion object {
        fun authenticated(user: User, token: String) = AuthState(
            user = user,
            token = token,
            isAuthenticated = true
        )

        fun unauthenticated() = AuthState(
            user = null,
            token = null,
            isAuthenticated = false
        )
    }
}
