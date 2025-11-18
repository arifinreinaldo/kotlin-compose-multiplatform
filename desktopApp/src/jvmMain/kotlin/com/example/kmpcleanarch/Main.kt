package com.example.kmpcleanarch

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.example.kmpcleanarch.di.commonModule
import com.example.kmpcleanarch.di.platformModule
import org.koin.core.context.startKoin

/**
 * Desktop Application Entry Point
 */
fun main() {
    // Initialize Koin
    startKoin {
        modules(platformModule, commonModule)
    }

    application {
        val windowState = rememberWindowState()

        Window(
            onCloseRequest = ::exitApplication,
            title = "KMP Clean Architecture",
            state = windowState
        ) {
            App()
        }
    }
}
