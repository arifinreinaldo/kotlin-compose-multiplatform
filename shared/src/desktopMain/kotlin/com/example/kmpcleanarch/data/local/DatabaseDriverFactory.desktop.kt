package com.example.kmpcleanarch.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.example.kmpcleanarch.database.AppDatabase
import java.io.File

/**
 * Desktop implementation of DatabaseDriverFactory
 */
actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val databasePath = File(System.getProperty("user.home"), ".kmpcleanarch")
        databasePath.mkdirs()
        val databaseFile = File(databasePath, "app.db")

        val driver = JdbcSqliteDriver("jdbc:sqlite:${databaseFile.absolutePath}")
        AppDatabase.Schema.create(driver)
        return driver
    }
}
