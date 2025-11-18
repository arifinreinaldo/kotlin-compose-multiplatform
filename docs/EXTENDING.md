# Extension Guide

This guide shows you how to extend the boilerplate with common features.

## Table of Contents

1. [Adding a New Feature](#adding-a-new-feature)
2. [Adding Network Layer](#adding-network-layer)
3. [Adding Navigation](#adding-navigation)
4. [Adding Authentication](#adding-authentication)
5. [Adding Offline Support](#adding-offline-support)
6. [Adding Analytics](#adding-analytics)

## Adding a New Feature

Let's add a "Notes" feature as an example.

### Step 1: Domain Layer

Create the entity:

```kotlin
// domain/model/Note.kt
data class Note(
    val id: Long = 0,
    val title: String,
    val content: String,
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
```

Create repository interface:

```kotlin
// domain/repository/NoteRepository.kt
interface NoteRepository {
    fun observeAllNotes(): Flow<List<Note>>
    suspend fun insertNote(note: Note): Long
    suspend fun updateNote(note: Note)
    suspend fun deleteNote(id: Long)
    suspend fun searchNotes(query: String): List<Note>
}
```

Create use cases:

```kotlin
// domain/usecase/GetAllNotesUseCase.kt
class GetAllNotesUseCase(private val repository: NoteRepository) {
    operator fun invoke(): Flow<List<Note>> = repository.observeAllNotes()
}

// domain/usecase/AddNoteUseCase.kt
class AddNoteUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(title: String, content: String): Result<Long> {
        if (title.isBlank()) {
            return Result.failure(IllegalArgumentException("Title required"))
        }
        return try {
            val note = Note(title = title.trim(), content = content.trim())
            Result.success(repository.insertNote(note))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### Step 2: Data Layer

Create database schema:

```sql
-- shared/src/commonMain/sqldelight/.../database/Note.sq
CREATE TABLE IF NOT EXISTS NoteEntity (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    tags TEXT NOT NULL,  -- Store as JSON array
    createdAt INTEGER NOT NULL,
    updatedAt INTEGER NOT NULL
);

selectAll:
SELECT * FROM NoteEntity ORDER BY updatedAt DESC;

selectById:
SELECT * FROM NoteEntity WHERE id = ?;

search:
SELECT * FROM NoteEntity
WHERE title LIKE '%' || ? || '%' OR content LIKE '%' || ? || '%';

insert:
INSERT INTO NoteEntity(title, content, tags, createdAt, updatedAt)
VALUES (?, ?, ?, ?, ?);

update:
UPDATE NoteEntity
SET title = ?, content = ?, tags = ?, updatedAt = ?
WHERE id = ?;

deleteById:
DELETE FROM NoteEntity WHERE id = ?;

lastInsertRowId:
SELECT last_insert_rowid();
```

Create mapper:

```kotlin
// data/mapper/NoteMapper.kt
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

fun NoteEntity.toDomainModel(): Note {
    return Note(
        id = id,
        title = title,
        content = content,
        tags = Json.decodeFromString(tags),
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Note.toEntity(): NoteEntity {
    return NoteEntity(
        id = id,
        title = title,
        content = content,
        tags = Json.encodeToString(tags),
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
```

Create repository implementation:

```kotlin
// data/repository/NoteRepositoryImpl.kt
class NoteRepositoryImpl(
    private val database: AppDatabase
) : NoteRepository {

    private val queries = database.noteQueries

    override fun observeAllNotes(): Flow<List<Note>> {
        return queries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { it.map { entity -> entity.toDomainModel() } }
    }

    override suspend fun insertNote(note: Note): Long = withContext(Dispatchers.Default) {
        queries.insert(
            title = note.title,
            content = note.content,
            tags = Json.encodeToString(note.tags),
            createdAt = note.createdAt,
            updatedAt = note.updatedAt
        )
        queries.lastInsertRowId().executeAsOne()
    }

    override suspend fun updateNote(note: Note) = withContext(Dispatchers.Default) {
        queries.update(
            title = note.title,
            content = note.content,
            tags = Json.encodeToString(note.tags),
            updatedAt = System.currentTimeMillis(),
            id = note.id
        )
    }

    override suspend fun deleteNote(id: Long) = withContext(Dispatchers.Default) {
        queries.deleteById(id)
    }

    override suspend fun searchNotes(query: String): List<Note> = withContext(Dispatchers.Default) {
        queries.search(query, query)
            .executeAsList()
            .map { it.toDomainModel() }
    }
}
```

### Step 3: Presentation Layer

Create UI state:

```kotlin
// presentation/state/NoteUiState.kt
data class NoteUiState(
    val notes: List<Note> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddDialog: Boolean = false
)
```

Create ViewModel:

```kotlin
// presentation/viewmodel/NoteViewModel.kt
class NoteViewModel(
    private val getAllNotesUseCase: GetAllNotesUseCase,
    private val addNoteUseCase: AddNoteUseCase,
    private val searchNotesUseCase: SearchNotesUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NoteUiState())
    val uiState: StateFlow<NoteUiState> = _uiState.asStateFlow()

    init {
        loadNotes()
    }

    private fun loadNotes() {
        viewModelScope.launch {
            getAllNotesUseCase()
                .catch { e -> _uiState.update { it.copy(error = e.message) } }
                .collect { notes ->
                    _uiState.update { it.copy(notes = notes, isLoading = false) }
                }
        }
    }

    fun addNote(title: String, content: String) {
        viewModelScope.launch {
            addNoteUseCase(title, content)
                .onSuccess { _uiState.update { it.copy(showAddDialog = false) } }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }

    fun deleteNote(noteId: Long) {
        viewModelScope.launch {
            deleteNoteUseCase(noteId)
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }

    fun search(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        // Implement search logic
    }
}
```

Create screen:

```kotlin
// presentation/ui/screen/NoteListScreen.kt
@Composable
fun NoteListScreen(
    windowSizeClass: WindowSizeClass,
    viewModel: NoteViewModel = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notes") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showAddDialog() }) {
                Text("+")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Search bar
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = { viewModel.search(it) }
            )

            // Note list
            LazyColumn {
                items(uiState.notes) { note ->
                    NoteCard(
                        note = note,
                        onClick = { /* Navigate to detail */ },
                        onDeleteClick = { viewModel.deleteNote(note.id) }
                    )
                }
            }
        }
    }
}
```

### Step 4: Dependency Injection

Update Koin module:

```kotlin
// di/AppModule.kt
val commonModule = module {
    // ... existing dependencies

    // Note feature
    singleOf(::NoteRepositoryImpl) bind NoteRepository::class
    factoryOf(::GetAllNotesUseCase)
    factoryOf(::AddNoteUseCase)
    factoryOf(::SearchNotesUseCase)
    factoryOf(::DeleteNoteUseCase)
    factoryOf(::NoteViewModel)
}
```

### Step 5: Add Navigation (Optional)

```kotlin
// presentation/navigation/Screen.kt
class NoteListScreenVoyager : Screen {
    @Composable
    override fun Content() {
        BoxWithConstraints {
            val density = LocalDensity.current
            val width = with(density) { maxWidth }
            val windowSizeClass = getWindowSizeClass(width)

            NoteListScreen(windowSizeClass = windowSizeClass)
        }
    }
}
```

## Adding Network Layer

### 1. Create API Interface

```kotlin
// data/remote/api/NoteApi.kt
interface NoteApi {
    suspend fun getNotes(): Result<List<NoteDto>>
    suspend fun createNote(note: NoteDto): Result<NoteDto>
    suspend fun updateNote(id: String, note: NoteDto): Result<NoteDto>
    suspend fun deleteNote(id: String): Result<Unit>
}
```

### 2. Implement API

```kotlin
// data/remote/api/NoteApiImpl.kt
class NoteApiImpl(private val httpClient: HttpClient) : NoteApi {
    override suspend fun getNotes(): Result<List<NoteDto>> {
        return try {
            val response = httpClient.get("/api/notes").body<List<NoteDto>>()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createNote(note: NoteDto): Result<NoteDto> {
        return try {
            val response = httpClient.post("/api/notes") {
                contentType(ContentType.Application.Json)
                setBody(note)
            }.body<NoteDto>()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### 3. Update Repository for Network Sync

```kotlin
class NoteRepositoryWithNetwork(
    private val database: AppDatabase,
    private val noteApi: NoteApi
) : NoteRepository {

    private val localRepo = NoteRepositoryImpl(database)

    override fun observeAllNotes(): Flow<List<Note>> {
        return localRepo.observeAllNotes()
    }

    override suspend fun insertNote(note: Note): Long {
        // Save locally first
        val localId = localRepo.insertNote(note)

        // Sync to server in background
        viewModelScope.launch {
            noteApi.createNote(note.toDto())
                .onSuccess { /* Update with server ID */ }
                .onFailure { /* Queue for retry */ }
        }

        return localId
    }

    private suspend fun syncFromServer() {
        noteApi.getNotes()
            .onSuccess { dtos ->
                dtos.forEach { dto ->
                    localRepo.insertNote(dto.toDomainModel())
                }
            }
    }
}
```

### 4. Configure in Koin

```kotlin
single {
    HttpClientFactory.create(enableLogging = true)
}

single<NoteApi> {
    NoteApiImpl(get())
}

singleOf(::NoteRepositoryWithNetwork) bind NoteRepository::class
```

## Adding Navigation

Already configured! Just use it:

```kotlin
// In your platform entry points (MainActivity.kt, Main.kt)
setContent {
    NavigationApp()  // Instead of App()
}

// Navigate in your screens
@Composable
override fun Content() {
    val navigator = LocalNavigator.currentOrThrow

    Button(onClick = {
        navigator.push(NoteDetailScreenVoyager(noteId = 123))
    }) {
        Text("View Note")
    }
}
```

## Adding Authentication

### 1. Create Auth Domain

```kotlin
// domain/model/User.kt
data class User(
    val id: String,
    val email: String,
    val name: String,
    val token: String
)

// domain/repository/AuthRepository.kt
interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun logout(): Result<Unit>
    suspend fun getCurrentUser(): User?
    fun observeAuthState(): Flow<User?>
}

// domain/usecase/LoginUseCase.kt
class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        if (!email.isValidEmail()) {
            return Result.failure(IllegalArgumentException("Invalid email"))
        }
        return repository.login(email, password)
    }
}
```

### 2. Implement Auth Repository

```kotlin
// data/repository/AuthRepositoryImpl.kt
class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val tokenStorage: TokenStorage
) : AuthRepository {

    private val _authState = MutableStateFlow<User?>(null)

    override suspend fun login(email: String, password: String): Result<User> {
        return authApi.login(email, password)
            .onSuccess { user ->
                tokenStorage.saveToken(user.token)
                _authState.value = user
            }
    }

    override suspend fun logout(): Result<Unit> {
        tokenStorage.clearToken()
        _authState.value = null
        return Result.success(Unit)
    }

    override fun observeAuthState(): Flow<User?> = _authState.asStateFlow()
}
```

### 3. Add Auth to HTTP Client

```kotlin
HttpClient {
    install(Auth) {
        bearer {
            loadTokens {
                val token = tokenStorage.getToken()
                BearerTokens(token, token)
            }
        }
    }
}
```

## Adding Offline Support

Already built in! The SQLite database is the single source of truth.

For network sync:

```kotlin
// Use WorkManager (Android) or Background Tasks
class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val repository = get<TaskRepository>()
        repository.syncWithServer()
        return Result.success()
    }
}

// Schedule periodic sync
WorkManager.getInstance(context).enqueueUniquePeriodicWork(
    "sync",
    ExistingPeriodicWorkPolicy.KEEP,
    PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES).build()
)
```

## Adding Analytics

### 1. Create Analytics Interface

```kotlin
// domain/analytics/Analytics.kt
interface Analytics {
    fun logEvent(event: String, params: Map<String, Any> = emptyMap())
    fun setUserId(userId: String)
    fun setUserProperty(key: String, value: String)
}
```

### 2. Implement for Platforms

```kotlin
// Android
actual class AnalyticsImpl(private val context: Context) : Analytics {
    private val firebase = Firebase.analytics

    actual override fun logEvent(event: String, params: Map<String, Any>) {
        firebase.logEvent(event, bundleOf(*params.toList().toTypedArray()))
    }
}

// iOS
actual class AnalyticsImpl : Analytics {
    actual override fun logEvent(event: String, params: Map<String, Any>) {
        // Use Firebase iOS SDK
    }
}
```

### 3. Use in ViewModels

```kotlin
class TaskViewModel(
    private val getAllTasksUseCase: GetAllTasksUseCase,
    private val analytics: Analytics
) : ViewModel() {

    fun addTask(title: String, description: String) {
        viewModelScope.launch {
            addTaskUseCase(title, description)
                .onSuccess {
                    analytics.logEvent("task_created", mapOf("title_length" to title.length))
                }
        }
    }
}
```

## Summary

The boilerplate provides all the extension points you need:

✅ Network layer structure (Ktor)
✅ Testing infrastructure
✅ Navigation setup (Voyager)
✅ Utility extensions
✅ Clean architecture pattern
✅ Dependency injection (Koin)

Just follow the patterns and add your features!
