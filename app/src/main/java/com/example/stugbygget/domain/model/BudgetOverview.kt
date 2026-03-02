package com.example.stugbygget.domain.model

data class BudgetOverview(
    val totalBudget: Double,
    val totalSpent: Double,
    val estimatedFinalCost: Double,
    val phaseBudgets: List<PhaseBudget>,
    val categoryBudgets: List<CategoryBudget>
)

data class PhaseBudget(
    val phaseId: String,
    val budgeted: Double,
    val spent: Double,
    val estimated: Double
)

data class CategoryBudget(
    val category: String,
    val spent: Double
)
