package com.example.kmpcleanarch.data.network

import kotlinx.coroutines.flow.Flow

/**
 * Network connectivity monitor
 *
 * Provides real-time network connectivity status across all platforms.
 * Uses platform-specific implementations via expect/actual pattern.
 */
interface NetworkMonitor {
    /**
     * Flow that emits network connectivity state
     * true = connected, false = disconnected
     */
    val isConnected: Flow<Boolean>

    /**
     * Current connectivity state (synchronous check)
     */
    suspend fun isCurrentlyConnected(): Boolean

    /**
     * Network type information
     */
    suspend fun getNetworkType(): NetworkType
}

/**
 * Types of network connections
 */
enum class NetworkType {
    WIFI,
    CELLULAR,
    ETHERNET,
    UNKNOWN,
    NONE
}

/**
 * Platform-specific NetworkMonitor factory
 */
expect class NetworkMonitorFactory {
    fun create(): NetworkMonitor
}
