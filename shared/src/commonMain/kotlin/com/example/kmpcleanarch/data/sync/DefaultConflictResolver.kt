package com.example.kmpcleanarch.data.sync

import com.example.kmpcleanarch.domain.sync.ConflictResolution

/**
 * Default implementation of ConflictResolver
 *
 * Provides basic conflict resolution strategies.
 * Can be extended for custom domain-specific logic.
 */
class DefaultConflictResolver : ConflictResolver {

    override suspend fun <T> resolve(
        local: T,
        remote: T,
        strategy: ConflictResolution
    ): T {
        return when (strategy) {
            is ConflictResolution.UseLocal -> local
            is ConflictResolution.UseRemote -> remote
            is ConflictResolution.LastWriteWins -> resolveLastWriteWins(local, remote)
            is ConflictResolution.Merge -> strategy.mergeStrategy.merge(local, remote)
            is ConflictResolution.Manual -> {
                // In a real implementation, this would prompt the user
                // For now, default to remote
                remote
            }
        }
    }

    /**
     * Resolve using last-write-wins strategy
     *
     * Requires entities to have a timestamp or version field.
     * Uses reflection to find 'updatedAt' or 'version' fields.
     */
    private fun <T> resolveLastWriteWins(local: T, remote: T): T {
        // Try to get timestamp fields
        val localTimestamp = getTimestamp(local)
        val remoteTimestamp = getTimestamp(remote)

        return if (remoteTimestamp != null && localTimestamp != null) {
            if (remoteTimestamp > localTimestamp) remote else local
        } else {
            // Can't determine, default to remote (server wins)
            remote
        }
    }

    /**
     * Extract timestamp from entity
     * Looks for common timestamp field names
     */
    private fun <T> getTimestamp(entity: T): Long? {
        if (entity == null) return null

        return try {
            val clazz = entity!!::class

            // Try common field names
            val fieldNames = listOf("updatedAt", "timestamp", "lastModified", "modifiedAt")

            for (fieldName in fieldNames) {
                try {
                    @Suppress("UNCHECKED_CAST")
                    val member = clazz.members.find { it.name == fieldName }
                    if (member != null) {
                        val value = member.call(entity)
                        if (value is Long) return value
                    }
                } catch (e: Exception) {
                    // Continue to next field
                }
            }

            null
        } catch (e: Exception) {
            null
        }
    }
}
