package com.example.kmpcleanarch

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import com.example.kmpcleanarch.presentation.navigation.LoginScreenVoyager

/**
 * Sample App with Authentication Flow
 *
 * This demonstrates:
 * 1. Login Screen
 * 2. User List Screen (after login)
 * 3. User Form Screen (create/edit)
 *
 * Use this instead of App() to see the sample screens in action
 */
@Composable
fun SampleApp() {
    MaterialTheme {
        Navigator(LoginScreenVoyager())
    }
}
