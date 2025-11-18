package com.example.kmpcleanarch.di

import com.example.kmpcleanarch.data.local.DatabaseDriverFactory
import com.example.kmpcleanarch.data.repository.AuthRepositoryImpl
import com.example.kmpcleanarch.data.repository.TaskRepositoryImpl
import com.example.kmpcleanarch.data.repository.UserRepositoryImpl
import com.example.kmpcleanarch.database.AppDatabase
import com.example.kmpcleanarch.domain.repository.AuthRepository
import com.example.kmpcleanarch.domain.repository.TaskRepository
import com.example.kmpcleanarch.domain.repository.UserRepository
import com.example.kmpcleanarch.domain.usecase.*
import com.example.kmpcleanarch.presentation.viewmodel.LoginViewModel
import com.example.kmpcleanarch.presentation.viewmodel.TaskViewModel
import com.example.kmpcleanarch.presentation.viewmodel.UserFormViewModel
import com.example.kmpcleanarch.presentation.viewmodel.UserListViewModel
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

    // Repositories
    singleOf(::TaskRepositoryImpl) bind TaskRepository::class
    singleOf(::AuthRepositoryImpl) bind AuthRepository::class
    singleOf(::UserRepositoryImpl) bind UserRepository::class

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

    // ViewModels
    factoryOf(::TaskViewModel)
    factoryOf(::LoginViewModel)
    factoryOf(::UserListViewModel)
    factoryOf(::UserFormViewModel)
}

/**
 * Platform-specific modules should provide DatabaseDriverFactory
 */
expect val platformModule: Module
