package com.example.kmpcleanarch.presentation.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Window size class for responsive UI design
 * Adapts UI layout based on screen size
 */
enum class WindowSizeClass {
    COMPACT,  // Phones in portrait
    MEDIUM,   // Tablets in portrait, phones in landscape
    EXPANDED  // Tablets in landscape, desktops
}

/**
 * Determine window size class based on width
 */
fun getWindowSizeClass(width: Dp): WindowSizeClass {
    return when {
        width < 600.dp -> WindowSizeClass.COMPACT
        width < 840.dp -> WindowSizeClass.MEDIUM
        else -> WindowSizeClass.EXPANDED
    }
}

/**
 * Layout configuration based on window size
 */
data class LayoutConfig(
    val columns: Int,
    val spacing: Dp,
    val contentPadding: Dp,
    val cardElevation: Dp
)

fun getLayoutConfig(windowSize: WindowSizeClass): LayoutConfig {
    return when (windowSize) {
        WindowSizeClass.COMPACT -> LayoutConfig(
            columns = 1,
            spacing = 8.dp,
            contentPadding = 16.dp,
            cardElevation = 2.dp
        )
        WindowSizeClass.MEDIUM -> LayoutConfig(
            columns = 2,
            spacing = 12.dp,
            contentPadding = 24.dp,
            cardElevation = 4.dp
        )
        WindowSizeClass.EXPANDED -> LayoutConfig(
            columns = 3,
            spacing = 16.dp,
            contentPadding = 32.dp,
            cardElevation = 6.dp
        )
    }
}
