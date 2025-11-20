package com.example.kmpcleanarch.di

import com.example.kmpcleanarch.data.local.DatabaseDriverFactory
import com.example.kmpcleanarch.data.network.NetworkMonitor
import com.example.kmpcleanarch.data.network.NetworkMonitorFactory
import com.example.kmpcleanarch.data.remote.api.AiApi
import com.example.kmpcleanarch.data.remote.api.MockTaskApi
import com.example.kmpcleanarch.data.remote.api.TaskApi
import com.example.kmpcleanarch.data.remote.HttpClientFactory
import com.example.kmpcleanarch.data.repository.AiRepositoryImpl
import com.example.kmpcleanarch.data.repository.AuthRepositoryImpl
import com.example.kmpcleanarch.data.repository.TaskRepositoryImpl
import com.example.kmpcleanarch.data.repository.UserRepositoryImpl
import com.example.kmpcleanarch.data.sync.*
import com.example.kmpcleanarch.database.AppDatabase
import com.example.kmpcleanarch.domain.repository.AiRepository
import com.example.kmpcleanarch.domain.repository.AuthRepository
import com.example.kmpcleanarch.domain.repository.TaskRepository
import com.example.kmpcleanarch.domain.repository.UserRepository
import com.example.kmpcleanarch.domain.usecase.*
import com.example.kmpcleanarch.presentation.viewmodel.AiChatViewModel
import com.example.kmpcleanarch.presentation.viewmodel.LoginViewModel
import com.example.kmpcleanarch.presentation.viewmodel.TaskViewModel
import com.example.kmpcleanarch.presentation.viewmodel.UserFormViewModel
import com.example.kmpcleanarch.presentation.viewmodel.UserListViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Common Koin module for dependency injection
 * Defines all dependencies for the application
 */
val commonModule = module {
    // HTTP Client for AI API
    single {
        HttpClientFactory.create(enableLogging = true)
    }

    // AI API (default configuration - update with your API key)
    single {
        AiApi(
            httpClient = get(),
            baseUrl = AiApi.OPENAI_BASE_URL,
            apiKey = "" // Set via AiChatViewModel.setApiKey()
        )
    }

    // Task API (Mock implementation - replace with real API when ready)
    single<TaskApi> {
        MockTaskApi()
    }

    // Database
    single {
        val driver = get<DatabaseDriverFactory>().createDriver()
        AppDatabase(driver)
    }

    // Repositories
    singleOf(::TaskRepositoryImpl) bind TaskRepository::class
    singleOf(::AuthRepositoryImpl) bind AuthRepository::class
    singleOf(::UserRepositoryImpl) bind UserRepository::class
    singleOf(::AiRepositoryImpl) bind AiRepository::class

    // Task Use Cases
    factoryOf(::GetAllTasksUseCase)
    factoryOf(::AddTaskUseCase)
    factoryOf(::ToggleTaskCompletionUseCase)
    factoryOf(::DeleteTaskUseCase)

    // Auth Use Cases
    factoryOf(::LoginUseCase)
    factoryOf(::LogoutUseCase)

    // User Use Cases
    factoryOf(::GetAllUsersUseCase)
    factoryOf(::SaveUserUseCase)
    factoryOf(::DeleteUserUseCase)

    // AI Use Cases
    factoryOf(::SendAiMessageUseCase)
    factoryOf(::GetAvailableAiModelsUseCase)

    // ViewModels
    factoryOf(::TaskViewModel)
    factoryOf(::LoginViewModel)
    factoryOf(::UserListViewModel)
    factoryOf(::UserFormViewModel)
    factoryOf(::AiChatViewModel)

    // Offline-First & Sync Infrastructure
    // Network Monitor (platform-specific via expect/actual)
    single<NetworkMonitor> {
        get<NetworkMonitorFactory>().create()
    }

    // Request Queue for offline operations
    single { RequestQueue() }

    // Conflict Resolver
    single<ConflictResolver> { DefaultConflictResolver() }

    // Sync Executor for Tasks
    single<SyncExecutor> {
        TaskSyncExecutor(
            taskApi = get(),
            json = kotlinx.serialization.json.Json {
                ignoreUnknownKeys = true
                isLenient = true
            }
        )
    }

    // Coroutine Scope for sync operations
    single {
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    // Sync Manager
    single {
        SyncManager(
            networkMonitor = get(),
            requestQueue = get(),
            syncExecutor = get(),
            conflictResolver = get(),
            coroutineScope = get()
        )
    }
}

/**
 * Platform-specific modules should provide DatabaseDriverFactory
 */
expect val platformModule: Module
