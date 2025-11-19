# Offline-First Architecture

Complete guide to the offline-first architecture implementation in this Kotlin Compose Multiplatform project.

## Table of Contents

- [Overview](#overview)
- [Architecture Components](#architecture-components)
- [Network Monitoring](#network-monitoring)
- [Sync Manager](#sync-manager)
- [Request Queue](#request-queue)
- [Conflict Resolution](#conflict-resolution)
- [Optimistic Updates](#optimistic-updates)
- [Usage Guide](#usage-guide)
- [Best Practices](#best-practices)
- [Testing](#testing)
- [Troubleshooting](#troubleshooting)

## Overview

The offline-first architecture ensures your app works seamlessly regardless of network connectivity:

### Key Principles

1. **Local Database is Source of Truth**
   - All data operations hit the local database first
   - UI updates immediately from local data
   - Network sync happens in the background

2. **Optimistic Updates**
   - User actions complete instantly
   - Operations queued for sync when offline
   - Automatic sync when network returns

3. **Transparent to UI**
   - UI layer doesn't know about network state
   - Same API whether online or offline
   - Automatic conflict resolution

### Benefits

- ✅ Works offline out of the box
- ✅ Instant UI updates (no network waiting)
- ✅ Automatic background sync
- ✅ Handles poor network gracefully
- ✅ Reduced server load
- ✅ Better user experience

## Architecture Components

```
┌─────────────────────────────────────────────────────┐
│                   Presentation Layer                 │
│         (ViewModels, UI - No network awareness)     │
└──────────────────┬──────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────┐
│              Repository (Offline-First)              │
│  - Local operations first (optimistic updates)       │
│  - Queue operations when offline                    │
│  - Sync when online                                 │
└──────┬─────────────────────┬────────────────────────┘
       │                     │
┌──────▼──────┐    ┌────────▼────────┐
│   Local DB   │    │  Sync Manager   │
│  (SQLDelight)│    │  - RequestQueue │
└──────────────┘    │  - NetworkMon   │
                    │  - SyncExecutor │
                    │  - Conflict Res │
                    └─────────────────┘
```

## Network Monitoring

### NetworkMonitor Interface

Platform-agnostic network connectivity monitoring.

**Location**: `shared/src/commonMain/.../data/network/NetworkMonitor.kt`

```kotlin
interface NetworkMonitor {
    val isConnected: Flow<Boolean>
    suspend fun isCurrentlyConnected(): Boolean
    suspend fun getNetworkType(): NetworkType
}

enum class NetworkType {
    WIFI, CELLULAR, ETHERNET, UNKNOWN, NONE
}
```

### Platform Implementations

#### Android
**File**: `NetworkMonitor.android.kt`

Uses `ConnectivityManager` and `NetworkCallback`:
- Real-time connectivity updates
- Network capability checking
- Battery-efficient

#### iOS
**File**: `NetworkMonitor.ios.kt`

Uses `SCNetworkReachability`:
- System-level reachability
- Works in background
- Native iOS integration

#### Desktop
**File**: `NetworkMonitor.desktop.kt`

Uses ping-based checking:
- Polls Google DNS (8.8.8.8)
- 5-second intervals
- Works on all desktop platforms

### Usage

```kotlin
class MyViewModel(
    private val networkMonitor: NetworkMonitor
) : ViewModel() {
    
    val isOnline = networkMonitor.isConnected
        .stateIn(viewModelScope, SharingStarted.Lazily, false)
    
    suspend fun checkNetwork() {
        val connected = networkMonitor.isCurrentlyConnected()
        val type = networkMonitor.getNetworkType()
        println("Connected: $connected, Type: $type")
    }
}
```

## Sync Manager

Coordinates all synchronization between local database and remote server.

**Location**: `shared/src/commonMain/.../data/sync/SyncManager.kt`

### Features

- ✅ Automatic sync when network available
- ✅ Exponential backoff retry logic
- ✅ Progress tracking
- ✅ Conflict resolution
- ✅ Queue management
- ✅ Batch processing

### Sync States

```kotlin
sealed class SyncState {
    data object Idle
    data class Syncing(val progress: Float, val message: String)
    data class Success(val timestamp: Instant, val itemsSynced: Int)
    data class Error(val message: String, val canRetry: Boolean)
    data object WaitingForNetwork
}
```

### Automatic Sync

```kotlin
// SyncManager automatically syncs when:
// 1. Network becomes available
// 2. Pending operations exist

init {
    startAutoSync()
}

private fun startAutoSync() {
    networkMonitor.isConnected.collect { isConnected ->
        if (isConnected && requestQueue.hasPending()) {
            delay(1000) // Ensure stable connection
            sync()
        }
    }
}
```

### Manual Sync

```kotlin
class MyViewModel(
    private val syncManager: SyncManager
) : ViewModel() {
    
    val syncState = syncManager.syncState
    
    fun syncNow() {
        viewModelScope.launch {
            val result = syncManager.sync()
            result.onSuccess {
                println("Synced successfully")
            }
        }
    }
}
```

### Retry Logic

Exponential backoff with configurable parameters:

```kotlin
// Base delay: 1 second
// Factor: 2x
// Max delay: 32 seconds
// Retries: 3 attempts

// Retry schedule:
// 1st retry: 1 second
// 2nd retry: 2 seconds
// 3rd retry: 4 seconds
// 4th retry: 8 seconds
// Final retry: Max 32 seconds
```

## Request Queue

Manages offline operations that need synchronization.

**Location**: `shared/src/commonMain/.../data/sync/RequestQueue.kt`

### Operation Types

```kotlin
enum class SyncOperationType {
    CREATE,   // New entity
    UPDATE,   // Modified entity
    DELETE    // Removed entity
}
```

### Pending Operations

```kotlin
data class PendingSyncOperation(
    val id: String,
    val type: SyncOperationType,
    val entityType: String,
    val entityId: String,
    val data: String, // JSON serialized
    val timestamp: Long,
    val retryCount: Int = 0,
    val maxRetries: Int = 3
)
```

### Queue Operations

```kotlin
// Enqueue operation
val operationId = requestQueue.enqueue(
    type = SyncOperationType.CREATE,
    entityType = "Task",
    entityId = "123",
    data = """{"title": "Buy groceries"}""",
    maxRetries = 3
)

// Get all pending
val pending = requestQueue.getAll()

// Complete operation
requestQueue.complete(operationId)

// Increment retry count
val canRetry = requestQueue.incrementRetry(operationId)

// Clear all
requestQueue.clear()
```

### Observable Queue

```kotlin
class SyncViewModel(
    private val requestQueue: RequestQueue
) : ViewModel() {
    
    val pendingOps = requestQueue.pendingOperations
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    val pendingCount = pendingOps.map { it.size }
}
```

## Conflict Resolution

Handles conflicts when local and remote data differ.

**Location**: `shared/src/commonMain/.../data/sync/DefaultConflictResolver.kt`

### Strategies

```kotlin
sealed class ConflictResolution {
    // Use local version (client wins)
    data object UseLocal
    
    // Use remote version (server wins)
    data object UseRemote
    
    // Use most recent (timestamp-based)
    data object LastWriteWins
    
    // Merge both versions
    data class Merge(val strategy: MergeStrategy)
    
    // Ask user to resolve
    data object Manual
}
```

### Last Write Wins (Default)

Automatically resolves using timestamps:

```kotlin
data class Task(
    val id: Long,
    val title: String,
    val updatedAt: Long // Required for LastWriteWins
)

// Resolver looks for these fields:
// - updatedAt
// - timestamp
// - lastModified
// - modifiedAt
```

### Custom Merge Strategy

```kotlin
class TaskMergeStrategy : MergeStrategy {
    override fun <T> merge(local: T, remote: T): T {
        if (local is Task && remote is Task) {
            return Task(
                id = local.id,
                title = if (local.title.isNotEmpty()) local.title else remote.title,
                isCompleted = local.isCompleted || remote.isCompleted,
                updatedAt = maxOf(local.updatedAt, remote.updatedAt)
            )
        }
        return remote
    }
}

// Use custom strategy
val resolution = ConflictResolution.Merge(TaskMergeStrategy())
```

## Optimistic Updates

UI updates immediately, sync happens in background.

**Location**: `shared/src/commonMain/.../data/repository/OfflineFirstRepository.kt`

### Base Repository

```kotlin
abstract class OfflineFirstRepository<T : Any>(
    protected val networkMonitor: NetworkMonitor,
    protected val requestQueue: RequestQueue
) {
    abstract val entityType: String
    
    protected suspend fun createOptimistically(
        entity: T,
        entityId: String,
        localCreate: suspend () -> Result<Unit>,
        remoteCreate: suspend () -> Result<Unit>
    ): Result<Unit>
}
```

### Example Implementation

```kotlin
class OfflineFirstTaskRepository(
    private val localDataSource: TaskLocalDataSource,
    private val taskApi: TaskApi,
    networkMonitor: NetworkMonitor,
    requestQueue: RequestQueue
) : OfflineFirstRepository<Task>(networkMonitor, requestQueue) {
    
    override val entityType = "Task"
    
    override suspend fun insertTask(title: String): Result<Unit> {
        val task = Task(...)
        
        return createOptimistically(
            entity = task,
            entityId = task.id.toString(),
            localCreate = {
                // Saves immediately to local DB
                localDataSource.insertTask(title)
            },
            remoteCreate = {
                // Syncs to server (or queues if offline)
                taskApi.createTask(task)
            }
        )
    }
}
```

### Flow Diagram

```
User Action (Create Task)
        ↓
Save to Local DB (immediate)
        ↓
UI Updates (instant feedback)
        ↓
Check Network
        ├─ Online → Sync to Server
        │             ├─ Success → Done
        │             └─ Failure → Queue
        └─ Offline → Queue for later
```

## Usage Guide

### 1. Inject Dependencies

```kotlin
class MyViewModel(
    private val syncManager: SyncManager,
    private val networkMonitor: NetworkMonitor,
    private val repository: OfflineFirstTaskRepository
) : ViewModel() {
    
    val syncState = syncManager.syncState.stateIn(...)
    val isOnline = networkMonitor.isConnected.stateIn(...)
}
```

### 2. Create Entity (Optimistic)

```kotlin
fun createTask(title: String) {
    viewModelScope.launch {
        // This returns immediately after local save
        repository.insertTask(title)
        // UI already updated, sync happens in background
    }
}
```

### 3. Monitor Sync Status

```kotlin
@Composable
fun SyncIndicator(syncState: SyncState) {
    when (syncState) {
        is SyncState.Idle -> {
            // Show nothing or "Synced" badge
        }
        is SyncState.Syncing -> {
            CircularProgressIndicator()
            Text("Syncing ${syncState.message}")
        }
        is SyncState.Success -> {
            Icon(Icons.Default.CheckCircle, "Synced")
            Text("${syncState.itemsSynced} items synced")
        }
        is SyncState.Error -> {
            Icon(Icons.Default.Error, "Error")
            Text(syncState.message)
        }
        is SyncState.WaitingForNetwork -> {
            Icon(Icons.Default.CloudOff, "Offline")
            Text("Waiting for network")
        }
    }
}
```

### 4. Show Network Status

```kotlin
@Composable
fun NetworkBanner(isOnline: Boolean) {
    if (!isOnline) {
        Surface(color = Color.Yellow) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.CloudOff, "Offline")
                Spacer(Modifier.width(8.dp))
                Text("You're offline. Changes will sync when connected.")
            }
        }
    }
}
```

### 5. Manual Sync Button

```kotlin
@Composable
fun SyncButton(
    syncManager: SyncManager,
    modifier: Modifier = Modifier
) {
    val syncState by syncManager.syncState.collectAsState()
    val scope = rememberCoroutineScope()
    
    IconButton(
        onClick = {
            scope.launch {
                syncManager.sync()
            }
        },
        enabled = syncState !is SyncState.Syncing,
        modifier = modifier
    ) {
        when (syncState) {
            is SyncState.Syncing -> CircularProgressIndicator()
            else -> Icon(Icons.Default.Sync, "Sync")
        }
    }
}
```

## Best Practices

### 1. Always Use Local-First Pattern

```kotlin
// ❌ Bad: Network first
suspend fun getData(): List<Task> {
    return taskApi.getTasks().getOrElse {
        localDataSource.getTasks()
    }
}

// ✅ Good: Local first
fun getData(): Flow<List<Task>> {
    // Return local immediately
    val localFlow = localDataSource.getAllTasks()
    
    // Refresh from network in background
    if (isOnline) {
        refreshFromNetwork()
    }
    
    return localFlow
}
```

### 2. Add updatedAt to All Entities

```kotlin
// Required for LastWriteWins conflict resolution
data class MyEntity(
    val id: Long,
    val name: String,
    val updatedAt: Long = System.currentTimeMillis()
)
```

### 3. Handle Sync Errors Gracefully

```kotlin
syncManager.syncState.collect { state ->
    when (state) {
        is SyncState.Error -> {
            // Don't block user, show non-intrusive message
            showSnackbar("Sync failed. Will retry automatically.")
        }
    }
}
```

### 4. Clear Sensitive Data from Queue

```kotlin
// On logout, clear pending operations
fun logout() {
    syncManager.clearPendingOperations()
    localDataSource.clearAll()
}
```

### 5. Monitor Pending Operations

```kotlin
// Show badge when operations pending
val pendingCount by requestQueue.pendingOperations
    .map { it.size }
    .collectAsState(0)

if (pendingCount > 0) {
    Badge { Text("$pendingCount") }
}
```

## Testing

### Test Network Monitor

```kotlin
@Test
fun testNetworkMonitor() = runTest {
    val monitor = FakeNetworkMonitor()
    
    monitor.setConnected(true)
    assertTrue(monitor.isCurrentlyConnected())
    
    monitor.setConnected(false)
    assertFalse(monitor.isCurrentlyConnected())
}

class FakeNetworkMonitor : NetworkMonitor {
    private val _isConnected = MutableStateFlow(true)
    override val isConnected = _isConnected.asStateFlow()
    
    fun setConnected(connected: Boolean) {
        _isConnected.value = connected
    }
    
    override suspend fun isCurrentlyConnected() = _isConnected.value
    override suspend fun getNetworkType() = NetworkType.WIFI
}
```

### Test Request Queue

```kotlin
@Test
fun testQueueOperations() {
    val queue = RequestQueue()
    
    // Enqueue
    val id = queue.enqueue(
        type = SyncOperationType.CREATE,
        entityType = "Task",
        entityId = "1",
        data = """{"title":"Test"}"""
    )
    
    // Verify
    assertEquals(1, queue.count())
    assertTrue(queue.hasPending())
    
    // Complete
    queue.complete(id)
    assertEquals(0, queue.count())
}
```

### Test Optimistic Updates

```kotlin
@Test
fun testOptimisticCreate() = runTest {
    val repo = OfflineFirstTaskRepository(...)
    val monitor = FakeNetworkMonitor()
    monitor.setConnected(false) // Offline
    
    // Create while offline
    repo.insertTask("Test task")
    
    // Verify saved locally
    val tasks = repo.getAllTasks().first()
    assertEquals(1, tasks.size)
    
    // Verify queued for sync
    assertEquals(1, requestQueue.count())
}
```

## Troubleshooting

### Operations Not Syncing

**Check:**
1. Network monitor showing connected?
2. Request queue has pending operations?
3. SyncManager running?
4. Check logs for sync errors

```kotlin
// Debug logging
syncManager.syncState.collect { state ->
    println("Sync State: $state")
}

requestQueue.pendingOperations.collect { ops ->
    println("Pending: ${ops.size} operations")
}
```

### Conflicts Not Resolving

**Solution**: Ensure entities have `updatedAt` field:

```kotlin
// ❌ Missing timestamp
data class Task(val id: Long, val title: String)

// ✅ With timestamp
data class Task(
    val id: Long,
    val title: String,
    val updatedAt: Long
)
```

### High Battery Usage

**iOS/Desktop**: Reduce network polling frequency:

```kotlin
// In DesktopNetworkMonitor
delay(30000) // 30 seconds instead of 5
```

### Memory Leaks

**Ensure cleanup:**

```kotlin
class MyViewModel : ViewModel() {
    override fun onCleared() {
        syncManager.stop()
        super.onCleared()
    }
}
```

## Advanced Topics

### Custom Sync Executor

```kotlin
class MyEntitySyncExecutor(
    private val myApi: MyApi
) : SyncExecutor {
    override suspend fun execute(
        operation: PendingSyncOperation
    ): Result<Unit> {
        return when (operation.type) {
            CREATE -> myApi.create(operation.data)
            UPDATE -> myApi.update(operation.data)
            DELETE -> myApi.delete(operation.entityId)
        }
    }
}

// Register in DI
single<SyncExecutor> { MyEntitySyncExecutor(get()) }
```

### Batch Sync

```kotlin
// Sync multiple entities at once
suspend fun batchSync() {
    val operations = requestQueue.getAll()
    val batches = operations.chunked(50) // 50 per batch
    
    batches.forEach { batch ->
        myApi.batchSync(batch)
    }
}
```

### Priority Queue

```kotlin
// Add priority to operations
data class PendingSyncOperation(
    // ... existing fields
    val priority: Int = 0 // Higher = more important
)

// Process high priority first
fun getAll(): List<PendingSyncOperation> {
    return operationsMap.values
        .sortedWith(
            compareByDescending<PendingSyncOperation> { it.priority }
                .thenBy { it.timestamp }
        )
}
```

---

## Summary

The offline-first architecture provides:

- ✅ **Instant UI updates** - No network waiting
- ✅ **Automatic sync** - Background synchronization
- ✅ **Conflict resolution** - Smart merge strategies
- ✅ **Network resilience** - Works offline or online
- ✅ **Production-ready** - Battery efficient, tested
- ✅ **Transparent** - UI layer unchanged

Your app now works seamlessly regardless of connectivity! 🎉

## Next Steps

1. **Customize conflict resolution** for your domain
2. **Add sync indicators** to your UI
3. **Test offline scenarios** thoroughly
4. **Monitor sync metrics** in production
5. **Optimize battery usage** for your use case

For questions or issues, see the main documentation or open an issue.
