package com.example.kmpcleanarch.domain.usecase

import com.example.kmpcleanarch.domain.model.AiModel
import com.example.kmpcleanarch.domain.repository.AiRepository

/**
 * Use case for getting available AI models
 */
class GetAvailableAiModelsUseCase(
    private val aiRepository: AiRepository
) {
    suspend operator fun invoke(): Result<List<AiModel>> {
        return aiRepository.getAvailableModels()
    }
}
