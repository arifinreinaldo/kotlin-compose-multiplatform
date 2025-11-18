package com.example.kmpcleanarch.domain.repository

import com.example.kmpcleanarch.domain.model.Task
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Task operations
 * This belongs to the domain layer and defines the contract for data operations
 * Implementations are in the data layer
 */
interface TaskRepository {
    /**
     * Observe all tasks as a Flow (single source of truth from database)
     */
    fun observeAllTasks(): Flow<List<Task>>

    /**
     * Observe a specific task by ID
     */
    fun observeTaskById(id: Long): Flow<Task?>

    /**
     * Get all tasks (one-time query)
     */
    suspend fun getAllTasks(): List<Task>

    /**
     * Get a specific task by ID
     */
    suspend fun getTaskById(id: Long): Task?

    /**
     * Insert a new task
     */
    suspend fun insertTask(task: Task): Long

    /**
     * Update an existing task
     */
    suspend fun updateTask(task: Task)

    /**
     * Delete a task
     */
    suspend fun deleteTask(id: Long)

    /**
     * Delete all tasks
     */
    suspend fun deleteAllTasks()
}
