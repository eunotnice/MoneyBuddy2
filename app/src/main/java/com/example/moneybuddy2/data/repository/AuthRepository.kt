package com.example.moneybuddy2.data.repository

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class AuthRepository {

    suspend fun getIdToken(): String {

        val user = FirebaseAuth.getInstance().currentUser
            ?: throw Exception("User not logged in")

        val tokenResult = user.getIdToken(true).await()

        return tokenResult.token ?: throw Exception("Token null")
    }
}