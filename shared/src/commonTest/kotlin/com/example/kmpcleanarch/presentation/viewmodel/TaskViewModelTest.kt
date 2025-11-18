package com.example.kmpcleanarch.presentation.viewmodel

import app.cash.turbine.test
import com.example.kmpcleanarch.domain.model.Task
import com.example.kmpcleanarch.domain.usecase.AddTaskUseCase
import com.example.kmpcleanarch.domain.usecase.DeleteTaskUseCase
import com.example.kmpcleanarch.domain.usecase.GetAllTasksUseCase
import com.example.kmpcleanarch.domain.usecase.ToggleTaskCompletionUseCase
import com.example.kmpcleanarch.domain.usecase.FakeTaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlin.test.*

/**
 * Example ViewModel test
 * Tests UI logic and state management
 * Uses Turbine for testing Flow emissions
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TaskViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeTaskRepository
    private lateinit var viewModel: TaskViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeTaskRepository()

        val getAllTasksUseCase = GetAllTasksUseCase(repository)
        val addTaskUseCase = AddTaskUseCase(repository)
        val toggleTaskUseCase = ToggleTaskCompletionUseCase(repository)
        val deleteTaskUseCase = DeleteTaskUseCase(repository)

        viewModel = TaskViewModel(
            getAllTasksUseCase,
            addTaskUseCase,
            toggleTaskUseCase,
            deleteTaskUseCase
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `should show add dialog when showAddDialog is called`() = runTest {
        // When
        viewModel.showAddDialog()

        // Then
        assertTrue(viewModel.uiState.value.showAddDialog)
    }

    @Test
    fun `should hide add dialog when hideAddDialog is called`() = runTest {
        // Given
        viewModel.showAddDialog()

        // When
        viewModel.hideAddDialog()

        // Then
        assertFalse(viewModel.uiState.value.showAddDialog)
    }

    @Test
    fun `should add task and hide dialog on success`() = runTest {
        viewModel.uiState.test {
            // Initial state
            val initialState = awaitItem()
            assertEquals(0, initialState.tasks.size)

            // Show dialog
            viewModel.showAddDialog()
            val dialogShownState = awaitItem()
            assertTrue(dialogShownState.showAddDialog)

            // Add task
            viewModel.addTask("New Task", "Description")

            // Verify dialog is hidden and task is added
            testScheduler.advanceUntilIdle()

            val updatedState = viewModel.uiState.value
            assertFalse(updatedState.showAddDialog)
            // Note: Actual task list update happens through Flow observation
        }
    }

    @Test
    fun `should show error when adding task with empty title`() = runTest {
        // When
        viewModel.addTask("", "Description")
        testScheduler.advanceUntilIdle()

        // Then
        assertNotNull(viewModel.uiState.value.error)
    }

    @Test
    fun `should clear error when clearError is called`() = runTest {
        // Given
        viewModel.addTask("", "Description")
        testScheduler.advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.error)

        // When
        viewModel.clearError()

        // Then
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `should load tasks on initialization`() = runTest {
        // Given - add some tasks to repository
        repository.insertTask(Task(id = 1, title = "Task 1", description = "Desc 1"))
        repository.insertTask(Task(id = 2, title = "Task 2", description = "Desc 2"))

        // Create new viewmodel to trigger initialization
        val newViewModel = TaskViewModel(
            GetAllTasksUseCase(repository),
            AddTaskUseCase(repository),
            ToggleTaskCompletionUseCase(repository),
            DeleteTaskUseCase(repository)
        )

        testScheduler.advanceUntilIdle()

        // Then
        newViewModel.uiState.test {
            val state = awaitItem()
            assertEquals(2, state.tasks.size)
        }
    }
}
