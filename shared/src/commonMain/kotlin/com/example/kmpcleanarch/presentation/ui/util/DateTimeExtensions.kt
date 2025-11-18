package com.example.kmpcleanarch.presentation.ui.util

import kotlinx.datetime.*

/**
 * Utility extensions for date and time formatting
 * Uses kotlinx-datetime for multiplatform support
 */

/**
 * Format timestamp to human-readable string
 */
fun Long.toFormattedDateTime(): String {
    val instant = Instant.fromEpochMilliseconds(this)
    val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return dateTime.toString()
}

/**
 * Format timestamp to date only
 */
fun Long.toFormattedDate(): String {
    val instant = Instant.fromEpochMilliseconds(this)
    val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val date = dateTime.date
    return "${date.dayOfMonth}/${date.monthNumber}/${date.year}"
}

/**
 * Format timestamp to time only
 */
fun Long.toFormattedTime(): String {
    val instant = Instant.fromEpochMilliseconds(this)
    val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val hour = dateTime.hour.toString().padStart(2, '0')
    val minute = dateTime.minute.toString().padStart(2, '0')
    return "$hour:$minute"
}

/**
 * Get relative time string (e.g., "2 hours ago")
 */
fun Long.toRelativeTime(): String {
    val now = Clock.System.now()
    val instant = Instant.fromEpochMilliseconds(this)
    val duration = now - instant

    return when {
        duration.inWholeSeconds < 60 -> "Just now"
        duration.inWholeMinutes < 60 -> "${duration.inWholeMinutes} minute${if (duration.inWholeMinutes != 1L) "s" else ""} ago"
        duration.inWholeHours < 24 -> "${duration.inWholeHours} hour${if (duration.inWholeHours != 1L) "s" else ""} ago"
        duration.inWholeDays < 7 -> "${duration.inWholeDays} day${if (duration.inWholeDays != 1L) "s" else ""} ago"
        duration.inWholeDays < 30 -> "${duration.inWholeDays / 7} week${if (duration.inWholeDays / 7 != 1L) "s" else ""} ago"
        duration.inWholeDays < 365 -> "${duration.inWholeDays / 30} month${if (duration.inWholeDays / 30 != 1L) "s" else ""} ago"
        else -> "${duration.inWholeDays / 365} year${if (duration.inWholeDays / 365 != 1L) "s" else ""} ago"
    }
}

/**
 * Check if timestamp is today
 */
fun Long.isToday(): Boolean {
    val now = Clock.System.now()
    val instant = Instant.fromEpochMilliseconds(this)

    val nowDate = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
    val thisDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date

    return nowDate == thisDate
}

/**
 * Check if timestamp is within last N days
 */
fun Long.isWithinDays(days: Int): Boolean {
    val now = Clock.System.now()
    val instant = Instant.fromEpochMilliseconds(this)
    val duration = now - instant

    return duration.inWholeDays < days
}
