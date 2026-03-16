package com.example.moneybuddy2.backend.service

import com.google.firebase.auth.FirebaseAuth

class AuthService {

    fun verifyAndGetUid(authHeader: String?): String {
        require(!authHeader.isNullOrBlank()) { "Missing Authorization header" }
        require(authHeader.startsWith("Bearer ")) { "Invalid Authorization header" }

        val idToken = authHeader.removePrefix("Bearer ").trim()
        val decoded = FirebaseAuth.getInstance().verifyIdToken(idToken)
        return decoded.uid
    }
}