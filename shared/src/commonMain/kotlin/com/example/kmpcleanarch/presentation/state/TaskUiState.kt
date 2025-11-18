package com.example.kmpcleanarch.presentation.state

import com.example.kmpcleanarch.domain.model.Task

/**
 * UI State for the Task screen
 * Represents the state of the UI at any given time
 */
data class TaskUiState(
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddDialog: Boolean = false
)
