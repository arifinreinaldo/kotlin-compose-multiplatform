# Testing Guide

This guide explains how to test your Kotlin Compose Multiplatform application.

## Table of Contents

1. [Testing Strategy](#testing-strategy)
2. [Testing Each Layer](#testing-each-layer)
3. [Running Tests](#running-tests)
4. [Testing Tools](#testing-tools)
5. [Best Practices](#best-practices)

## Testing Strategy

The Clean Architecture makes testing easy by separating concerns:

```
┌─────────────────────────────────────────────────────────┐
│  UI Tests (Optional)                                    │
│  - End-to-end flows                                     │
│  - User interactions                                    │
│  Tools: Compose Testing, Screenshot tests               │
└─────────────────────────────────────────────────────────┘
                         ▼
┌─────────────────────────────────────────────────────────┐
│  ViewModel Tests                                        │
│  - State management                                     │
│  - User event handling                                  │
│  Tools: Turbine for Flow testing                       │
└─────────────────────────────────────────────────────────┘
                         ▼
┌─────────────────────────────────────────────────────────┐
│  Use Case Tests                                         │
│  - Business logic                                       │
│  - Validation rules                                     │
│  Tools: Kotlin Test, Fake repositories                 │
└─────────────────────────────────────────────────────────┘
                         ▼
┌─────────────────────────────────────────────────────────┐
│  Repository Tests                                       │
│  - Data operations                                      │
│  - Mapping logic                                        │
│  Tools: In-memory database, MockK                      │
└─────────────────────────────────────────────────────────┘
```

## Testing Each Layer

### 1. Domain Layer Tests

**What to test:**
- Entity business logic
- Use case validation
- Use case orchestration

**Tools:**
- `kotlin.test`
- Fake repositories (no mocking needed!)

**Example: Testing Entity Logic**

```kotlin
// TaskTest.kt
class TaskTest {

    @Test
    fun `should mark task as completed`() {
        // Given
        val task = Task(
            id = 1,
            title = "Test",
            description = "Test",
            isCompleted = false
        )

        // When
        val completedTask = task.markAsCompleted()

        // Then
        assertTrue(completedTask.isCompleted)
        assertTrue(completedTask.updatedAt > task.updatedAt)
    }

    @Test
    fun `should preserve other fields when marking as completed`() {
        // Given
        val task = Task(id = 123, title = "Important", description = "Very important")

        // When
        val result = task.markAsCompleted()

        // Then
        assertEquals(123, result.id)
        assertEquals("Important", result.title)
        assertEquals("Very important", result.description)
    }
}
```

**Example: Testing Use Case**

```kotlin
// AddTaskUseCaseTest.kt
class AddTaskUseCaseTest {

    @Test
    fun `should add task successfully with valid title`() = runTest {
        // Given
        val fakeRepository = FakeTaskRepository()
        val useCase = AddTaskUseCase(fakeRepository)

        // When
        val result = useCase("Test Task", "Description")

        // Then
        assertTrue(result.isSuccess)
        assertEquals(1, fakeRepository.insertedTasks.size)
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
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `should trim whitespace from inputs`() = runTest {
        // Given
        val fakeRepository = FakeTaskRepository()
        val useCase = AddTaskUseCase(fakeRepository)

        // When
        useCase("  Test  ", "  Description  ")

        // Then
        assertEquals("Test", fakeRepository.insertedTasks[0].title)
        assertEquals("Description", fakeRepository.insertedTasks[0].description)
    }
}

// Fake repository - simple in-memory implementation
class FakeTaskRepository : TaskRepository {
    val insertedTasks = mutableListOf<Task>()
    private val tasks = mutableListOf<Task>()

    override suspend fun insertTask(task: Task): Long {
        insertedTasks.add(task)
        val newTask = task.copy(id = tasks.size + 1L)
        tasks.add(newTask)
        return newTask.id
    }

    override fun observeAllTasks(): Flow<List<Task>> = flowOf(tasks)
    // ... other methods
}
```

### 2. Data Layer Tests

**What to test:**
- Repository implementations
- Data source operations
- Mapper logic

**Tools:**
- In-memory SQLite driver
- MockK for API mocking

**Example: Testing Repository**

```kotlin
// TaskRepositoryTest.kt
class TaskRepositoryImplTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: TaskRepositoryImpl

    @BeforeTest
    fun setup() {
        // Use in-memory database for testing
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        AppDatabase.Schema.create(driver)
        database = AppDatabase(driver)
        repository = TaskRepositoryImpl(database)
    }

    @AfterTest
    fun teardown() {
        database.close()
    }

    @Test
    fun `should insert and retrieve task`() = runTest {
        // Given
        val task = Task(title = "Test", description = "Description")

        // When
        val id = repository.insertTask(task)
        val retrieved = repository.getTaskById(id)

        // Then
        assertNotNull(retrieved)
        assertEquals("Test", retrieved.title)
        assertEquals("Description", retrieved.description)
    }

    @Test
    fun `should update task`() = runTest {
        // Given
        val task = Task(title = "Original", description = "Original")
        val id = repository.insertTask(task)

        // When
        val updated = task.copy(id = id, title = "Updated")
        repository.updateTask(updated)
        val retrieved = repository.getTaskById(id)

        // Then
        assertEquals("Updated", retrieved?.title)
    }

    @Test
    fun `should observe tasks reactively`() = runTest {
        // Given
        repository.insertTask(Task(title = "Task 1", description = "Desc 1"))

        // When & Then
        repository.observeAllTasks().test {
            val tasks = awaitItem()
            assertEquals(1, tasks.size)
            assertEquals("Task 1", tasks[0].title)

            // Insert another task
            repository.insertTask(Task(title = "Task 2", description = "Desc 2"))

            val updatedTasks = awaitItem()
            assertEquals(2, updatedTasks.size)
        }
    }
}
```

**Example: Testing Mapper**

```kotlin
// TaskMapperTest.kt
class TaskMapperTest {

    @Test
    fun `should map entity to domain model`() {
        // Given
        val entity = TaskEntity(
            id = 1,
            title = "Test",
            description = "Description",
            isCompleted = true,
            createdAt = 1000L,
            updatedAt = 2000L
        )

        // When
        val domain = entity.toDomainModel()

        // Then
        assertEquals(1, domain.id)
        assertEquals("Test", domain.title)
        assertEquals("Description", domain.description)
        assertTrue(domain.isCompleted)
        assertEquals(1000L, domain.createdAt)
        assertEquals(2000L, domain.updatedAt)
    }

    @Test
    fun `should map domain model to entity`() {
        // Given
        val domain = Task(
            id = 1,
            title = "Test",
            description = "Description",
            isCompleted = true,
            createdAt = 1000L,
            updatedAt = 2000L
        )

        // When
        val entity = domain.toEntity()

        // Then
        assertEquals(1, entity.id)
        assertEquals("Test", entity.title)
        assertTrue(entity.isCompleted)
    }
}
```

### 3. Presentation Layer Tests

**What to test:**
- ViewModel state management
- User event handling
- State transformations

**Tools:**
- Coroutine Test
- Turbine (for Flow testing)
- Fake use cases

**Example: Testing ViewModel**

```kotlin
// TaskViewModelTest.kt
@OptIn(ExperimentalCoroutinesApi::class)
class TaskViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeTaskRepository
    private lateinit var viewModel: TaskViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeTaskRepository()

        viewModel = TaskViewModel(
            getAllTasksUseCase = GetAllTasksUseCase(repository),
            addTaskUseCase = AddTaskUseCase(repository),
            toggleTaskCompletionUseCase = ToggleTaskCompletionUseCase(repository),
            deleteTaskUseCase = DeleteTaskUseCase(repository)
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `should show add dialog`() = runTest {
        // When
        viewModel.showAddDialog()

        // Then
        assertTrue(viewModel.uiState.value.showAddDialog)
    }

    @Test
    fun `should hide add dialog after successful task addition`() = runTest {
        viewModel.uiState.test {
            // Skip initial state
            awaitItem()

            // Show dialog
            viewModel.showAddDialog()
            assertTrue(awaitItem().showAddDialog)

            // Add task
            viewModel.addTask("New Task", "Description")
            testScheduler.advanceUntilIdle()

            // Dialog should be hidden
            val state = viewModel.uiState.value
            assertFalse(state.showAddDialog)
        }
    }

    @Test
    fun `should show error when adding task with empty title`() = runTest {
        // When
        viewModel.addTask("", "Description")
        testScheduler.advanceUntilIdle()

        // Then
        assertNotNull(viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.error!!.contains("Title"))
    }

    @Test
    fun `should load tasks on initialization`() = runTest {
        // Given
        repository.insertTask(Task(title = "Task 1", description = "Desc 1"))
        repository.insertTask(Task(title = "Task 2", description = "Desc 2"))

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

    @Test
    fun `should clear error`() = runTest {
        // Given
        viewModel.addTask("", "Description")
        testScheduler.advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.error)

        // When
        viewModel.clearError()

        // Then
        assertNull(viewModel.uiState.value.error)
    }
}
```

### 4. UI Tests (Optional)

**What to test:**
- User interactions
- Navigation flows
- Visual regressions

**Example: Compose UI Test**

```kotlin
// TaskListScreenTest.kt
class TaskListScreenTest {

    @Test
    fun `should display tasks`() {
        composeTestRule.setContent {
            val fakeViewModel = FakeTaskViewModel()
            TaskListScreen(
                windowSizeClass = WindowSizeClass.COMPACT,
                viewModel = fakeViewModel
            )
        }

        composeTestRule.onNodeWithText("Task 1").assertExists()
        composeTestRule.onNodeWithText("Task 2").assertExists()
    }

    @Test
    fun `should show add dialog when FAB is clicked`() {
        composeTestRule.setContent {
            TaskListScreen(windowSizeClass = WindowSizeClass.COMPACT)
        }

        composeTestRule.onNodeWithText("+").performClick()
        composeTestRule.onNodeWithText("Add New Task").assertExists()
    }
}
```

## Running Tests

### Run All Tests

```bash
# Common tests (run on all platforms)
./gradlew :shared:allTests

# Android tests
./gradlew :shared:testDebugUnitTest
./gradlew :androidApp:testDebugUnitTest

# Desktop tests
./gradlew :shared:desktopTest

# iOS tests
./gradlew :shared:iosSimulatorArm64Test
```

### Run Specific Test

```bash
./gradlew :shared:testDebugUnitTest --tests TaskViewModelTest
```

### Generate Coverage Report

```bash
./gradlew :shared:testDebugUnitTestCoverage
```

## Testing Tools

### Dependencies (Already Included)

```kotlin
commonTest {
    implementation(kotlin("test"))                            // Kotlin Test
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test") // Coroutine Test
    implementation("io.insert-koin:koin-test")               // Koin Test
    implementation("app.cash.turbine:turbine")               // Flow testing
}

androidUnitTest {
    implementation("junit:junit:4.13.2")                     // JUnit
    implementation("io.mockk:mockk:1.13.8")                  // Mocking
    implementation("org.robolectric:robolectric:4.11.1")     // Android Test
}
```

### Key Libraries

**1. Turbine - Flow Testing**
```kotlin
flow.test {
    assertEquals(expected, awaitItem())
    awaitComplete()
}
```

**2. Coroutine Test - Async Testing**
```kotlin
@Test
fun test() = runTest {
    // Coroutines run immediately
    val result = suspendFunction()
    assertEquals(expected, result)
}
```

**3. MockK - Mocking**
```kotlin
val mockApi = mockk<TaskApi>()
coEvery { mockApi.getTasks() } returns Result.success(emptyList())
```

## Best Practices

### 1. Test Naming

Use descriptive names:
```kotlin
// ❌ Bad
@Test
fun test1() { }

// ✅ Good
@Test
fun `should add task successfully with valid title`() { }
```

### 2. AAA Pattern

Arrange, Act, Assert:
```kotlin
@Test
fun test() {
    // Given (Arrange)
    val task = Task(...)

    // When (Act)
    val result = task.markAsCompleted()

    // Then (Assert)
    assertTrue(result.isCompleted)
}
```

### 3. Test One Thing

```kotlin
// ❌ Bad - testing multiple things
@Test
fun `test task operations`() {
    repository.insertTask(task)
    repository.updateTask(task)
    repository.deleteTask(task.id)
}

// ✅ Good - one test per operation
@Test
fun `should insert task`() { }

@Test
fun `should update task`() { }

@Test
fun `should delete task`() { }
```

### 4. Use Fakes Over Mocks

```kotlin
// ✅ Prefer fakes (simple implementations)
class FakeTaskRepository : TaskRepository {
    private val tasks = mutableListOf<Task>()

    override suspend fun insertTask(task: Task): Long {
        tasks.add(task)
        return task.id
    }
}

// ❌ Avoid mocks when possible
val mockRepo = mockk<TaskRepository>()
coEvery { mockRepo.insertTask(any()) } returns 1L
```

### 5. Test Edge Cases

```kotlin
@Test
fun `should handle empty title`() { }

@Test
fun `should handle very long title`() { }

@Test
fun `should handle special characters`() { }

@Test
fun `should handle concurrent updates`() { }
```

### 6. Keep Tests Fast

- Use in-memory databases
- Avoid Thread.sleep()
- Use testDispatcher for coroutines
- Mock expensive operations

### 7. Clean Up Resources

```kotlin
@AfterTest
fun tearDown() {
    database.close()
    Dispatchers.resetMain()
}
```

## Test Coverage Goals

Aim for:
- **Domain Layer**: 100% (easy, no external dependencies)
- **Data Layer**: 80%+ (test repositories and mappers)
- **Presentation Layer**: 70%+ (test ViewModels)
- **UI Layer**: 50%+ (critical user flows)

## Continuous Integration

Example GitHub Actions:

```yaml
name: Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - uses: actions/setup-java@v2
        with:
          java-version: '17'
      - name: Run tests
        run: ./gradlew :shared:allTests
      - name: Upload coverage
        run: ./gradlew :shared:jacocoTestReport
```

## Summary

✅ Test each layer independently
✅ Use fakes over mocks when possible
✅ Test business logic thoroughly
✅ Keep tests fast and reliable
✅ Follow AAA pattern
✅ Use descriptive test names
✅ Aim for high domain layer coverage

The boilerplate includes example tests for each layer - use them as templates!
