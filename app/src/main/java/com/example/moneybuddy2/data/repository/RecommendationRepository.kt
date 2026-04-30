package com.example.moneybuddy2.data.repository

import com.example.moneybuddy2.data.remote.AiRecommendationRequestDto
import com.example.moneybuddy2.data.remote.AiRecommendationResponseDto
import com.example.moneybuddy2.network.ApiClient

class AiRecommendationRepository(
    private val authRepository: AuthRepository
) {

    suspend fun getRecommendations(
        lifestyleNote: String?,
        priorities: List<String>,
        riskPreference: String?,
        savingGoalNote: String?
    ): AiRecommendationResponseDto {
        val idToken = authRepository.getIdToken()

        return ApiClient.api.recommendations(
            authorization = "Bearer $idToken",
            request = AiRecommendationRequestDto(
                lifestyleNote = lifestyleNote,
                priorities = priorities,
                riskPreference = riskPreference,
                savingGoalNote = savingGoalNote
            )
        )
    }
}

//import com.example.moneybuddy2.data.repository.AuthRepository
//import com.example.moneybuddy2.data.remote.RecommendationRequestDto
//import com.example.moneybuddy2.data.remote.RecommendationResponseDto
//import com.example.moneybuddy2.network.ApiClient
//
//class RecommendationRepository(
//    private val authRepository: AuthRepository
//) {
//
//    suspend fun getRecommendations(
//        lifestyleNote: String?,
//        priorities: List<String>,
//        riskPreference: String?,
//        savingGoalNote: String?
//    ): RecommendationResponseDto {
//        val idToken = authRepository.getIdToken()
//
//        return ApiClient.api.recommendations(
//            authorization = "Bearer $idToken",
//            request = RecommendationRequestDto(
//                lifestyleNote = lifestyleNote,
//                priorities = priorities,
//                riskPreference = riskPreference,
//                savingGoalNote = savingGoalNote
//            )
//        )
//    }
//}