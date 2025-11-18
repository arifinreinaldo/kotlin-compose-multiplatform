package com.example.kmpcleanarch.data.remote.api

import com.example.kmpcleanarch.data.remote.dto.ChatCompletionRequest
import com.example.kmpcleanarch.data.remote.dto.ChatCompletionResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * AI API client (OpenAI-compatible)
 *
 * Supports:
 * - OpenAI (https://api.openai.com/v1)
 * - Azure OpenAI
 * - Local LLM APIs (LM Studio, Ollama, etc.)
 * - Any OpenAI-compatible API
 */
class AiApi(
    private val httpClient: HttpClient,
    private val baseUrl: String,
    private val apiKey: String
) {
    /**
     * Send chat completion request
     */
    suspend fun chatCompletion(request: ChatCompletionRequest): Result<ChatCompletionResponse> {
        return try {
            val response = httpClient.post("$baseUrl/chat/completions") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $apiKey")
                setBody(request)
            }

            if (response.status.isSuccess()) {
                Result.success(response.body<ChatCompletionResponse>())
            } else {
                Result.failure(Exception("API error: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Validate API key by making a simple request
     */
    suspend fun validateApiKey(): Result<Boolean> {
        return try {
            val request = ChatCompletionRequest(
                model = "gpt-3.5-turbo",
                messages = listOf(
                    com.example.kmpcleanarch.data.remote.dto.ChatMessage(
                        role = "user",
                        content = "Hi"
                    )
                ),
                maxTokens = 5
            )

            chatCompletion(request)
                .map { true }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        // Common AI provider base URLs
        const val OPENAI_BASE_URL = "https://api.openai.com/v1"
        const val ANTHROPIC_BASE_URL = "https://api.anthropic.com/v1"

        // For local development/testing
        const val LOCAL_LM_STUDIO = "http://localhost:1234/v1"
        const val LOCAL_OLLAMA = "http://localhost:11434/v1"
    }
}
