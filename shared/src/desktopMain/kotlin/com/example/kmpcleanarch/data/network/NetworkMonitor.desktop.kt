package com.example.kmpcleanarch.data.network

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.net.InetAddress
import java.net.NetworkInterface

/**
 * Desktop (JVM) implementation of NetworkMonitor
 */
class DesktopNetworkMonitor : NetworkMonitor {

    override val isConnected: Flow<Boolean> = flow {
        while (true) {
            emit(checkConnection())
            delay(5000) // Poll every 5 seconds
        }
    }

    override suspend fun isCurrentlyConnected(): Boolean {
        return checkConnection()
    }

    override suspend fun getNetworkType(): NetworkType {
        if (!checkConnection()) return NetworkType.NONE

        try {
            NetworkInterface.getNetworkInterfaces().toList().forEach { networkInterface ->
                if (networkInterface.isUp && !networkInterface.isLoopback) {
                    return when {
                        networkInterface.name.contains("eth", ignoreCase = true) -> NetworkType.ETHERNET
                        networkInterface.name.contains("wlan", ignoreCase = true) ||
                        networkInterface.name.contains("wifi", ignoreCase = true) -> NetworkType.WIFI
                        else -> NetworkType.UNKNOWN
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore
        }

        return NetworkType.UNKNOWN
    }

    private fun checkConnection(): Boolean {
        return try {
            // Try to reach Google DNS
            val address = InetAddress.getByName("8.8.8.8")
            address.isReachable(3000) // 3 second timeout
        } catch (e: Exception) {
            false
        }
    }
}

actual class NetworkMonitorFactory {
    actual fun create(): NetworkMonitor = DesktopNetworkMonitor()
}
