package com.example.kmpcleanarch.domain.usecase

import com.example.kmpcleanarch.domain.repository.UserRepository

/**
 * Use case for deleting a user
 */
class DeleteUserUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: Long): Result<Unit> {
        return try {
            userRepository.deleteUser(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
