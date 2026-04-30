package com.example.moneybuddy2.data.repository

import com.example.moneybuddy2.data.model.FaqItem

class FaqRepository (
    private val faqs: List<FaqItem>
) {
    fun getFaqs(): List<FaqItem> = faqs

    fun categories(): List<String> =
        faqs.map { it.category }.distinct().sorted()
}
