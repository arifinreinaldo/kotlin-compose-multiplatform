package com.example.kmpcleanarch.domain.usecase

import com.example.kmpcleanarch.domain.model.Task
import com.example.kmpcleanarch.domain.repository.TaskRepository

/**
 * Use case for adding a new task
 * Can include validation logic here
 */
class AddTaskUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(title: String, description: String): Result<Long> {
        return try {
            // Validation
            if (title.isBlank()) {
                return Result.failure(IllegalArgumentException("Title cannot be empty"))
            }

            val task = Task(
                title = title.trim(),
                description = description.trim()
            )

            val id = taskRepository.insertTask(task)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
