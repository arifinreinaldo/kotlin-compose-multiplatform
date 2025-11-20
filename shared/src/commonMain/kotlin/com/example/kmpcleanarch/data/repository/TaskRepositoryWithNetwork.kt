package com.example.kmpcleanarch.data.repository

import com.example.kmpcleanarch.data.mapper.toDomainModel
import com.example.kmpcleanarch.data.mapper.toDto
import com.example.kmpcleanarch.data.remote.api.TaskApi
import com.example.kmpcleanarch.database.AppDatabase
import com.example.kmpcleanarch.domain.model.Task
import com.example.kmpcleanarch.domain.repository.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * EXTENSION POINT: Example repository implementation with network layer
 *
 * This demonstrates the pattern for adding remote API support:
 * 1. Local database remains the single source of truth
 * 2. Network calls update the local database
 * 3. UI observes the local database through Flow
 *
 * To use this implementation:
 * 1. Implement TaskApi interface with your actual API calls
 * 2. Update Koin module to use this repository instead of TaskRepositoryImpl
 * 3. Handle sync conflicts and offline scenarios
 *
 * Example sync strategy:
 * - On app start: Fetch from API and update local DB
 * - On create/update: Save locally first, then sync to API in background
 * - On conflict: Last-write-wins or show conflict resolution UI
 */
class TaskRepositoryWithNetwork(
    private val database: AppDatabase,
    private val taskApi: TaskApi
) : TaskRepository {

    private val baseRepository = TaskRepositoryImpl(database)

    override fun observeAllTasks(): Flow<List<Task>> {
        // Return local database flow - single source of truth
        return baseRepository.observeAllTasks()
    }

    override fun observeTaskById(id: Long): Flow<Task?> {
        return baseRepository.observeTaskById(id)
    }

    override suspend fun getAllTasks(): List<Task> {
        // Try to sync from network first
        syncTasksFromNetwork()
        // Return from local database
        return baseRepository.getAllTasks()
    }

    override suspend fun getTaskById(id: Long): Task? {
        return baseRepository.getTaskById(id)
    }

    override suspend fun insertTask(task: Task): Long = withContext(Dispatchers.Default) {
        // Insert locally first
        val localId = baseRepository.insertTask(task)

        // Sync to network in background (fire and forget)
        // In production, use WorkManager or similar for reliability
        try {
            val taskWithId = task.copy(id = localId)
            taskApi.createTask(taskWithId.toDto())
        } catch (e: Exception) {
            // Handle network error - maybe queue for retry
            println("Failed to sync task to network: ${e.message}")
        }

        localId
    }

    override suspend fun updateTask(task: Task): Unit = withContext(Dispatchers.Default) {
        // Update locally first
        baseRepository.updateTask(task)

        // Sync to network in background
        try {
            taskApi.updateTask(task.id.toString(), task.toDto())
        } catch (e: Exception) {
            // Handle network error - maybe queue for retry
            println("Failed to sync update to network: ${e.message}")
        }
    }

    override suspend fun deleteTask(id: Long): Unit = withContext(Dispatchers.Default) {
        // Delete locally first
        baseRepository.deleteTask(id)

        // Sync deletion to network
        try {
            taskApi.deleteTask(id.toString())
        } catch (e: Exception) {
            // Handle network error - maybe queue for retry
            println("Failed to sync deletion to network: ${e.message}")
        }
    }

    override suspend fun deleteAllTasks() {
        baseRepository.deleteAllTasks()
    }

    /**
     * Sync tasks from network to local database
     * This is the cache update mechanism
     */
    private suspend fun syncTasksFromNetwork() {
        try {
            val result = taskApi.getTasks(page = 1, pageSize = 100)
            result.onSuccess { paginatedResponse ->
                // Convert DTOs to domain models
                val tasks = paginatedResponse.items.map { it.toDomainModel() }

                // Update local database (you may want to implement smarter merging)
                // For now, this is a simple example
                tasks.forEach { task ->
                    val existingTask = baseRepository.getTaskById(task.id)
                    if (existingTask == null) {
                        baseRepository.insertTask(task)
                    } else {
                        // Merge strategy: prefer server data for synced items
                        baseRepository.updateTask(task)
                    }
                }
            }
        } catch (e: Exception) {
            // Network error - continue with cached data
            println("Failed to sync from network: ${e.message}")
        }
    }
}
