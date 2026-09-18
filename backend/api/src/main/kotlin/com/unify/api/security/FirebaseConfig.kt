package com.unify.api.security

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.ByteArrayInputStream
import java.util.Base64

@Configuration
class FirebaseConfig(@Value($$"${FIREBASE_CREDENTIALS}") private val firebaseCredentials: String) {
    @Bean
    fun firebaseAuth(): FirebaseAuth {
        val decoded = Base64.getDecoder().decode(firebaseCredentials)
        val credentials = GoogleCredentials.fromStream(ByteArrayInputStream(decoded))

        val options = FirebaseOptions.builder()
        .setCredentials(credentials)
        .build()

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options)
        }
        return FirebaseAuth.getInstance()
    }
}