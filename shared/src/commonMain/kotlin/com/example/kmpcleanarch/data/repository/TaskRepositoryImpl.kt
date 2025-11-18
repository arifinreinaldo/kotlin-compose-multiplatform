package com.example.kmpcleanarch.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.example.kmpcleanarch.data.mapper.toDomainModel
import com.example.kmpcleanarch.database.AppDatabase
import com.example.kmpcleanarch.domain.model.Task
import com.example.kmpcleanarch.domain.repository.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Implementation of TaskRepository using SQLDelight as the single source of truth
 * All data operations go through the local database
 */
class TaskRepositoryImpl(
    private val database: AppDatabase
) : TaskRepository {

    private val queries = database.taskQueries

    override fun observeAllTasks(): Flow<List<Task>> {
        return queries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { entities -> entities.map { it.toDomainModel() } }
    }

    override fun observeTaskById(id: Long): Flow<Task?> {
        return queries.selectById(id)
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default)
            .map { it?.toDomainModel() }
    }

    override suspend fun getAllTasks(): List<Task> = withContext(Dispatchers.Default) {
        queries.selectAll()
            .executeAsList()
            .map { it.toDomainModel() }
    }

    override suspend fun getTaskById(id: Long): Task? = withContext(Dispatchers.Default) {
        queries.selectById(id)
            .executeAsOneOrNull()
            ?.toDomainModel()
    }

    override suspend fun insertTask(task: Task): Long = withContext(Dispatchers.Default) {
        queries.insert(
            title = task.title,
            description = task.description,
            isCompleted = task.isCompleted,
            createdAt = task.createdAt,
            updatedAt = task.updatedAt
        )
        queries.lastInsertRowId().executeAsOne()
    }

    override suspend fun updateTask(task: Task) = withContext(Dispatchers.Default) {
        queries.update(
            title = task.title,
            description = task.description,
            isCompleted = task.isCompleted,
            updatedAt = System.currentTimeMillis(),
            id = task.id
        )
    }

    override suspend fun deleteTask(id: Long) = withContext(Dispatchers.Default) {
        queries.deleteById(id)
    }

    override suspend fun deleteAllTasks() = withContext(Dispatchers.Default) {
        queries.deleteAll()
    }
}
