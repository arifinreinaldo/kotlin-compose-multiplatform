package com.example.kmpcleanarch.data.remote

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Factory for creating configured HttpClient instances
 * Extension point for adding network layer to the application
 */
object HttpClientFactory {

    fun create(enableLogging: Boolean = true): HttpClient {
        return HttpClient {
            // JSON serialization
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }

            // Logging
            if (enableLogging) {
                install(Logging) {
                    logger = Logger.DEFAULT
                    level = LogLevel.INFO
                }
            }

            // TODO: Add authentication plugin when needed
            // install(Auth) { ... }

            // TODO: Add default request configuration
            // defaultRequest {
            //     url("https://api.example.com/")
            //     header("Content-Type", "application/json")
            // }
        }
    }
}
