package com.example.moneybuddy2.backend.util

import com.example.moneybuddy2.backend.data.BudgetBucket

fun categoryToBucket(category: String): BudgetBucket {
    return when (category) {
        "Food & Drink" -> BudgetBucket.NEEDS
        "Groceries" -> BudgetBucket.NEEDS
        "Transport" -> BudgetBucket.NEEDS
        "Utilities" -> BudgetBucket.NEEDS
        "Health" -> BudgetBucket.NEEDS
        "Education" -> BudgetBucket.NEEDS
        "Services" -> BudgetBucket.NEEDS

        "Shopping" -> BudgetBucket.WANTS
        "Entertainment" -> BudgetBucket.WANTS
        "Travel" -> BudgetBucket.WANTS

        else -> BudgetBucket.UNCATEGORISED
    }
}