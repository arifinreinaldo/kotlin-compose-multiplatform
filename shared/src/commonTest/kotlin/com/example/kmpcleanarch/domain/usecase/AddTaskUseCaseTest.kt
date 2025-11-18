package com.example.kmpcleanarch.domain.usecase

import com.example.kmpcleanarch.domain.model.Task
import com.example.kmpcleanarch.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Example unit test for use case
 * Tests business logic without dependencies on framework or database
 */
class AddTaskUseCaseTest {

    @Test
    fun `should add task successfully with valid title`() = runTest {
        // Given
        val fakeRepository = FakeTaskRepository()
        val useCase = AddTaskUseCase(fakeRepository)
        val title = "Test Task"
        val description = "Test Description"

        // When
        val result = useCase(title, description)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(1, fakeRepository.insertedTasks.size)
        assertEquals(title, fakeRepository.insertedTasks[0].title)
        assertEquals(description, fakeRepository.insertedTasks[0].description)
    }

    @Test
    fun `should fail when title is empty`() = runTest {
        // Given
        val fakeRepository = FakeTaskRepository()
        val useCase = AddTaskUseCase(fakeRepository)

        // When
        val result = useCase("", "Description")

        // Then
        assertTrue(result.isFailure)
        assertEquals(0, fakeRepository.insertedTasks.size)
    }

    @Test
    fun `should fail when title is blank`() = runTest {
        // Given
        val fakeRepository = FakeTaskRepository()
        val useCase = AddTaskUseCase(fakeRepository)

        // When
        val result = useCase("   ", "Description")

        // Then
        assertTrue(result.isFailure)
        assertEquals(0, fakeRepository.insertedTasks.size)
    }

    @Test
    fun `should trim whitespace from title and description`() = runTest {
        // Given
        val fakeRepository = FakeTaskRepository()
        val useCase = AddTaskUseCase(fakeRepository)

        // When
        val result = useCase("  Test  ", "  Description  ")

        // Then
        assertTrue(result.isSuccess)
        assertEquals("Test", fakeRepository.insertedTasks[0].title)
        assertEquals("Description", fakeRepository.insertedTasks[0].description)
    }
}

/**
 * Fake repository for testing
 * No database required, pure in-memory implementation
 */
class FakeTaskRepository : TaskRepository {
    val insertedTasks = mutableListOf<Task>()
    val tasks = mutableListOf<Task>()

    override fun observeAllTasks(): Flow<List<Task>> = flowOf(tasks)

    override fun observeTaskById(id: Long): Flow<Task?> {
        return flowOf(tasks.find { it.id == id })
    }

    override suspend fun getAllTasks(): List<Task> = tasks

    override suspend fun getTaskById(id: Long): Task? {
        return tasks.find { it.id == id }
    }

    override suspend fun insertTask(task: Task): Long {
        insertedTasks.add(task)
        val newTask = task.copy(id = (tasks.maxOfOrNull { it.id } ?: 0) + 1)
        tasks.add(newTask)
        return newTask.id
    }

    override suspend fun updateTask(task: Task) {
        val index = tasks.indexOfFirst { it.id == task.id }
        if (index != -1) {
            tasks[index] = task
        }
    }

    override suspend fun deleteTask(id: Long) {
        tasks.removeIf { it.id == id }
    }

    override suspend fun deleteAllTasks() {
        tasks.clear()
    }
}
