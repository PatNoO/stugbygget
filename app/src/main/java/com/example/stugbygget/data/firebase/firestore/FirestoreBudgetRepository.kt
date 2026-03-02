package com.example.stugbygget.data.firebase.firestore

import com.example.stugbygget.domain.model.BudgetOverview
import com.example.stugbygget.domain.model.CategoryBudget
import com.example.stugbygget.domain.model.PhaseBudget
import com.example.stugbygget.domain.repository.BudgetRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

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
}
