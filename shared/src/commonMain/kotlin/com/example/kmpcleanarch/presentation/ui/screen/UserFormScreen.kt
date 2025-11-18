package com.example.kmpcleanarch.presentation.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.kmpcleanarch.presentation.viewmodel.UserFormViewModel
import org.koin.compose.koinInject

/**
 * User Form Screen
 * Used for both creating new users and editing existing users
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserFormScreen(
    userId: Long = 0,
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit,
    viewModel: UserFormViewModel = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Load user data if editing
    LaunchedEffect(userId) {
        if (userId > 0) {
            viewModel.loadUser(userId)
        }
    }

    // Navigate back on save success
    LaunchedEffect(uiState.isSaveSuccessful) {
        if (uiState.isSaveSuccessful) {
            onSaveSuccess()
            viewModel.resetSaveSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.title) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Text("←", style = MaterialTheme.typography.headlineMedium)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header
                    Text(
                        text = if (uiState.isEditMode) {
                            "Edit user information"
                        } else {
                            "Create a new user"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Name Field
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = { viewModel.onNameChanged(it) },
                        label = { Text("Full Name *") },
                        placeholder = { Text("John Doe") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !uiState.isSaving,
                        isError = uiState.error?.contains("name", ignoreCase = true) == true
                    )

                    // Email Field
                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = { viewModel.onEmailChanged(it) },
                        label = { Text("Email *") },
                        placeholder = { Text("john.doe@example.com") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !uiState.isSaving,
                        isError = uiState.error?.contains("email", ignoreCase = true) == true
                    )

                    // Role Selection
                    Text(
                        text = "Role *",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.availableRoles.forEach { role ->
                            FilterChip(
                                selected = uiState.role == role,
                                onClick = { viewModel.onRoleChanged(role) },
                                label = { Text(role.uppercase()) },
                                enabled = !uiState.isSaving,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Avatar URL Field (Optional)
                    OutlinedTextField(
                        value = uiState.avatarUrl,
                        onValueChange = { viewModel.onAvatarUrlChanged(it) },
                        label = { Text("Avatar URL (Optional)") },
                        placeholder = { Text("https://example.com/avatar.jpg") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !uiState.isSaving,
                        supportingText = { Text("URL to user's profile picture") }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Save Button
                    Button(
                        onClick = { viewModel.saveUser() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = !uiState.isSaving &&
                                uiState.name.isNotBlank() &&
                                uiState.email.isNotBlank() &&
                                uiState.role.isNotBlank()
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(
                                text = uiState.saveButtonText,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Cancel Button
                    OutlinedButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = !uiState.isSaving
                    ) {
                        Text(
                            text = "Cancel",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    // Required Fields Note
                    Text(
                        text = "* Required fields",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Error Message
                    if (uiState.error != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "⚠️",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = uiState.error!!,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    // Validation Info Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "📋 Validation Rules",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "• Name must be at least 2 characters",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "• Email must be a valid email address",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "• Role must be: admin, user, or moderator",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
