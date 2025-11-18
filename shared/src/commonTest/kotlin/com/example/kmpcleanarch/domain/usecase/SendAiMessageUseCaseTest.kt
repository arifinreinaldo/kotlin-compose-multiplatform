package com.example.kmpcleanarch.domain.usecase

import com.example.kmpcleanarch.domain.model.AiMessage
import com.example.kmpcleanarch.domain.model.AiModel
import com.example.kmpcleanarch.domain.model.AiProvider
import com.example.kmpcleanarch.domain.model.MessageRole
import com.example.kmpcleanarch.domain.repository.AiRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SendAiMessageUseCaseTest {

    private val testModel = AiModel(
        id = "gpt-3.5-turbo",
        name = "GPT-3.5 Turbo",
        provider = AiProvider.OPENAI,
        maxTokens = 1000,
        temperature = 0.7f
    )

    @Test
    fun `should reject empty message`() = runTest {
        val fakeRepository = FakeAiRepository()
        val useCase = SendAiMessageUseCase(fakeRepository)

        val result = useCase("", emptyList(), testModel)

        assertTrue(result.isFailure)
        assertEquals("Message cannot be empty", result.exceptionOrNull()?.message)
    }

    @Test
    fun `should reject blank message`() = runTest {
        val fakeRepository = FakeAiRepository()
        val useCase = SendAiMessageUseCase(fakeRepository)

        val result = useCase("   ", emptyList(), testModel)

        assertTrue(result.isFailure)
        assertEquals("Message cannot be empty", result.exceptionOrNull()?.message)
    }

    @Test
    fun `should send message successfully`() = runTest {
        val fakeRepository = FakeAiRepository()
        val useCase = SendAiMessageUseCase(fakeRepository)

        val result = useCase("Hello, AI!", emptyList(), testModel)

        assertTrue(result.isSuccess)
        val message = result.getOrNull()!!
        assertEquals("Test AI response", message.content)
        assertEquals(MessageRole.ASSISTANT, message.role)
    }

    @Test
    fun `should pass conversation history to repository`() = runTest {
        val fakeRepository = FakeAiRepository()
        val useCase = SendAiMessageUseCase(fakeRepository)

        val conversationHistory = listOf(
            AiMessage(content = "Previous message", role = MessageRole.USER),
            AiMessage(content = "Previous response", role = MessageRole.ASSISTANT)
        )

        val result = useCase("New message", conversationHistory, testModel)

        assertTrue(result.isSuccess)
        assertEquals(conversationHistory, fakeRepository.lastConversationHistory)
    }

    @Test
    fun `should use correct model`() = runTest {
        val fakeRepository = FakeAiRepository()
        val useCase = SendAiMessageUseCase(fakeRepository)

        val customModel = testModel.copy(id = "gpt-4")

        useCase("Test message", emptyList(), customModel)

        assertEquals(customModel, fakeRepository.lastModel)
    }

    @Test
    fun `should propagate repository errors`() = runTest {
        val fakeRepository = FakeAiRepository(shouldFail = true)
        val useCase = SendAiMessageUseCase(fakeRepository)

        val result = useCase("Test message", emptyList(), testModel)

        assertTrue(result.isFailure)
        assertEquals("Repository error", result.exceptionOrNull()?.message)
    }
}

/**
 * Fake implementation of AiRepository for testing
 */
class FakeAiRepository(
    private val shouldFail: Boolean = false
) : AiRepository {

    var lastMessage: String? = null
    var lastConversationHistory: List<AiMessage>? = null
    var lastModel: AiModel? = null

    override suspend fun sendMessage(
        message: String,
        conversationHistory: List<AiMessage>,
        model: AiModel
    ): Result<AiMessage> {
        lastMessage = message
        lastConversationHistory = conversationHistory
        lastModel = model

        return if (shouldFail) {
            Result.failure(Exception("Repository error"))
        } else {
            Result.success(
                AiMessage(
                    content = "Test AI response",
                    role = MessageRole.ASSISTANT,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    override fun streamMessage(
        message: String,
        conversationHistory: List<AiMessage>,
        model: AiModel
    ): Flow<String> {
        return flowOf("Test stream response")
    }

    override suspend fun getAvailableModels(): Result<List<AiModel>> {
        return Result.success(
            listOf(
                AiModel(
                    id = "gpt-3.5-turbo",
                    name = "GPT-3.5 Turbo",
                    provider = AiProvider.OPENAI
                )
            )
        )
    }

    override suspend fun validateApiKey(apiKey: String): Result<Boolean> {
        return Result.success(!shouldFail)
    }
}
