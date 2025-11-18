# Architecture Guide

This document explains the Clean Architecture implementation in this Kotlin Compose Multiplatform project.

## Table of Contents

1. [Overview](#overview)
2. [Layer Responsibilities](#layer-responsibilities)
3. [Data Flow](#data-flow)
4. [Dependency Rules](#dependency-rules)
5. [Extension Points](#extension-points)

## Overview

The project follows **Clean Architecture** principles with three main layers:

```
┌──────────────────────────────────────────────────────┐
│                 Presentation Layer                    │
│          (UI, ViewModels, State, Navigation)         │
│                                                       │
│  Dependencies: Compose, Voyager, Koin                │
└───────────────────┬──────────────────────────────────┘
                    │ Observes/Invokes
                    ▼
┌──────────────────────────────────────────────────────┐
│                   Domain Layer                        │
│        (Entities, Use Cases, Repo Interfaces)        │
│                                                       │
│  Dependencies: NONE (pure Kotlin)                    │
└───────────────────┬──────────────────────────────────┘
                    │ Implements
                    ▼
┌──────────────────────────────────────────────────────┐
│                    Data Layer                         │
│    (Repositories, Data Sources, DTOs, Mappers)       │
│                                                       │
│  Dependencies: SQLDelight, Ktor, Platform APIs       │
└──────────────────────────────────────────────────────┘
```

## Layer Responsibilities

### 1. Domain Layer

**Location:** `shared/src/commonMain/kotlin/.../domain/`

**Purpose:** Contains business logic and entities

**Components:**
- **Entities** (`model/`): Core business objects (e.g., `Task`)
  - Pure Kotlin classes
  - No framework dependencies
  - Business logic methods (e.g., `markAsCompleted()`)

- **Use Cases** (`usecase/`): Single-responsibility business operations
  - One use case = one business action
  - Example: `AddTaskUseCase`, `ToggleTaskCompletionUseCase`
  - Can include validation logic
  - Returns `Result<T>` for error handling

- **Repository Interfaces** (`repository/`): Data operation contracts
  - Define what data operations are needed
  - No implementation details
  - Return domain models, not DTOs

**Dependencies:** NONE - This layer is completely independent

**Testing:** Easy - pure Kotlin, no mocking needed for entities

```kotlin
// Example Use Case
class AddTaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(title: String, description: String): Result<Long> {
        // Validation logic here
        if (title.isBlank()) {
            return Result.failure(IllegalArgumentException("Title cannot be empty"))
        }

        val task = Task(title = title.trim(), description = description.trim())
        return try {
            val id = repository.insertTask(task)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### 2. Data Layer

**Location:** `shared/src/commonMain/kotlin/.../data/`

**Purpose:** Handles data operations and implements repository interfaces

**Components:**
- **Repository Implementations** (`repository/`): Concrete implementations
  - Implements domain repository interfaces
  - Coordinates between local and remote data sources
  - Handles caching strategy

- **Local Data Sources** (`local/`): Database operations
  - SQLDelight queries and drivers
  - Platform-specific drivers (Android, iOS, Desktop)
  - Type-safe SQL

- **Remote Data Sources** (`remote/`): API operations
  - Ktor HTTP client
  - API interfaces
  - DTOs for network communication

- **Mappers** (`mapper/`): Convert between layers
  - `Entity ↔ Domain Model`
  - `DTO ↔ Domain Model`
  - Keep domain layer clean

**Dependencies:** SQLDelight, Ktor, Kotlinx Serialization

**Testing:** Use in-memory databases or mock APIs

```kotlin
// Example Repository Implementation
class TaskRepositoryImpl(private val database: AppDatabase) : TaskRepository {
    override fun observeAllTasks(): Flow<List<Task>> {
        return database.taskQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { entities -> entities.map { it.toDomainModel() } }
    }

    override suspend fun insertTask(task: Task): Long {
        database.taskQueries.insert(...)
        return database.taskQueries.lastInsertRowId().executeAsOne()
    }
}
```

### 3. Presentation Layer

**Location:** `shared/src/commonMain/kotlin/.../presentation/`

**Purpose:** Handles UI logic and user interactions

**Components:**
- **ViewModels** (`viewmodel/`): State management and UI logic
  - Hold UI state
  - Handle user events
  - Call use cases
  - Expose state as `StateFlow`

- **UI State** (`state/`): Immutable UI state classes
  - Represent UI at any given moment
  - Data classes with all UI needs

- **Screens** (`ui/screen/`): Full-screen composables
  - Observe ViewModel state
  - Handle user input
  - Responsive to window size

- **Components** (`ui/component/`): Reusable UI pieces
  - Buttons, cards, dialogs
  - Accept data as parameters
  - Stateless when possible

- **Navigation** (`navigation/`): Screen navigation
  - Voyager screens
  - Navigation extensions
  - Deep linking (future)

**Dependencies:** Compose Multiplatform, Voyager, Koin, Lifecycle

**Testing:** Test ViewModels with fake use cases

```kotlin
// Example ViewModel
class TaskViewModel(
    private val getAllTasksUseCase: GetAllTasksUseCase,
    private val addTaskUseCase: AddTaskUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

    fun addTask(title: String, description: String) {
        viewModelScope.launch {
            addTaskUseCase(title, description)
                .onSuccess { _uiState.update { it.copy(showAddDialog = false) } }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }
}
```

## Data Flow

### Read Flow (Database → UI)

```
SQLite Database
    ↓ (Query)
SQLDelight Queries
    ↓ (as Flow)
Repository
    ↓ (map to domain)
Domain Models (Flow<List<Task>>)
    ↓ (collect in Use Case)
Use Case
    ↓ (collect in ViewModel)
ViewModel State (StateFlow)
    ↓ (collectAsState)
Composable UI
```

### Write Flow (UI → Database)

```
User Action (Button Click)
    ↓
Composable calls ViewModel method
    ↓
ViewModel calls Use Case
    ↓
Use Case validates and calls Repository
    ↓
Repository saves to Database
    ↓
Database emits new data via Flow
    ↓
UI updates automatically (reactive)
```

## Dependency Rules

### The Dependency Rule

**Dependencies point inward:**
- Presentation → Domain ✓
- Data → Domain ✓
- Domain → Presentation ✗ (NEVER)
- Domain → Data ✗ (NEVER)

### How to Maintain Clean Boundaries

1. **Domain layer imports:**
   - ✓ Kotlin stdlib only
   - ✓ Kotlinx coroutines
   - ✗ Compose
   - ✗ SQLDelight
   - ✗ Ktor
   - ✗ Android/iOS APIs

2. **Data layer imports:**
   - ✓ Domain models and interfaces
   - ✓ SQLDelight, Ktor
   - ✓ Platform-specific APIs (via expect/actual)
   - ✗ Compose
   - ✗ ViewModels

3. **Presentation layer imports:**
   - ✓ Domain models and use cases
   - ✓ Compose
   - ✓ Voyager
   - ✗ Data layer implementations
   - ✗ SQLDelight queries
   - ✗ API DTOs

## Extension Points

### Adding a Network Layer

See: `data/remote/api/TaskApi.kt` and `data/repository/TaskRepositoryWithNetwork.kt`

1. Implement `TaskApi` interface with real Ktor calls
2. Create DTOs for your API responses
3. Update repository to fetch from network and cache locally
4. Update Koin module to provide `TaskApi`

```kotlin
// In AppModule.kt
single<TaskApi> { TaskApiImpl(get()) }
singleOf(::TaskRepositoryWithNetwork) bind TaskRepository::class
```

### Adding Navigation

See: `presentation/navigation/Screen.kt`

1. Replace `App()` with `NavigationApp()` in your platform entry points
2. Create new `Screen` implementations for new features
3. Use `navigator.push()` to navigate

```kotlin
// In MainActivity.kt or Main.kt
setContent {
    NavigationApp()  // Instead of App()
}
```

### Adding New Features

Follow the pattern:

1. **Domain:** Create entity, repository interface, use cases
2. **Data:** Implement repository, add database schema, create DTOs if needed
3. **Presentation:** Create ViewModel, UI state, screens, and components
4. **DI:** Register in Koin modules

### Adding Testing

See: `shared/src/commonTest/` for examples

1. Test entities and use cases with simple unit tests
2. Test repositories with in-memory databases or mocks
3. Test ViewModels with fake repositories
4. Use Turbine for testing Flows

## Best Practices

1. **Single Source of Truth:** Database is always the source of truth
2. **Unidirectional Data Flow:** State flows down, events flow up
3. **Immutability:** Use data classes, never mutate state directly
4. **Separation of Concerns:** Each class has one responsibility
5. **Dependency Inversion:** Depend on abstractions, not concretions
6. **Testability:** Write testable code from the start

## Common Patterns

### Repository Pattern
```kotlin
interface TaskRepository {
    fun observeAllTasks(): Flow<List<Task>>  // Reactive
    suspend fun insertTask(task: Task): Long  // One-time operation
}
```

### Use Case Pattern
```kotlin
class GetTasksUseCase(private val repository: TaskRepository) {
    operator fun invoke(): Flow<List<Task>> = repository.observeAllTasks()
}
```

### State Management Pattern
```kotlin
data class UiState(
    val data: List<Item> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

private val _state = MutableStateFlow(UiState())
val state: StateFlow<UiState> = _state.asStateFlow()
```

### Mapper Pattern
```kotlin
fun EntityDB.toDomain(): DomainModel = DomainModel(...)
fun DomainModel.toEntity(): EntityDB = EntityDB(...)
```

## Resources

- [Clean Architecture by Uncle Bob](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Kotlin Multiplatform Documentation](https://kotlinlang.org/docs/multiplatform.html)
- [SQLDelight Documentation](https://cashapp.github.io/sqldelight/)
- [Ktor Documentation](https://ktor.io/docs/)
