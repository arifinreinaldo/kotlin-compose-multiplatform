package com.example.kmpcleanarch

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import com.example.kmpcleanarch.presentation.ui.screen.TaskListScreen
import com.example.kmpcleanarch.presentation.ui.util.getWindowSizeClass

/**
 * Main App Composable
 * Entry point for the Compose Multiplatform UI
 * Handles window size detection and theme
 */
@Composable
fun App() {
    MaterialTheme {
        BoxWithConstraints {
            val density = LocalDensity.current
            val width = with(density) { maxWidth }
            val windowSizeClass = getWindowSizeClass(width)

            TaskListScreen(windowSizeClass = windowSizeClass)
        }
    }
}
