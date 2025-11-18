package com.example.kmpcleanarch.di

import com.example.kmpcleanarch.data.local.DatabaseDriverFactory
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * iOS platform module
 * Provides iOS-specific dependencies like DatabaseDriverFactory
 */
actual val platformModule: Module = module {
    single { DatabaseDriverFactory() }
}
