package com.example.kmpcleanarch.domain.usecase

import com.example.kmpcleanarch.domain.model.AuthState
import com.example.kmpcleanarch.domain.repository.AuthRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LoginUseCaseTest {

    @Test
    fun `should reject empty email`() = runTest {
        val fakeRepository = FakeAuthRepository()
        val useCase = LoginUseCase(fakeRepository)

        val result = useCase("", "password123")

        assertTrue(result.isFailure)
        assertEquals("Email and password are required", result.exceptionOrNull()?.message)
    }

    @Test
    fun `should reject empty password`() = runTest {
        val fakeRepository = FakeAuthRepository()
        val useCase = LoginUseCase(fakeRepository)

        val result = useCase("test@example.com", "")

        assertTrue(result.isFailure)
        assertEquals("Email and password are required", result.exceptionOrNull()?.message)
    }

    @Test
    fun `should reject invalid email format`() = runTest {
        val fakeRepository = FakeAuthRepository()
        val useCase = LoginUseCase(fakeRepository)

        val result = useCase("invalid-email", "password123")

        assertTrue(result.isFailure)
        assertEquals("Invalid email format", result.exceptionOrNull()?.message)
    }

    @Test
    fun `should accept valid email formats`() = runTest {
        val fakeRepository = FakeAuthRepository()
        val useCase = LoginUseCase(fakeRepository)

        val validEmails = listOf(
            "test@example.com",
            "user.name@domain.co.uk",
            "user+tag@example.com",
            "123@numbers.com"
        )

        validEmails.forEach { email ->
            val result = useCase(email, "password123")
            assertTrue(result.isSuccess, "Email $email should be valid")
        }
    }

    @Test
    fun `should login successfully with valid credentials`() = runTest {
        val fakeRepository = FakeAuthRepository()
        val useCase = LoginUseCase(fakeRepository)

        val result = useCase("test@example.com", "password123")

        assertTrue(result.isSuccess)
        val authState = result.getOrNull()!!
        assertTrue(authState is AuthState.Authenticated)
        assertEquals("test@example.com", (authState as AuthState.Authenticated).user.email)
    }

    @Test
    fun `should propagate repository errors`() = runTest {
        val fakeRepository = FakeAuthRepository(shouldFail = true)
        val useCase = LoginUseCase(fakeRepository)

        val result = useCase("test@example.com", "password123")

        assertTrue(result.isFailure)
        assertEquals("Invalid credentials", result.exceptionOrNull()?.message)
    }

    @Test
    fun `should pass credentials to repository`() = runTest {
        val fakeRepository = FakeAuthRepository()
        val useCase = LoginUseCase(fakeRepository)

        useCase("test@example.com", "mypassword")

        assertEquals("test@example.com", fakeRepository.lastEmail)
        assertEquals("mypassword", fakeRepository.lastPassword)
    }
}

/**
 * Fake implementation of AuthRepository for testing
 */
class FakeAuthRepository(
    private val shouldFail: Boolean = false
) : AuthRepository {

    var lastEmail: String? = null
    var lastPassword: String? = null

    override suspend fun login(email: String, password: String): Result<AuthState> {
        lastEmail = email
        lastPassword = password

        return if (shouldFail) {
            Result.failure(Exception("Invalid credentials"))
        } else {
            Result.success(
                AuthState.Authenticated(
                    user = com.example.kmpcleanarch.domain.model.User(
                        id = 1,
                        email = email,
                        name = "Test User",
                        role = "User"
                    )
                )
            )
        }
    }

    override suspend fun logout(): Result<AuthState> {
        return Result.success(AuthState.Unauthenticated)
    }

    override suspend fun getCurrentAuthState(): AuthState {
        return AuthState.Unauthenticated
    }
}
