package com.example.moneybuddy2.backend.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.Firestore
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.FirestoreClient
import java.io.FileInputStream

object FirebaseConfig {

    fun init() {

        val serviceAccount = FileInputStream("C:/Users/user/AndroidStudioProjects/MoneyBuddy2/backend/serviceAccountKey.json")

        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .build()

        FirebaseApp.initializeApp(options)
    }

    fun firestore(): Firestore {
        return FirestoreClient.getFirestore()
    }
}