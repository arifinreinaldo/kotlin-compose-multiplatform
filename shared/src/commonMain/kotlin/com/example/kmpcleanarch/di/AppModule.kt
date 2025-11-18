package com.example.kmpcleanarch.di

import com.example.kmpcleanarch.data.local.DatabaseDriverFactory
import com.example.kmpcleanarch.data.repository.TaskRepositoryImpl
import com.example.kmpcleanarch.database.AppDatabase
import com.example.kmpcleanarch.domain.repository.TaskRepository
import com.example.kmpcleanarch.domain.usecase.AddTaskUseCase
import com.example.kmpcleanarch.domain.usecase.DeleteTaskUseCase
import com.example.kmpcleanarch.domain.usecase.GetAllTasksUseCase
import com.example.kmpcleanarch.domain.usecase.ToggleTaskCompletionUseCase
import com.example.kmpcleanarch.presentation.viewmodel.TaskViewModel
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
    // Database
    single {
        val driver = get<DatabaseDriverFactory>().createDriver()
        AppDatabase(driver)
    }

    // Repository
    singleOf(::TaskRepositoryImpl) bind TaskRepository::class

    // Use Cases
    factoryOf(::GetAllTasksUseCase)
    factoryOf(::AddTaskUseCase)
    factoryOf(::ToggleTaskCompletionUseCase)
    factoryOf(::DeleteTaskUseCase)

    // ViewModel
    factoryOf(::TaskViewModel)
}

/**
 * Platform-specific modules should provide DatabaseDriverFactory
 */
expect val platformModule: Module
