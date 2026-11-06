package com.example.prog7314p2.util

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException

object UserProfile {

    suspend fun reviewerName(): String? {
        val user = FirebaseAuth.getInstance().currentUser ?: return null

        val (first, last) = try {
            val doc = FirebaseFirestore.getInstance().collection("users").document(user.uid).get().awaitResult()
            doc.getString("name")?.trim().orEmpty() to doc.getString("surname")?.trim().orEmpty()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            "" to ""
        }

        if (first.isNotEmpty()) return format(first, last)

        val parts = user.displayName?.trim()?.split(Regex("\\s+")).orEmpty().filter { it.isNotEmpty() }
        return if (parts.isNotEmpty()) format(parts.first(), parts.drop(1).lastOrNull().orEmpty()) else null
    }

    private fun format(first: String, last: String): String =
        if (last.isEmpty()) first else "$first ${last.first().uppercaseChar()}."
}
