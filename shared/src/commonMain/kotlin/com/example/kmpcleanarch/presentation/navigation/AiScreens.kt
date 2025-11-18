package com.example.kmpcleanarch.presentation.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.kmpcleanarch.presentation.ui.screen.AiChatScreen

/**
 * AI Chat Screen with Voyager
 */
class AiChatScreenVoyager : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        AiChatScreen(
            onBackClick = {
                navigator.pop()
            }
        )
    }
}
