package com.example.kmpcleanarch.domain.util

/**
 * Extension functions for Kotlin's Result type
 * Makes it easier to work with Result in the domain layer
 */

/**
 * Map success value to another type
 */
inline fun <T, R> Result<T>.mapSuccess(transform: (T) -> R): Result<R> {
    return when {
        isSuccess -> Result.success(transform(getOrThrow()))
        else -> Result.failure(exceptionOrNull()!!)
    }
}

/**
 * Map failure exception to another exception
 */
inline fun <T> Result<T>.mapFailure(transform: (Throwable) -> Throwable): Result<T> {
    return when {
        isFailure -> Result.failure(transform(exceptionOrNull()!!))
        else -> this
    }
}

/**
 * Execute action if result is success
 */
inline fun <T> Result<T>.onSuccessSuspend(action: suspend (T) -> Unit): Result<T> {
    if (isSuccess) {
        kotlinx.coroutines.runBlocking {
            action(getOrThrow())
        }
    }
    return this
}

/**
 * Execute action if result is failure
 */
inline fun <T> Result<T>.onFailureSuspend(action: suspend (Throwable) -> Unit): Result<T> {
    if (isFailure) {
        kotlinx.coroutines.runBlocking {
            action(exceptionOrNull()!!)
        }
    }
    return this
}

/**
 * Fold result into a single value
 */
inline fun <T, R> Result<T>.foldResult(
    onSuccess: (T) -> R,
    onFailure: (Throwable) -> R
): R {
    return when {
        isSuccess -> onSuccess(getOrThrow())
        else -> onFailure(exceptionOrNull()!!)
    }
}

/**
 * Combine multiple results into a single result with a list
 */
fun <T> List<Result<T>>.combineResults(): Result<List<T>> {
    val failures = mapNotNull { it.exceptionOrNull() }
    if (failures.isNotEmpty()) {
        return Result.failure(failures.first())
    }
    return Result.success(mapNotNull { it.getOrNull() })
}

/**
 * Convert nullable value to Result
 */
fun <T : Any> T?.toResult(errorMessage: String = "Value is null"): Result<T> {
    return this?.let { Result.success(it) }
        ?: Result.failure(NullPointerException(errorMessage))
}

/**
 * Recover from failure with a default value
 */
fun <T> Result<T>.recoverWith(default: T): T {
    return getOrElse { default }
}

/**
 * Recover from failure with a function that provides a default value
 */
inline fun <T> Result<T>.recoverWithCatching(recovery: (Throwable) -> T): Result<T> {
    return if (isFailure) {
        runCatching { recovery(exceptionOrNull()!!) }
    } else {
        this
    }
}
