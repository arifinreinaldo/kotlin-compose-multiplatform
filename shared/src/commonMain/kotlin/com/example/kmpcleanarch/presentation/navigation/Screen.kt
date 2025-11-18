package com.example.kmpcleanarch.presentation.navigation

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import com.example.kmpcleanarch.presentation.ui.screen.TaskListScreen
import com.example.kmpcleanarch.presentation.ui.util.getWindowSizeClass

/**
 * EXTENSION POINT: Navigation Setup with Voyager
 *
 * This demonstrates how to add navigation to your app using Voyager.
 *
 * To enable navigation:
 * 1. Replace the simple App() composable with NavigationApp()
 * 2. Create additional Screen implementations for new features
 * 3. Use navigator.push() to navigate to new screens
 *
 * Benefits of Voyager:
 * - Type-safe navigation
 * - Lifecycle aware
 * - Multi-module support
 * - Animations and transitions
 * - Tab navigation support
 */

/**
 * Main navigation setup
 * Replace App() with this in your platform entry points to enable navigation
 */
@Composable
fun NavigationApp() {
    Navigator(TaskListScreenVoyager())
}

/**
 * Voyager Screen wrapper for TaskListScreen
 */
class TaskListScreenVoyager : Screen {
    @Composable
    override fun Content() {
        BoxWithConstraints {
            val density = LocalDensity.current
            val width = with(density) { maxWidth }
            val windowSizeClass = getWindowSizeClass(width)

            TaskListScreen(windowSizeClass = windowSizeClass)
        }
    }
}

/**
 * Example: Task Detail Screen
 *
 * Uncomment and implement when you need a detail screen:
 *
 * ```
 * data class TaskDetailScreenVoyager(val taskId: Long) : Screen {
 *     @Composable
 *     override fun Content() {
 *         val navigator = LocalNavigator.currentOrThrow
 *
 *         TaskDetailScreen(
 *             taskId = taskId,
 *             onBackClick = { navigator.pop() }
 *         )
 *     }
 * }
 * ```
 *
 * Navigate to detail screen:
 * ```
 * navigator.push(TaskDetailScreenVoyager(taskId = task.id))
 * ```
 */

/**
 * Example: Settings Screen
 *
 * ```
 * class SettingsScreenVoyager : Screen {
 *     @Composable
 *     override fun Content() {
 *         SettingsScreen()
 *     }
 * }
 * ```
 */

/**
 * Example: Tab Navigation
 *
 * ```
 * object TasksTab : Tab {
 *     override val options: TabOptions
 *         @Composable
 *         get() = TabOptions(
 *             index = 0u,
 *             title = "Tasks",
 *             icon = rememberVectorPainter(Icons.Default.List)
 *         )
 *
 *     @Composable
 *     override fun Content() {
 *         TaskListScreenVoyager().Content()
 *     }
 * }
 *
 * object SettingsTab : Tab {
 *     override val options: TabOptions
 *         @Composable
 *         get() = TabOptions(
 *             index = 1u,
 *             title = "Settings",
 *             icon = rememberVectorPainter(Icons.Default.Settings)
 *         )
 *
 *     @Composable
 *     override fun Content() {
 *         SettingsScreenVoyager().Content()
 *     }
 * }
 *
 * // Use in your app:
 * TabNavigator(tab = TasksTab) {
 *     Scaffold(
 *         bottomBar = {
 *             NavigationBar {
 *                 TabNavigationItem(TasksTab)
 *                 TabNavigationItem(SettingsTab)
 *             }
 *         }
 *     ) {
 *         CurrentTab()
 *     }
 * }
 * ```
 */
