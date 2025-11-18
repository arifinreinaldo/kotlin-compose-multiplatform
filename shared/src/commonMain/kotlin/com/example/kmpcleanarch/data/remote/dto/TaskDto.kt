package com.example.kmpcleanarch.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for Task
 * Used for network communication (API requests/responses)
 *
 * Extension point: Add this when you implement a remote API
 */
@Serializable
data class TaskDto(
    val id: Long = 0,
    val title: String,
    val description: String,
    val isCompleted: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
    // Add server-specific fields here
    val serverId: String? = null,
    val syncedAt: Long? = null
)

/**
 * Example response wrapper from API
 */
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val errorCode: String? = null
)

/**
 * Example paginated response
 */
@Serializable
data class PaginatedResponse<T>(
    val items: List<T>,
    val page: Int,
    val pageSize: Int,
    val totalItems: Int,
    val totalPages: Int
)
