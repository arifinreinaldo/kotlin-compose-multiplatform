package com.example.kmpcleanarch.presentation.ui.util

/**
 * Utility extensions for string operations
 */

/**
 * Capitalize first letter of each word
 */
fun String.toTitleCase(): String {
    return split(" ").joinToString(" ") { word ->
        word.lowercase().replaceFirstChar { it.uppercase() }
    }
}

/**
 * Truncate string with ellipsis
 */
fun String.truncate(maxLength: Int, ellipsis: String = "..."): String {
    return if (length <= maxLength) {
        this
    } else {
        take(maxLength - ellipsis.length) + ellipsis
    }
}

/**
 * Check if string is a valid email (basic validation)
 */
fun String.isValidEmail(): Boolean {
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
    return matches(emailRegex)
}

/**
 * Mask string for privacy (e.g., credit cards, passwords)
 */
fun String.mask(visibleChars: Int = 4, maskChar: Char = '*'): String {
    return if (length <= visibleChars) {
        this
    } else {
        takeLast(visibleChars).padStart(length, maskChar)
    }
}

/**
 * Convert string to initials (e.g., "John Doe" -> "JD")
 */
fun String.toInitials(): String {
    return split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .map { it.first().uppercase() }
        .joinToString("")
}

/**
 * Remove HTML tags from string
 */
fun String.stripHtml(): String {
    return replace(Regex("<[^>]*>"), "")
}
