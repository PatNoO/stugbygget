package com.example.stugbygget.data.firebase.firestore

import com.example.stugbygget.domain.model.BudgetOverview
import com.example.stugbygget.domain.model.CategoryBudget
import com.example.stugbygget.domain.model.PhaseBudget
import com.example.stugbygget.domain.repository.BudgetRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreBudgetRepository(
    private val firestore: FirebaseFirestore
) : BudgetRepository {
    override fun observeBudget(projectId: String): Flow<BudgetOverview> = callbackFlow {
        val document = firestore.collection("projects")
            .document(projectId)
            .collection("budget")
            .document("overview")

        val registration = document.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val data = snapshot?.data.orEmpty()
            val totalBudget = (data["totalBudget"] as? Number)?.toDouble() ?: 0.0
            val phaseMap = data["phases"] as? Map<*, *> ?: emptyMap<Any, Any>()
            val categoryMap = data["categories"] as? Map<*, *> ?: emptyMap<Any, Any>()

            val phaseBudgets = phaseMap.mapNotNull { (phaseId, value) ->
                val detail = value as? Map<*, *> ?: return@mapNotNull null
                PhaseBudget(
                    phaseId = phaseId?.toString().orEmpty(),
                    budgeted = (detail["budgeted"] as? Number)?.toDouble() ?: 0.0,
                    spent = (detail["spent"] as? Number)?.toDouble() ?: 0.0,
                    estimated = (detail["estimated"] as? Number)?.toDouble() ?: 0.0
                )
            }
            val categories = categoryMap.mapNotNull { (category, spent) ->
                val value = spent as? Number ?: return@mapNotNull null
                CategoryBudget(
                    category = category?.toString().orEmpty(),
                    spent = value.toDouble()
                )
            }

            val totalSpent = phaseBudgets.sumOf { it.spent }
            val estimatedFinalCost = phaseBudgets.sumOf { it.estimated }.coerceAtLeast(totalSpent)

            trySend(
                BudgetOverview(
                    totalBudget = totalBudget,
                    totalSpent = totalSpent,
                    estimatedFinalCost = estimatedFinalCost,
                    phaseBudgets = phaseBudgets.sortedBy { it.phaseId },
                    categoryBudgets = categories.sortedByDescending { it.spent }
                )
            )
        }

        awaitClose { registration.remove() }
    }

    override suspend fun setTotalBudget(projectId: String, totalBudget: Double) {
        overviewDoc(projectId)
            .set(mapOf("totalBudget" to totalBudget), SetOptions.merge())
            .await()
    }

    override suspend fun savePhaseBudget(projectId: String, phaseId: String, budgeted: Double) {
        overviewDoc(projectId)
            .set(mapOf("phases" to mapOf(phaseId to mapOf("budgeted" to budgeted))), SetOptions.merge())
            .await()
    }

    override suspend fun addExpense(projectId: String, phaseId: String, category: String, amount: Double) {
        overviewDoc(projectId)
            .set(
                mapOf(
                    "phases" to mapOf(phaseId to mapOf("spent" to FieldValue.increment(amount))),
                    "categories" to mapOf(category to FieldValue.increment(amount))
                ),
                SetOptions.merge()
            )
            .await()
    }

    private fun overviewDoc(projectId: String) =
        firestore.collection("projects").document(projectId).collection("budget").document("overview")
}
