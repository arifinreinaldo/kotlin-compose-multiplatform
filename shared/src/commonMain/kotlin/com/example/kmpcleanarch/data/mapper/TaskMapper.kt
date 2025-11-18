package com.example.kmpcleanarch.data.mapper

import com.example.kmpcleanarch.database.TaskEntity
import com.example.kmpcleanarch.domain.model.Task

/**
 * Mapper between database entities and domain models
 * Keeps the domain layer independent of the database implementation
 */
fun TaskEntity.toDomainModel(): Task {
    return Task(
        id = id,
        title = title,
        description = description,
        isCompleted = isCompleted,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Task.toEntity(): TaskEntity {
    return TaskEntity(
        id = id,
        title = title,
        description = description,
        isCompleted = isCompleted,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
