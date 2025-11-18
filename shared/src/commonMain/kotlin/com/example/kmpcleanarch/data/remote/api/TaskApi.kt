package com.example.kmpcleanarch.data.remote.api

import com.example.kmpcleanarch.data.remote.dto.ApiResponse
import com.example.kmpcleanarch.data.remote.dto.PaginatedResponse
import com.example.kmpcleanarch.data.remote.dto.TaskDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * API interface for Task operations
 * Extension point: Implement this when adding a remote backend
 *
 * Example usage:
 * ```
 * class TaskApiImpl(private val httpClient: HttpClient) : TaskApi {
 *     override suspend fun getTasks(page: Int, pageSize: Int): Result<PaginatedResponse<TaskDto>> {
 *         return try {
 *             val response = httpClient.get("/tasks") {
 *                 parameter("page", page)
 *                 parameter("pageSize", pageSize)
 *             }.body<ApiResponse<PaginatedResponse<TaskDto>>>()
 *
 *             if (response.success && response.data != null) {
 *                 Result.success(response.data)
 *             } else {
 *                 Result.failure(Exception(response.message ?: "Unknown error"))
 *             }
 *         } catch (e: Exception) {
 *             Result.failure(e)
 *         }
 *     }
 * }
 * ```
 */
interface TaskApi {

    /**
     * Fetch tasks from remote server
     */
    suspend fun getTasks(page: Int = 1, pageSize: Int = 20): Result<PaginatedResponse<TaskDto>>

    /**
     * Fetch a specific task by ID
     */
    suspend fun getTaskById(id: String): Result<TaskDto>

    /**
     * Create a new task on the server
     */
    suspend fun createTask(task: TaskDto): Result<TaskDto>

    /**
     * Update an existing task on the server
     */
    suspend fun updateTask(id: String, task: TaskDto): Result<TaskDto>

    /**
     * Delete a task from the server
     */
    suspend fun deleteTask(id: String): Result<Unit>

    /**
     * Sync local tasks with server
     */
    suspend fun syncTasks(localTasks: List<TaskDto>): Result<List<TaskDto>>
}

/**
 * Mock implementation for testing
 */
class MockTaskApi : TaskApi {
    override suspend fun getTasks(page: Int, pageSize: Int): Result<PaginatedResponse<TaskDto>> {
        // Return mock data for testing
        return Result.success(
            PaginatedResponse(
                items = emptyList(),
                page = page,
                pageSize = pageSize,
                totalItems = 0,
                totalPages = 0
            )
        )
    }

    override suspend fun getTaskById(id: String): Result<TaskDto> {
        return Result.failure(NotImplementedError("Mock API - not implemented"))
    }

    override suspend fun createTask(task: TaskDto): Result<TaskDto> {
        return Result.success(task.copy(serverId = "mock-${System.currentTimeMillis()}"))
    }

    override suspend fun updateTask(id: String, task: TaskDto): Result<TaskDto> {
        return Result.success(task)
    }

    override suspend fun deleteTask(id: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun syncTasks(localTasks: List<TaskDto>): Result<List<TaskDto>> {
        return Result.success(localTasks)
    }
}
