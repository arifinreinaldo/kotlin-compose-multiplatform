package com.example.kmpcleanarch.domain.usecase

import com.example.kmpcleanarch.domain.model.User
import com.example.kmpcleanarch.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for getting all users
 */
class GetAllUsersUseCase(
    private val userRepository: UserRepository
) {
    operator fun invoke(): Flow<List<User>> {
        return userRepository.observeAllUsers()
    }
}
