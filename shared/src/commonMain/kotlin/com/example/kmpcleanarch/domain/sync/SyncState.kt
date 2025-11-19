package com.example.kmpcleanarch.domain.sync

import kotlinx.datetime.Instant

/**
 * Represents the state of data synchronization
 */
sealed class SyncState {
    /**
     * No sync activity
     */
    data object Idle : SyncState()

    /**
     * Sync in progress
     */
    data class Syncing(
        val progress: Float = 0f,
        val message: String = "Syncing..."
    ) : SyncState()

    /**
     * Sync completed successfully
     */
    data class Success(
        val timestamp: Instant,
        val itemsSynced: Int = 0
    ) : SyncState()

    /**
     * Sync failed
     */
    data class Error(
        val message: String,
        val exception: Throwable? = null,
        val canRetry: Boolean = true
    ) : SyncState()

    /**
     * Waiting for network connectivity
     */
    data object WaitingForNetwork : SyncState()
}

/**
 * Represents a pending sync operation
 */
data class PendingSyncOperation(
    val id: String,
    val type: SyncOperationType,
    val entityType: String,
    val entityId: String,
    val data: String, // JSON serialized data
    val timestamp: Long,
    val retryCount: Int = 0,
    val maxRetries: Int = 3
) {
    fun canRetry(): Boolean = retryCount < maxRetries
}

/**
 * Types of sync operations
 */
enum class SyncOperationType {
    CREATE,
    UPDATE,
    DELETE
}

/**
 * Conflict resolution strategy when local and remote data differ
 */
sealed class ConflictResolution {
    /**
     * Use local version (client wins)
     */
    data object UseLocal : ConflictResolution()

    /**
     * Use remote version (server wins)
     */
    data object UseRemote : ConflictResolution()

    /**
     * Use the most recent version based on timestamp
     */
    data object LastWriteWins : ConflictResolution()

    /**
     * Merge both versions (requires custom logic)
     */
    data class Merge(val mergeStrategy: MergeStrategy) : ConflictResolution()

    /**
     * Ask user to resolve manually
     */
    data object Manual : ConflictResolution()
}

/**
 * Custom merge strategy for conflict resolution
 */
interface MergeStrategy {
    fun <T> merge(local: T, remote: T): T
}

/**
 * Result of a sync operation
 */
sealed class SyncResult {
    data class Success(val itemsSynced: Int) : SyncResult()
    data class PartialSuccess(val itemsSynced: Int, val itemsFailed: Int) : SyncResult()
    data class Failure(val error: String, val exception: Throwable? = null) : SyncResult()
}
