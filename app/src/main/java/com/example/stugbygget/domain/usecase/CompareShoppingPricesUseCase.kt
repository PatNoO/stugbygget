package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.BestSplitLine
import com.example.stugbygget.domain.model.PriceComparisonResult
import com.example.stugbygget.domain.model.PriceQuote
import com.example.stugbygget.domain.model.ShoppingList
import com.example.stugbygget.domain.model.StoreTotal
import com.example.stugbygget.domain.repository.PriceRecommendationRepository

class CompareShoppingPricesUseCase(
    private val recommendationRepository: PriceRecommendationRepository
) {

    /**
     * Computes:
     * 1) all single-store totals where every item is available,
     * 2) the cheapest valid single-store option,
     * 3) the best split where each item is bought from the cheapest in-stock store.
     * Saves a snapshot for auditability after calculation.
     */
    suspend operator fun invoke(
        projectId: String,
        shoppingList: ShoppingList,
        quotes: List<PriceQuote>
    ): PriceComparisonResult {
        val singleStoreTotals = buildSingleStoreTotals(shoppingList, quotes).sortedBy { it.totalCost }
        val bestSplitLines = buildBestSplitLines(shoppingList, quotes)
        val bestSplitTotal = bestSplitLines.sumOf { line ->
            val quantity = shoppingList.items.firstOrNull { it.name == line.itemName }?.quantity ?: 1.0
            quantity * line.unitPrice
        }

        val result = PriceComparisonResult(
            singleStoreTotals = singleStoreTotals,
            cheapestSingleStore = singleStoreTotals.firstOrNull(),
            bestSplitLines = bestSplitLines,
            bestSplitTotal = bestSplitTotal
        )

        recommendationRepository.saveSnapshot(
            projectId = projectId,
            shoppingListId = shoppingList.id,
            result = result
        )
        return result
    }

    private fun buildSingleStoreTotals(
        shoppingList: ShoppingList,
        quotes: List<PriceQuote>
    ): List<StoreTotal> {
        val stores = quotes.map { it.store }.distinct()
        return stores.mapNotNull { store ->
            var total = 0.0
            for (item in shoppingList.items) {
                val quote = quotes
                    .asSequence()
                    .filter { it.store == store && it.materialName == item.name && it.inStock }
                    .minByOrNull { it.unitPrice }
                    ?: return@mapNotNull null
                total += item.quantity * quote.unitPrice
            }
            StoreTotal(store = store, totalCost = total)
        }
    }

    private fun buildBestSplitLines(
        shoppingList: ShoppingList,
        quotes: List<PriceQuote>
    ): List<BestSplitLine> {
        return shoppingList.items.mapNotNull { item ->
            val quote = quotes
                .asSequence()
                .filter { it.materialName == item.name && it.inStock }
                .minByOrNull { it.unitPrice }
                ?: return@mapNotNull null
            BestSplitLine(
                itemName = item.name,
                store = quote.store,
                unitPrice = quote.unitPrice
            )
        }
    }
}
