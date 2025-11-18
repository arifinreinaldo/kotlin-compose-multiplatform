package com.example.kmpcleanarch.presentation.navigation

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.kmpcleanarch.presentation.ui.screen.LoginScreen
import com.example.kmpcleanarch.presentation.ui.screen.UserFormScreen
import com.example.kmpcleanarch.presentation.ui.screen.UserListScreen
import com.example.kmpcleanarch.presentation.ui.util.getWindowSizeClass

/**
 * Sample Navigation Screens demonstrating:
 * 1. Login Screen
 * 2. User List Screen (display list)
 * 3. User Form Screen (create/edit form)
 */

/**
 * Login Screen with Voyager
 */
class LoginScreenVoyager : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        LoginScreen(
            onLoginSuccess = {
                // Navigate to user list after successful login
                navigator.replace(UserListScreenVoyager())
            }
        )
    }
}

/**
 * User List Screen with Voyager
 * Displays a list of users
 */
class UserListScreenVoyager : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        UserListScreen(
            onAddUserClick = {
                // Navigate to form for creating new user
                navigator.push(UserFormScreenVoyager())
            },
            onEditUserClick = { userId ->
                // Navigate to form for editing existing user
                navigator.push(UserFormScreenVoyager(userId))
            },
            onLogout = {
                // Go back to login screen
                navigator.replaceAll(LoginScreenVoyager())
            }
        )
    }
}

/**
 * User Form Screen with Voyager
 * For creating new or editing existing users
 */
data class UserFormScreenVoyager(val userId: Long = 0) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        UserFormScreen(
            userId = userId,
            onBackClick = {
                navigator.pop()
            },
            onSaveSuccess = {
                // Go back to list after saving
                navigator.pop()
            }
        )
    }
}
