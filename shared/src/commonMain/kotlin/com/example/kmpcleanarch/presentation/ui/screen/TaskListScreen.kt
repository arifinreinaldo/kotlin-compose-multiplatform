package com.example.kmpcleanarch.presentation.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.kmpcleanarch.domain.model.Task
import com.example.kmpcleanarch.presentation.state.TaskUiState
import com.example.kmpcleanarch.presentation.ui.component.AddTaskDialog
import com.example.kmpcleanarch.presentation.ui.component.TaskCard
import com.example.kmpcleanarch.presentation.ui.util.WindowSizeClass
import com.example.kmpcleanarch.presentation.ui.util.getLayoutConfig
import com.example.kmpcleanarch.presentation.viewmodel.TaskViewModel
import org.koin.compose.koinInject

/**
 * Main Task List Screen with responsive design
 * Adapts layout based on screen size using WindowSizeClass
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    windowSizeClass: WindowSizeClass,
    viewModel: TaskViewModel = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()
    val layoutConfig = getLayoutConfig(windowSizeClass)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tasks") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddDialog() }
            ) {
                Text("+", style = MaterialTheme.typography.headlineMedium)
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.tasks.isEmpty() -> {
                    EmptyState(modifier = Modifier.align(Alignment.Center))
                }
                else -> {
                    TaskList(
                        tasks = uiState.tasks,
                        windowSizeClass = windowSizeClass,
                        layoutConfig = layoutConfig,
                        onTaskClick = { viewModel.toggleTaskCompletion(it.id) },
                        onDeleteClick = { viewModel.deleteTask(it.id) }
                    )
                }
            }

            // Error Snackbar
            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(error)
                }
            }
        }

        // Add Task Dialog
        if (uiState.showAddDialog) {
            AddTaskDialog(
                onDismiss = { viewModel.hideAddDialog() },
                onConfirm = { title, description ->
                    viewModel.addTask(title, description)
                }
            )
        }
    }
}

@Composable
private fun TaskList(
    tasks: List<Task>,
    windowSizeClass: WindowSizeClass,
    layoutConfig: com.example.kmpcleanarch.presentation.ui.util.LayoutConfig,
    onTaskClick: (Task) -> Unit,
    onDeleteClick: (Task) -> Unit
) {
    when (windowSizeClass) {
        WindowSizeClass.COMPACT -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(layoutConfig.contentPadding),
                verticalArrangement = Arrangement.spacedBy(layoutConfig.spacing)
            ) {
                items(tasks) { task ->
                    TaskCard(
                        task = task,
                        elevation = layoutConfig.cardElevation,
                        onClick = { onTaskClick(task) },
                        onDeleteClick = { onDeleteClick(task) }
                    )
                }
            }
        }
        WindowSizeClass.MEDIUM, WindowSizeClass.EXPANDED -> {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(layoutConfig.columns),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(layoutConfig.contentPadding),
                horizontalArrangement = Arrangement.spacedBy(layoutConfig.spacing),
                verticalItemSpacing = layoutConfig.spacing
            ) {
                items(tasks) { task ->
                    TaskCard(
                        task = task,
                        elevation = layoutConfig.cardElevation,
                        onClick = { onTaskClick(task) },
                        onDeleteClick = { onDeleteClick(task) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No tasks yet",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tap + to add a new task",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
