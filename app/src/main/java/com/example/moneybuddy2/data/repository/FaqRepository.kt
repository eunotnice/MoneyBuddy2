package com.example.moneybuddy2.data.repository

import android.content.Context
import com.example.moneybuddy2.data.model.FaqItem
import org.json.JSONArray
import java.io.BufferedReader

class FaqRepository (
    private val faqs: List<FaqItem>
) {
    fun getFaqs(): List<FaqItem> = faqs

    fun categories(): List<String> =
        faqs.map { it.category }.distinct().sorted()
}
