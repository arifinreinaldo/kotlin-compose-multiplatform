package com.example.kmpcleanarch.data.repository

import com.example.kmpcleanarch.data.network.NetworkMonitor
import com.example.kmpcleanarch.data.sync.RequestQueue
import com.example.kmpcleanarch.domain.sync.SyncOperationType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Base class for offline-first repositories
 *
 * Provides automatic queueing of operations when offline.
 * Subclasses handle entity-specific logic.
 */
abstract class OfflineFirstRepository<T : Any>(
    protected val networkMonitor: NetworkMonitor,
    protected val requestQueue: RequestQueue,
    protected val json: Json = Json { ignoreUnknownKeys = true }
) {

    /**
     * Entity type name for queueing
     */
    abstract val entityType: String

    /**
     * Create entity optimistically
     * - Saves to local database immediately
     * - Queues for sync if offline
     * - Syncs immediately if online
     */
    protected suspend fun createOptimistically(
        entityId: String,
        serializedEntity: String,
        localCreate: suspend () -> Result<Unit>,
        remoteCreate: suspend () -> Result<Unit>
    ): Result<Unit> {
        // Always save locally first (optimistic update)
        val localResult = localCreate()
        if (localResult.isFailure) {
            return localResult
        }

        // Check network and sync or queue
        return if (networkMonitor.isCurrentlyConnected()) {
            // Try to sync immediately
            val remoteResult = remoteCreate()
            if (remoteResult.isFailure) {
                // Remote failed, queue for later
                queueOperation(SyncOperationType.CREATE, entityId, serializedEntity)
            }
            Result.success(Unit) // Local success is what matters
        } else {
            // Queue for when network is available
            queueOperation(SyncOperationType.CREATE, entityId, serializedEntity)
            Result.success(Unit)
        }
    }

    /**
     * Update entity optimistically
     */
    protected suspend fun updateOptimistically(
        entityId: String,
        serializedEntity: String,
        localUpdate: suspend () -> Result<Unit>,
        remoteUpdate: suspend () -> Result<Unit>
    ): Result<Unit> {
        // Always save locally first (optimistic update)
        val localResult = localUpdate()
        if (localResult.isFailure) {
            return localResult
        }

        // Check network and sync or queue
        return if (networkMonitor.isCurrentlyConnected()) {
            // Try to sync immediately
            val remoteResult = remoteUpdate()
            if (remoteResult.isFailure) {
                // Remote failed, queue for later
                queueOperation(SyncOperationType.UPDATE, entityId, serializedEntity)
            }
            Result.success(Unit)
        } else {
            // Queue for when network is available
            queueOperation(SyncOperationType.UPDATE, entityId, serializedEntity)
            Result.success(Unit)
        }
    }

    /**
     * Delete entity optimistically
     */
    protected suspend fun deleteOptimistically(
        entityId: String,
        serializedEntity: String,
        localDelete: suspend () -> Result<Unit>,
        remoteDelete: suspend () -> Result<Unit>
    ): Result<Unit> {
        // Always delete locally first (optimistic update)
        val localResult = localDelete()
        if (localResult.isFailure) {
            return localResult
        }

        // Check network and sync or queue
        return if (networkMonitor.isCurrentlyConnected()) {
            // Try to sync immediately
            val remoteResult = remoteDelete()
            if (remoteResult.isFailure) {
                // Remote failed, queue for later
                queueOperation(SyncOperationType.DELETE, entityId, serializedEntity)
            }
            Result.success(Unit)
        } else {
            // Queue for when network is available
            queueOperation(SyncOperationType.DELETE, entityId, serializedEntity)
            Result.success(Unit)
        }
    }

    /**
     * Fetch data with cache-first strategy
     */
    protected suspend fun <R> fetchWithCacheFirst(
        cacheLoad: suspend () -> Flow<R>,
        remoteLoad: suspend () -> Result<R>,
        cacheSave: suspend (R) -> Result<Unit>
    ): Flow<R> {
        // Always return cache first for immediate UI update
        val cachedFlow = cacheLoad()

        // Try to refresh from remote in background
        if (networkMonitor.isCurrentlyConnected()) {
            try {
                val remoteResult = remoteLoad()
                if (remoteResult.isSuccess) {
                    cacheSave(remoteResult.getOrThrow())
                }
            } catch (e: Exception) {
                // Ignore remote failures, we have cache
            }
        }

        return cachedFlow
    }

    /**
     * Queue an operation for later sync
     */
    private fun queueOperation(
        type: SyncOperationType,
        entityId: String,
        serializedData: String
    ): Result<Unit> {
        return try {
            requestQueue.enqueue(
                type = type,
                entityType = entityType,
                entityId = entityId,
                data = serializedData
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
