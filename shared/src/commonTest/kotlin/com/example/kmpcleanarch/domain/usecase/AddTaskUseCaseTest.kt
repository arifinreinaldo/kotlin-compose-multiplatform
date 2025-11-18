package com.example.kmpcleanarch.domain.usecase

import com.example.kmpcleanarch.domain.model.Task
import com.example.kmpcleanarch.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AddTaskUseCaseTest {

    @Test
    fun `should reject empty title`() = runTest {
        val fakeRepository = FakeTaskRepository()
        val useCase = AddTaskUseCase(fakeRepository)

        val result = useCase("")

        assertTrue(result.isFailure)
        assertEquals("Task title cannot be empty", result.exceptionOrNull()?.message)
    }

    @Test
    fun `should reject blank title`() = runTest {
        val fakeRepository = FakeTaskRepository()
        val useCase = AddTaskUseCase(fakeRepository)

        val result = useCase("   ")

        assertTrue(result.isFailure)
        assertEquals("Task title cannot be empty", result.exceptionOrNull()?.message)
    }

    @Test
    fun `should add task successfully`() = runTest {
        val fakeRepository = FakeTaskRepository()
        val useCase = AddTaskUseCase(fakeRepository)

        val result = useCase("Buy groceries")

        assertTrue(result.isSuccess)
        assertEquals("Buy groceries", fakeRepository.lastTitle)
    }

    @Test
    fun `should trim whitespace from title`() = runTest {
        val fakeRepository = FakeTaskRepository()
        val useCase = AddTaskUseCase(fakeRepository)

        useCase("  Buy groceries  ")

        assertEquals("Buy groceries", fakeRepository.lastTitle)
    }

    @Test
    fun `should propagate repository errors`() = runTest {
        val fakeRepository = FakeTaskRepository(shouldFail = true)
        val useCase = AddTaskUseCase(fakeRepository)

        val result = useCase("Buy groceries")

        assertTrue(result.isFailure)
        assertEquals("Database error", result.exceptionOrNull()?.message)
    }
}

class ToggleTaskCompletionUseCaseTest {

    @Test
    fun `should toggle task completion`() = runTest {
        val fakeRepository = FakeTaskRepository()
        val useCase = ToggleTaskCompletionUseCase(fakeRepository)

        val task = Task(id = 1, title = "Test task", isCompleted = false)

        val result = useCase(task)

        assertTrue(result.isSuccess)
        assertEquals(1L, fakeRepository.lastTaskId)
        assertTrue(fakeRepository.lastCompletedState!!)
    }

    @Test
    fun `should propagate repository errors`() = runTest {
        val fakeRepository = FakeTaskRepository(shouldFail = true)
        val useCase = ToggleTaskCompletionUseCase(fakeRepository)

        val task = Task(id = 1, title = "Test task", isCompleted = false)

        val result = useCase(task)

        assertTrue(result.isFailure)
    }
}

class DeleteTaskUseCaseTest {

    @Test
    fun `should delete task successfully`() = runTest {
        val fakeRepository = FakeTaskRepository()
        val useCase = DeleteTaskUseCase(fakeRepository)

        val task = Task(id = 1, title = "Test task", isCompleted = false)

        val result = useCase(task)

        assertTrue(result.isSuccess)
        assertEquals(1L, fakeRepository.lastDeletedTaskId)
    }

    @Test
    fun `should propagate repository errors`() = runTest {
        val fakeRepository = FakeTaskRepository(shouldFail = true)
        val useCase = DeleteTaskUseCase(fakeRepository)

        val task = Task(id = 1, title = "Test task", isCompleted = false)

        val result = useCase(task)

        assertTrue(result.isFailure)
    }
}

/**
 * Fake implementation of TaskRepository for testing
 */
class FakeTaskRepository(
    private val shouldFail: Boolean = false
) : TaskRepository {

    var lastTitle: String? = null
    var lastTaskId: Long? = null
    var lastCompletedState: Boolean? = null
    var lastDeletedTaskId: Long? = null

    private val tasks = mutableListOf<Task>()

    override suspend fun insertTask(title: String): Result<Unit> {
        lastTitle = title
        return if (shouldFail) {
            Result.failure(Exception("Database error"))
        } else {
            tasks.add(Task(id = tasks.size.toLong() + 1, title = title, isCompleted = false))
            Result.success(Unit)
        }
    }

    override suspend fun updateTaskCompletion(taskId: Long, isCompleted: Boolean): Result<Unit> {
        lastTaskId = taskId
        lastCompletedState = isCompleted
        return if (shouldFail) {
            Result.failure(Exception("Database error"))
        } else {
            Result.success(Unit)
        }
    }

    override suspend fun deleteTask(taskId: Long): Result<Unit> {
        lastDeletedTaskId = taskId
        return if (shouldFail) {
            Result.failure(Exception("Database error"))
        } else {
            tasks.removeIf { it.id == taskId }
            Result.success(Unit)
        }
    }

    override fun getAllTasks(): Flow<List<Task>> {
        return flowOf(tasks)
    }

    override suspend fun getTaskById(id: Long): Task? {
        return tasks.find { it.id == id }
    }
}
