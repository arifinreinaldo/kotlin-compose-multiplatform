package com.example.kmpcleanarch.data.sync

import com.example.kmpcleanarch.data.mapper.toDto
import com.example.kmpcleanarch.data.remote.api.TaskApi
import com.example.kmpcleanarch.domain.model.Task
import com.example.kmpcleanarch.domain.sync.PendingSyncOperation
import com.example.kmpcleanarch.domain.sync.SyncOperationType
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

/**
 * SyncExecutor implementation for Task entities
 *
 * Executes pending sync operations for tasks by calling the remote API.
 * This is an example implementation showing how to integrate sync with your API.
 */
class TaskSyncExecutor(
    private val taskApi: TaskApi,
    private val json: Json = Json { ignoreUnknownKeys = true }
) : SyncExecutor {

    override suspend fun execute(operation: PendingSyncOperation): Result<Unit> {
        if (operation.entityType != "Task") {
            return Result.failure(Exception("Unsupported entity type: ${operation.entityType}"))
        }

        return try {
            when (operation.type) {
                SyncOperationType.CREATE -> handleCreate(operation)
                SyncOperationType.UPDATE -> handleUpdate(operation)
                SyncOperationType.DELETE -> handleDelete(operation)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun handleCreate(operation: PendingSyncOperation): Result<Unit> {
        val task = json.decodeFromString<Task>(operation.data)
        val taskDto = task.toDto()
        return taskApi.createTask(taskDto).map { Unit }
    }

    private suspend fun handleUpdate(operation: PendingSyncOperation): Result<Unit> {
        val task = json.decodeFromString<Task>(operation.data)
        val taskDto = task.toDto()
        return taskApi.updateTask(operation.entityId, taskDto).map { Unit }
    }

    private suspend fun handleDelete(operation: PendingSyncOperation): Result<Unit> {
        return taskApi.deleteTask(operation.entityId)
    }
}
