package com.example.kmpcleanarch.data.mapper

import com.example.kmpcleanarch.data.remote.dto.TaskDto
import com.example.kmpcleanarch.domain.model.Task

/**
 * Mapper between network DTOs and domain models
 * Extension point: Use this when implementing remote API sync
 */

fun TaskDto.toDomainModel(): Task {
    return Task(
        id = id,
        title = title,
        description = description,
        isCompleted = isCompleted,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Task.toDto(serverId: String? = null): TaskDto {
    return TaskDto(
        id = id,
        title = title,
        description = description,
        isCompleted = isCompleted,
        createdAt = createdAt,
        updatedAt = updatedAt,
        serverId = serverId,
        syncedAt = System.currentTimeMillis()
    )
}

fun List<TaskDto>.toDomainModels(): List<Task> = map { it.toDomainModel() }

fun List<Task>.toDtos(): List<TaskDto> = map { it.toDto() }
