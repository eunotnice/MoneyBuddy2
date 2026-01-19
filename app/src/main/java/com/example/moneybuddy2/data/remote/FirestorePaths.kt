package com.example.moneybuddy2.data.remote

import com.example.moneybuddy2.core.Constants

object FirestorePaths {
    fun userDoc(uid: String) =
        FirebaseProvider.db.collection(Constants.USERS).document(uid)

    fun expenseCol(uid: String) =
        userDoc(uid).collection(Constants.EXPENSES)
}