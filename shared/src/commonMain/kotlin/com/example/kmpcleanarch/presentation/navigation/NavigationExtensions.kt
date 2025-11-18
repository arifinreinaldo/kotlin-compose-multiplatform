package com.example.kmpcleanarch.presentation.navigation

import cafe.adriel.voyager.navigator.Navigator

/**
 * EXTENSION POINT: Navigation Helper Extensions
 *
 * Useful extensions for navigation operations
 */

/**
 * Navigate back to root screen and replace with new screen
 */
fun Navigator.replaceRoot(screen: cafe.adriel.voyager.core.screen.Screen) {
    replaceAll(screen)
}

/**
 * Pop back to a specific screen type
 */
inline fun <reified T : cafe.adriel.voyager.core.screen.Screen> Navigator.popUntil(): Boolean {
    while (items.isNotEmpty()) {
        if (lastItem is T) {
            return true
        }
        pop()
    }
    return false
}

/**
 * Check if a specific screen type is in the back stack
 */
inline fun <reified T : cafe.adriel.voyager.core.screen.Screen> Navigator.contains(): Boolean {
    return items.any { it is T }
}

/**
 * Example usage in a screen:
 *
 * ```
 * @Composable
 * override fun Content() {
 *     val navigator = LocalNavigator.currentOrThrow
 *
 *     Button(onClick = {
 *         // Simple navigation
 *         navigator.push(TaskDetailScreenVoyager(taskId = 123))
 *     }) {
 *         Text("View Details")
 *     }
 *
 *     Button(onClick = {
 *         // Navigate back
 *         navigator.pop()
 *     }) {
 *         Text("Back")
 *     }
 *
 *     Button(onClick = {
 *         // Replace current screen
 *         navigator.replace(SettingsScreenVoyager())
 *     }) {
 *         Text("Go to Settings")
 *     }
 *
 *     Button(onClick = {
 *         // Clear back stack and go to home
 *         navigator.replaceRoot(TaskListScreenVoyager())
 *     }) {
 *         Text("Home")
 *     }
 * }
 * ```
 */
