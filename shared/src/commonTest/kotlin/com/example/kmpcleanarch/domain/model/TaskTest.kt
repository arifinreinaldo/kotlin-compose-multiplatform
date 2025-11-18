package com.example.kmpcleanarch.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Example unit tests for domain entity
 * Tests business logic in the entity itself
 */
class TaskTest {

    @Test
    fun `should create task with default values`() {
        // Given & When
        val task = Task(
            title = "Test Task",
            description = "Test Description"
        )

        // Then
        assertEquals("Test Task", task.title)
        assertEquals("Test Description", task.description)
        assertFalse(task.isCompleted)
        assertEquals(0, task.id)
    }

    @Test
    fun `should mark task as completed`() {
        // Given
        val task = Task(
            id = 1,
            title = "Test",
            description = "Test",
            isCompleted = false,
            createdAt = 1000L,
            updatedAt = 1000L
        )

        // When
        val completedTask = task.markAsCompleted()

        // Then
        assertTrue(completedTask.isCompleted)
        assertTrue(completedTask.updatedAt > task.updatedAt)
    }

    @Test
    fun `should mark task as incomplete`() {
        // Given
        val task = Task(
            id = 1,
            title = "Test",
            description = "Test",
            isCompleted = true,
            createdAt = 1000L,
            updatedAt = 1000L
        )

        // When
        val incompleteTask = task.markAsIncomplete()

        // Then
        assertFalse(incompleteTask.isCompleted)
        assertTrue(incompleteTask.updatedAt > task.updatedAt)
    }

    @Test
    fun `should preserve other fields when marking as completed`() {
        // Given
        val task = Task(
            id = 123,
            title = "Important Task",
            description = "Very important",
            isCompleted = false,
            createdAt = 5000L,
            updatedAt = 6000L
        )

        // When
        val completedTask = task.markAsCompleted()

        // Then
        assertEquals(123, completedTask.id)
        assertEquals("Important Task", completedTask.title)
        assertEquals("Very important", completedTask.description)
        assertEquals(5000L, completedTask.createdAt)
    }
}
