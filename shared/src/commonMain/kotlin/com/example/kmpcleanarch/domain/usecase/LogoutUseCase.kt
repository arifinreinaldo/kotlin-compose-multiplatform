package com.example.kmpcleanarch.domain.usecase

import com.example.kmpcleanarch.domain.repository.AuthRepository

/**
 * Use case for user logout
 */
class LogoutUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return authRepository.logout()
    }
}
