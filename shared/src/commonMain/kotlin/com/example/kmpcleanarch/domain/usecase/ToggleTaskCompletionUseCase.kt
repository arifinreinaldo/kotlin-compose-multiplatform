package com.example.kmpcleanarch.domain.usecase

import com.example.kmpcleanarch.domain.repository.TaskRepository

/**
 * Use case for toggling task completion status
 */
class ToggleTaskCompletionUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: Long): Result<Unit> {
        return try {
            val task = taskRepository.getTaskById(taskId)
                ?: return Result.failure(IllegalArgumentException("Task not found"))

            val updatedTask = if (task.isCompleted) {
                task.markAsIncomplete()
            } else {
                task.markAsCompleted()
            }

            taskRepository.updateTask(updatedTask)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
