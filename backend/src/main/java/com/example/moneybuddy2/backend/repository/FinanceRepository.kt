package com.example.moneybuddy2.backend.repository

import com.example.moneybuddy2.backend.data.Expense
import com.example.moneybuddy2.backend.data.Income
import com.google.cloud.firestore.Firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FinanceRepository(
    private val db: Firestore
) {

    suspend fun getExpensesInRange(
        uid: String,
        startMillis: Long,
        endMillis: Long
    ): List<Expense> = withContext(Dispatchers.IO) {
        try {
            val snapshot = db.collection("users")
                .document(uid)
                .collection("expenses")
                .whereGreaterThanOrEqualTo("dateMillis", startMillis)
                .whereLessThanOrEqualTo("dateMillis", endMillis)
                .get()
                .get()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Expense::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getIncomesInRange(
        uid: String,
        startMillis: Long,
        endMillis: Long
    ): List<Income> = withContext(Dispatchers.IO) {
        try {
            val snapshot = db.collection("users")
                .document(uid)
                .collection("income")
                .whereGreaterThanOrEqualTo("dateMillis", startMillis)
                .whereLessThanOrEqualTo("dateMillis", endMillis)
                .get()
                .get()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Income::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

}