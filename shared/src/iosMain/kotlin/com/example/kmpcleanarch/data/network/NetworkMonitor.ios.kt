package com.example.kmpcleanarch.data.network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Foundation.*
import platform.SystemConfiguration.*

/**
 * iOS implementation of NetworkMonitor using Reachability
 */
class IosNetworkMonitor : NetworkMonitor {

    private val _isConnected = MutableStateFlow(checkConnection())

    override val isConnected: Flow<Boolean> = _isConnected.asStateFlow()

    override suspend fun isCurrentlyConnected(): Boolean {
        return checkConnection()
    }

    override suspend fun getNetworkType(): NetworkType {
        if (!checkConnection()) return NetworkType.NONE

        // iOS doesn't provide easy API to distinguish network types
        // Would need NWPathMonitor for detailed info
        return NetworkType.UNKNOWN
    }

    private fun checkConnection(): Boolean {
        var zeroAddress = sockaddr_in()
        zeroAddress.sin_len = sizeOf<sockaddr_in>().toUByte()
        zeroAddress.sin_family = AF_INET.toUByte()

        val reachability = SCNetworkReachabilityCreateWithAddress(
            null,
            @Suppress("UNCHECKED_CAST")
            ((&zeroAddress) as CPointer<sockaddr>)
        ) ?: return false

        var flags: SCNetworkReachabilityFlags = 0u
        if (!SCNetworkReachabilityGetFlags(reachability, &flags)) {
            return false
        }

        val isReachable = (flags and kSCNetworkReachabilityFlagsReachable.toUInt()) != 0u
        val needsConnection = (flags and kSCNetworkReachabilityFlagsConnectionRequired.toUInt()) != 0u

        return isReachable && !needsConnection
    }
}

actual class NetworkMonitorFactory {
    actual fun create(): NetworkMonitor = IosNetworkMonitor()
}
