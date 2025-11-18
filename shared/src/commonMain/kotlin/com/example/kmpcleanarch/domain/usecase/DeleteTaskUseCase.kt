package com.example.kmpcleanarch.domain.usecase

import com.example.kmpcleanarch.domain.repository.TaskRepository

/**
 * Use case for deleting a task
 */
class DeleteTaskUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: Long): Result<Unit> {
        return try {
            taskRepository.deleteTask(taskId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
