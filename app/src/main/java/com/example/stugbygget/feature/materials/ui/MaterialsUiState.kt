package com.example.stugbygget.feature.materials.ui

import android.net.Uri
import com.example.stugbygget.domain.model.MaterialSpec
import com.example.stugbygget.domain.model.OwnedMaterial
import com.example.stugbygget.domain.model.PriceQuote

data class MaterialsUiState(
    val isLoading: Boolean = false,
    val materials: List<MaterialSpec> = emptyList(),
    val searchQuery: String = "",
    val errorMessage: String? = null,
    // Owned materials
    val ownedMaterials: List<OwnedMaterial> = emptyList(),
    val showOwnedAddSheet: Boolean = false,
    val draftOwnedName: String = "",
    val draftOwnedQuantity: String = "",
    val draftOwnedUnit: String = "",
    val draftOwnedNotes: String = "",
    val draftOwnedPhotoUri: Uri? = null,
    val isAddingOwned: Boolean = false,
    val ownedAddError: String? = null,
    val viewingPhotoUrl: String? = null,
) {
    val filteredMaterials: List<MaterialSpec>
        get() = if (searchQuery.isBlank()) materials
        else materials.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                it.category.name.contains(searchQuery, ignoreCase = true)
        }
}

data class MaterialDetailUiState(
    val isLoading: Boolean = false,
    val material: MaterialSpec? = null,
    val priceQuotes: List<PriceQuote> = emptyList(),
    val areaInput: String = "",
    val calculatedUnits: Int? = null,
    val errorMessage: String? = null,
)
