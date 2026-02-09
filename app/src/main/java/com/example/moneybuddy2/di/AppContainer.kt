package com.example.moneybuddy2.di

import android.content.Context
import com.example.moneybuddy2.core.carbon.CarbonCalculator
import com.example.moneybuddy2.core.carbon.EmissionFactors
import com.example.moneybuddy2.core.carbon.CarbonEstimator
import com.example.moneybuddy2.core.carbon.loadEmissionFactors
import com.example.moneybuddy2.core.recommendation.Recommendation
import com.example.moneybuddy2.data.model.CarbonFactors
import com.example.moneybuddy2.data.model.FaqItem
import com.example.moneybuddy2.data.repository.FaqRepository
import com.example.moneybuddy2.data.repository.MoneyRepository
import com.example.moneybuddy2.data.repository.MoneyRepositoryImpl
import com.example.moneybuddy2.ui.viewmodel.CarbonViewModel
import com.example.moneybuddy2.ui.viewmodel.OcrViewModel
import com.example.moneybuddy2.ui.viewmodel.ChatViewModel
import com.example.moneybuddy2.ui.viewmodel.RecommendationViewModel
import com.example.moneybuddy2.ui.viewmodel.ChatbotViewModel
import org.json.JSONArray


class AppContainer {
    private val carbonEstimator: CarbonEstimator by lazy {
        CarbonEstimator(
            factors = CarbonFactors(
                version = "2026-02",
                electricityKgPerKwh = 0.58,
                petrolKgPerLitre = 2.31
            ),
            petrolPriceRmPerLitre = 2.05,
            electricityRmPerKwh = 0.50 // optional; can be null if not used
        )
    }

    val repository: MoneyRepository by lazy {
        MoneyRepositoryImpl(carbonEstimator)
    }
    // Shared OCR state between pick + confirm screens
    //val ocrViewModel: OcrViewModel by lazy { OcrViewModel(repository) }
    val chatViewModel: ChatViewModel by lazy { ChatViewModel(repository) }
    val recommendationViewModel: RecommendationViewModel by lazy { RecommendationViewModel(repository) }

    private fun loadFaqs(context: Context): List<FaqItem> {
        val json = context.assets.open("company_faq.json")
            .bufferedReader()
            .use { it.readText() }

        val arr = JSONArray(json)
        val out = mutableListOf<FaqItem>()

        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val keywordsJson = o.getJSONArray("keywords")
            val keywords = mutableListOf<String>()
            for (k in 0 until keywordsJson.length()) {
                keywords.add(keywordsJson.getString(k))
            }

            out += FaqItem(
                id = o.getString("id"),
                category = o.getString("category"),
                question = o.getString("question"),
                keywords = keywords,
                answer = o.getString("answer")
            )
        }
        return out
    }

    fun createCompanyBotViewModel(context: Context): ChatbotViewModel {
        val faqs = loadFaqs(context)
        val faqRepo = FaqRepository(faqs)
        return ChatbotViewModel(faqRepo)
    }



}