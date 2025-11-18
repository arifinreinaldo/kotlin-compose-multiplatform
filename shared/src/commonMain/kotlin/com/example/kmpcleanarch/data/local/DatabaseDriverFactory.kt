package com.example.kmpcleanarch.data.local

import app.cash.sqldelight.db.SqlDriver

/**
 * Factory interface for creating platform-specific SQLite drivers
 * Each platform (Android, iOS, Desktop) will provide its own implementation
 */
expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}
