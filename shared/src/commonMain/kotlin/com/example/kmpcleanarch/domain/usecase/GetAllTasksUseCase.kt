package com.example.kmpcleanarch.domain.usecase

import com.example.kmpcleanarch.domain.model.Task
import com.example.kmpcleanarch.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for observing all tasks
 * Encapsulates the business logic for retrieving tasks
 */
class GetAllTasksUseCase(
    private val taskRepository: TaskRepository
) {
    operator fun invoke(): Flow<List<Task>> {
        return taskRepository.observeAllTasks()
    }
}
