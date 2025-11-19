package com.example.kmpcleanarch.data.sync

import com.example.kmpcleanarch.data.network.NetworkMonitor
import com.example.kmpcleanarch.domain.sync.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock

/**
 * Manages data synchronization between local database and remote server
 *
 * Features:
 * - Automatic sync when network becomes available
 * - Retry logic with exponential backoff
 * - Conflict resolution
 * - Progress tracking
 * - Queue management for offline operations
 */
class SyncManager(
    private val networkMonitor: NetworkMonitor,
    private val requestQueue: RequestQueue,
    private val syncExecutor: SyncExecutor,
    private val conflictResolver: ConflictResolver,
    private val coroutineScope: CoroutineScope
) {

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private var syncJob: Job? = null
    private var autoSyncJob: Job? = null

    private val _lastSyncTime = MutableStateFlow<Long?>(null)
    val lastSyncTime: StateFlow<Long?> = _lastSyncTime.asStateFlow()

    init {
        startAutoSync()
    }

    /**
     * Start automatic sync when network is available
     */
    private fun startAutoSync() {
        autoSyncJob?.cancel()
        autoSyncJob = coroutineScope.launch {
            networkMonitor.isConnected.collect { isConnected ->
                if (isConnected && requestQueue.hasPending()) {
                    delay(1000) // Small delay to ensure stable connection
                    sync()
                } else if (!isConnected) {
                    _syncState.value = SyncState.WaitingForNetwork
                }
            }
        }
    }

    /**
     * Manually trigger sync
     */
    suspend fun sync(): Result<SyncResult> {
        if (syncJob?.isActive == true) {
            return Result.failure(Exception("Sync already in progress"))
        }

        if (!networkMonitor.isCurrentlyConnected()) {
            _syncState.value = SyncState.WaitingForNetwork
            return Result.failure(Exception("No network connection"))
        }

        return withContext(Dispatchers.Default) {
            try {
                _syncState.value = SyncState.Syncing(progress = 0f)

                val operations = requestQueue.getAll()
                if (operations.isEmpty()) {
                    _syncState.value = SyncState.Success(
                        timestamp = Clock.System.now(),
                        itemsSynced = 0
                    )
                    return@withContext Result.success(SyncResult.Success(0))
                }

                var synced = 0
                var failed = 0

                operations.forEachIndexed { index, operation ->
                    val progress = (index + 1).toFloat() / operations.size
                    _syncState.value = SyncState.Syncing(
                        progress = progress,
                        message = "Syncing ${index + 1}/${operations.size}"
                    )

                    val result = syncOperation(operation)
                    if (result.isSuccess) {
                        requestQueue.complete(operation.id)
                        synced++
                    } else {
                        val canRetry = requestQueue.incrementRetry(operation.id)
                        if (!canRetry) {
                            failed++
                        }
                    }

                    delay(100) // Avoid overwhelming the server
                }

                _lastSyncTime.value = System.currentTimeMillis()

                val syncResult = when {
                    failed == 0 -> {
                        _syncState.value = SyncState.Success(
                            timestamp = Clock.System.now(),
                            itemsSynced = synced
                        )
                        SyncResult.Success(synced)
                    }
                    synced > 0 -> {
                        _syncState.value = SyncState.Success(
                            timestamp = Clock.System.now(),
                            itemsSynced = synced
                        )
                        SyncResult.PartialSuccess(synced, failed)
                    }
                    else -> {
                        _syncState.value = SyncState.Error(
                            message = "All sync operations failed",
                            canRetry = true
                        )
                        SyncResult.Failure("All operations failed")
                    }
                }

                Result.success(syncResult)
            } catch (e: Exception) {
                _syncState.value = SyncState.Error(
                    message = e.message ?: "Unknown error",
                    exception = e,
                    canRetry = true
                )
                Result.failure(e)
            }
        }
    }

    /**
     * Sync a single operation with retry logic
     */
    private suspend fun syncOperation(operation: PendingSyncOperation): Result<Unit> {
        return withRetry(
            maxAttempts = operation.maxRetries - operation.retryCount,
            initialDelay = calculateBackoff(operation.retryCount)
        ) {
            syncExecutor.execute(operation)
        }
    }

    /**
     * Calculate exponential backoff delay
     */
    private fun calculateBackoff(retryCount: Int): Long {
        val baseDelay = 1000L // 1 second
        return baseDelay * (1 shl retryCount.coerceIn(0, 5)) // Cap at 32 seconds
    }

    /**
     * Retry logic with exponential backoff
     */
    private suspend fun <T> withRetry(
        maxAttempts: Int = 3,
        initialDelay: Long = 1000L,
        maxDelay: Long = 32000L,
        factor: Double = 2.0,
        block: suspend () -> Result<T>
    ): Result<T> {
        var currentDelay = initialDelay
        repeat(maxAttempts) { attempt ->
            val result = block()
            if (result.isSuccess) {
                return result
            }

            if (attempt < maxAttempts - 1) {
                delay(currentDelay)
                currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
            }
        }
        return block() // Final attempt
    }

    /**
     * Cancel ongoing sync
     */
    fun cancelSync() {
        syncJob?.cancel()
        _syncState.value = SyncState.Idle
    }

    /**
     * Check if sync is in progress
     */
    fun isSyncing(): Boolean {
        return _syncState.value is SyncState.Syncing
    }

    /**
     * Get pending operations count
     */
    fun getPendingCount(): Int {
        return requestQueue.count()
    }

    /**
     * Clear all pending operations
     */
    fun clearPendingOperations() {
        requestQueue.clear()
        _syncState.value = SyncState.Idle
    }

    /**
     * Stop auto sync and cleanup
     */
    fun stop() {
        autoSyncJob?.cancel()
        syncJob?.cancel()
    }
}

/**
 * Interface for executing sync operations
 */
interface SyncExecutor {
    suspend fun execute(operation: PendingSyncOperation): Result<Unit>
}

/**
 * Interface for resolving conflicts between local and remote data
 */
interface ConflictResolver {
    suspend fun <T> resolve(
        local: T,
        remote: T,
        strategy: ConflictResolution
    ): T
}
