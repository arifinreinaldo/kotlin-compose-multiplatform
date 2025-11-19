package com.example.kmpcleanarch.data.sync

import com.example.kmpcleanarch.domain.sync.PendingSyncOperation
import com.example.kmpcleanarch.domain.sync.SyncOperationType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

/**
 * Queue for managing offline operations that need to be synced
 *
 * Stores operations locally and processes them when network is available.
 * Provides ordering guarantees and automatic retry logic.
 */
class RequestQueue {

    private val _pendingOperations = MutableStateFlow<List<PendingSyncOperation>>(emptyList())
    val pendingOperations: Flow<List<PendingSyncOperation>> = _pendingOperations.asStateFlow()

    private val operationsMap = mutableMapOf<String, PendingSyncOperation>()

    /**
     * Add a new operation to the queue
     */
    fun enqueue(
        type: SyncOperationType,
        entityType: String,
        entityId: String,
        data: String,
        maxRetries: Int = 3
    ): String {
        val operation = PendingSyncOperation(
            id = UUID.randomUUID().toString(),
            type = type,
            entityType = entityType,
            entityId = entityId,
            data = data,
            timestamp = System.currentTimeMillis(),
            maxRetries = maxRetries
        )

        operationsMap[operation.id] = operation
        updateFlow()

        return operation.id
    }

    /**
     * Get all pending operations
     */
    fun getAll(): List<PendingSyncOperation> {
        return operationsMap.values.sortedBy { it.timestamp }
    }

    /**
     * Get operations by entity type
     */
    fun getByEntityType(entityType: String): List<PendingSyncOperation> {
        return operationsMap.values
            .filter { it.entityType == entityType }
            .sortedBy { it.timestamp }
    }

    /**
     * Get operation by ID
     */
    fun getById(id: String): PendingSyncOperation? {
        return operationsMap[id]
    }

    /**
     * Mark operation as completed and remove from queue
     */
    fun complete(operationId: String) {
        operationsMap.remove(operationId)
        updateFlow()
    }

    /**
     * Increment retry count for failed operation
     */
    fun incrementRetry(operationId: String): Boolean {
        val operation = operationsMap[operationId] ?: return false

        if (!operation.canRetry()) {
            // Max retries reached, remove from queue
            operationsMap.remove(operationId)
            updateFlow()
            return false
        }

        val updated = operation.copy(retryCount = operation.retryCount + 1)
        operationsMap[operationId] = updated
        updateFlow()
        return true
    }

    /**
     * Clear all operations from queue
     */
    fun clear() {
        operationsMap.clear()
        updateFlow()
    }

    /**
     * Remove specific operation
     */
    fun remove(operationId: String) {
        operationsMap.remove(operationId)
        updateFlow()
    }

    /**
     * Get count of pending operations
     */
    fun count(): Int = operationsMap.size

    /**
     * Check if queue is empty
     */
    fun isEmpty(): Boolean = operationsMap.isEmpty()

    /**
     * Check if queue has pending operations
     */
    fun hasPending(): Boolean = operationsMap.isNotEmpty()

    /**
     * Get operations that need retry (failed but can retry)
     */
    fun getRetryable(): List<PendingSyncOperation> {
        return operationsMap.values
            .filter { it.retryCount > 0 && it.canRetry() }
            .sortedBy { it.timestamp }
    }

    private fun updateFlow() {
        _pendingOperations.update { operationsMap.values.sortedBy { it.timestamp } }
    }
}
