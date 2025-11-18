package com.example.kmpcleanarch.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kmpcleanarch.domain.usecase.AddTaskUseCase
import com.example.kmpcleanarch.domain.usecase.DeleteTaskUseCase
import com.example.kmpcleanarch.domain.usecase.GetAllTasksUseCase
import com.example.kmpcleanarch.domain.usecase.ToggleTaskCompletionUseCase
import com.example.kmpcleanarch.presentation.state.TaskUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for Task management
 * Handles UI logic and state management
 * Communicates with the domain layer through use cases
 */
class TaskViewModel(
    private val getAllTasksUseCase: GetAllTasksUseCase,
    private val addTaskUseCase: AddTaskUseCase,
    private val toggleTaskCompletionUseCase: ToggleTaskCompletionUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

    init {
        loadTasks()
    }

    private fun loadTasks() {
        viewModelScope.launch {
            getAllTasksUseCase()
                .catch { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Unknown error occurred"
                        )
                    }
                }
                .collect { tasks ->
                    _uiState.update {
                        it.copy(
                            tasks = tasks,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    fun addTask(title: String, description: String) {
        viewModelScope.launch {
            addTaskUseCase(title, description)
                .onSuccess {
                    _uiState.update { it.copy(showAddDialog = false) }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(error = exception.message ?: "Failed to add task")
                    }
                }
        }
    }

    fun toggleTaskCompletion(taskId: Long) {
        viewModelScope.launch {
            toggleTaskCompletionUseCase(taskId)
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(error = exception.message ?: "Failed to update task")
                    }
                }
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            deleteTaskUseCase(taskId)
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(error = exception.message ?: "Failed to delete task")
                    }
                }
        }
    }

    fun showAddDialog() {
        _uiState.update { it.copy(showAddDialog = true) }
    }

    fun hideAddDialog() {
        _uiState.update { it.copy(showAddDialog = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
