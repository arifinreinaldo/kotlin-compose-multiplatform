package com.example.kmpcleanarch.domain.model

/**
 * Domain entity representing a Task
 * This is the core business model, independent of any framework or database
 */
data class Task(
    val id: Long = 0,
    val title: String,
    val description: String,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun markAsCompleted(): Task = copy(
        isCompleted = true,
        updatedAt = System.currentTimeMillis()
    )

    fun markAsIncomplete(): Task = copy(
        isCompleted = false,
        updatedAt = System.currentTimeMillis()
    )
}
