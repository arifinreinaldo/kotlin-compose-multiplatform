package com.example.kmpcleanarch.presentation.ui.util

/**
 * Generic class for handling resource states in the UI
 * Useful for network requests or any async operations
 *
 * EXTENSION POINT: Use this for API call states
 *
 * Example usage:
 * ```
 * sealed class TaskUiState {
 *     data class Tasks(val tasks: List<Task>) : TaskUiState()
 *     data object Loading : TaskUiState()
 *     data class Error(val message: String) : TaskUiState()
 * }
 *
 * // Or use ResourceState directly:
 * val tasksState = MutableStateFlow<ResourceState<List<Task>>>(ResourceState.Loading)
 *
 * // In UI:
 * when (val state = tasksState.value) {
 *     is ResourceState.Loading -> LoadingIndicator()
 *     is ResourceState.Success -> TaskList(state.data)
 *     is ResourceState.Error -> ErrorMessage(state.message)
 * }
 * ```
 */
sealed class ResourceState<out T> {
    /**
     * Loading state
     */
    data object Loading : ResourceState<Nothing>()

    /**
     * Success state with data
     */
    data class Success<T>(val data: T) : ResourceState<T>()

    /**
     * Error state with message and optional throwable
     */
    data class Error(
        val message: String,
        val throwable: Throwable? = null
    ) : ResourceState<Nothing>()

    /**
     * Empty state (e.g., no data available)
     */
    data object Empty : ResourceState<Nothing>()
}

/**
 * Extension functions for ResourceState
 */

/**
 * Get data if success, null otherwise
 */
fun <T> ResourceState<T>.getDataOrNull(): T? {
    return when (this) {
        is ResourceState.Success -> data
        else -> null
    }
}

/**
 * Check if state is loading
 */
fun <T> ResourceState<T>.isLoading(): Boolean {
    return this is ResourceState.Loading
}

/**
 * Check if state is success
 */
fun <T> ResourceState<T>.isSuccess(): Boolean {
    return this is ResourceState.Success
}

/**
 * Check if state is error
 */
fun <T> ResourceState<T>.isError(): Boolean {
    return this is ResourceState.Error
}

/**
 * Map success data to another type
 */
fun <T, R> ResourceState<T>.map(transform: (T) -> R): ResourceState<R> {
    return when (this) {
        is ResourceState.Success -> ResourceState.Success(transform(data))
        is ResourceState.Error -> ResourceState.Error(message, throwable)
        is ResourceState.Loading -> ResourceState.Loading
        is ResourceState.Empty -> ResourceState.Empty
    }
}
