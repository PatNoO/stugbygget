package com.example.stugbygget.data.firebase.firestore

import com.example.stugbygget.domain.model.ProjectChatContext
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreProjectContextProvider(
    private val firestore: FirebaseFirestore
) {
    suspend fun load(projectId: String): ProjectChatContext {
        val basePath = firestore.collection("projects").document(projectId)

        val phaseSnapshot = basePath.collection("phases").limit(8).get().await()
        val phases = phaseSnapshot.documents.mapNotNull { it.getString("name") }.ifEmpty {
            listOf("Rivning", "Tak", "El", "Fasad")
        }

        val budgetDoc = basePath.collection("budget").document("overview").get().await()
        val totalBudget = budgetDoc.getDouble("totalBudget")
        val budgetSummary = if (totalBudget != null) {
            "Total budget: ${totalBudget.toInt()} SEK"
        } else {
            "Budget ej satt"
        }

        val infoDoc = basePath.get().await()
        val projectName = infoDoc.getString("name") ?: "StugBygget"

        return ProjectChatContext(
            projectName = projectName,
            phaseNames = phases,
            budgetSummary = budgetSummary
        )
    }
}
