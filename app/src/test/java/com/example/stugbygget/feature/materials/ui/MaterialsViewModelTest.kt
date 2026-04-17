package com.example.stugbygget.feature.materials.ui

import com.example.stugbygget.domain.model.MaterialCategory
import com.example.stugbygget.domain.model.MaterialSpec
import com.example.stugbygget.domain.model.PriceQuote
import android.content.ContentResolver
import com.example.stugbygget.domain.model.OwnedMaterial
import com.example.stugbygget.domain.repository.MaterialRepository
import com.example.stugbygget.domain.repository.OwnedMaterialRepository
import com.example.stugbygget.domain.usecase.CalculateMaterialQuantityUseCase
import com.example.stugbygget.domain.usecase.DeleteOwnedMaterialUseCase
import com.example.stugbygget.domain.usecase.ObserveMaterialsUseCase
import com.example.stugbygget.domain.usecase.ObserveOwnedMaterialsUseCase
import com.example.stugbygget.domain.usecase.ObservePriceQuotesUseCase
import com.example.stugbygget.domain.usecase.SeedMaterialsUseCase
import com.example.stugbygget.domain.usecase.UpsertOwnedMaterialUseCase
import com.example.stugbygget.domain.usecase.mockMaterialSpec
import com.example.stugbygget.domain.usecase.mockPriceQuote
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MaterialsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── MaterialsViewModel ────────────────────────────────────────────────────

    @Test
    fun `materials loaded into state`() = runTest {
        val materials = listOf(mockMaterialSpec("m1"), mockMaterialSpec("m2"))
        val vm = buildMaterialsViewModel(materials = materials)

        assertEquals(materials, vm.uiState.value.materials)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `error from repository sets errorMessage`() = runTest {
        val vm = buildMaterialsViewModel(throwError = true)

        assertNotNull(vm.uiState.value.errorMessage)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `onSearchQueryChanged updates searchQuery`() = runTest {
        val vm = buildMaterialsViewModel()

        vm.onSearchQueryChanged("Färg")

        assertEquals("Färg", vm.uiState.value.searchQuery)
    }

    @Test
    fun `filteredMaterials returns all when query is blank`() = runTest {
        val materials = listOf(mockMaterialSpec("m1", name = "Färg"), mockMaterialSpec("m2", name = "Trä"))
        val vm = buildMaterialsViewModel(materials = materials)

        assertEquals(2, vm.uiState.value.filteredMaterials.size)
    }

    @Test
    fun `filteredMaterials filters by name case-insensitively`() = runTest {
        val materials = listOf(
            mockMaterialSpec("m1", name = "Vit Färg"),
            mockMaterialSpec("m2", name = "Trägolv"),
        )
        val vm = buildMaterialsViewModel(materials = materials)
        vm.onSearchQueryChanged("färg")

        assertEquals(1, vm.uiState.value.filteredMaterials.size)
        assertEquals("Vit Färg", vm.uiState.value.filteredMaterials.first().name)
    }

    @Test
    fun `filteredMaterials filters by category name`() = runTest {
        val materials = listOf(
            mockMaterialSpec("m1", name = "Kakel", category = MaterialCategory.TILE),
            mockMaterialSpec("m2", name = "Trägolv", category = MaterialCategory.WOOD),
        )
        val vm = buildMaterialsViewModel(materials = materials)
        vm.onSearchQueryChanged("TILE")

        assertEquals(1, vm.uiState.value.filteredMaterials.size)
        assertEquals("Kakel", vm.uiState.value.filteredMaterials.first().name)
    }

    @Test
    fun `filteredMaterials returns empty when no match`() = runTest {
        val materials = listOf(mockMaterialSpec("m1", name = "Färg"))
        val vm = buildMaterialsViewModel(materials = materials)
        vm.onSearchQueryChanged("xyz")

        assertTrue(vm.uiState.value.filteredMaterials.isEmpty())
    }

    // ── MaterialDetailViewModel ───────────────────────────────────────────────

    @Test
    fun `detail loads material by id`() = runTest {
        val target = mockMaterialSpec("mat-A", name = "Kakel")
        val other = mockMaterialSpec("mat-B", name = "Färg")
        val vm = buildDetailViewModel(materials = listOf(target, other), materialId = "mat-A")

        assertEquals(target, vm.uiState.value.material)
    }

    @Test
    fun `detail material is null when id not found`() = runTest {
        val vm = buildDetailViewModel(
            materials = listOf(mockMaterialSpec("mat-X")),
            materialId = "mat-MISSING"
        )

        assertNull(vm.uiState.value.material)
    }

    @Test
    fun `detail loads price quotes`() = runTest {
        val quotes = listOf(mockPriceQuote("Bauhaus"), mockPriceQuote("Hornbach"))
        val vm = buildDetailViewModel(quotes = quotes)

        assertEquals(quotes, vm.uiState.value.priceQuotes)
    }

    @Test
    fun `detail calculates units when area and material are available`() = runTest {
        // coveragePerUnit=10, wasteMargin=0.1, area=20 → ceil(20/10 * 1.1) = ceil(2.2) = 3
        val material = mockMaterialSpec("mat-1", coveragePerUnit = 10.0, wasteMargin = 0.1)
        val vm = buildDetailViewModel(materials = listOf(material), materialId = "mat-1")

        vm.onAreaInputChanged("20")

        assertEquals(3, vm.uiState.value.calculatedUnits)
    }

    @Test
    fun `detail does not calculate when area is not a number`() = runTest {
        val material = mockMaterialSpec("mat-1")
        val vm = buildDetailViewModel(materials = listOf(material), materialId = "mat-1")

        vm.onAreaInputChanged("abc")

        assertNull(vm.uiState.value.calculatedUnits)
    }

    @Test
    fun `detail error from materials repository sets errorMessage`() = runTest {
        val vm = buildDetailViewModel(throwOnMaterials = true)

        assertNotNull(vm.uiState.value.errorMessage)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun buildMaterialsViewModel(
        materials: List<MaterialSpec> = emptyList(),
        throwError: Boolean = false
    ): MaterialsViewModel {
        val repo = FakeMaterialRepository(
            materials = materials,
            throwOnMaterials = throwError
        )
        val ownedRepo = FakeOwnedMaterialRepository()
        @Suppress("UNCHECKED_CAST")
        return MaterialsViewModel(
            observeMaterialsUseCase = ObserveMaterialsUseCase(repo),
            seedMaterialsUseCase = SeedMaterialsUseCase(repo),
            observeOwnedMaterialsUseCase = ObserveOwnedMaterialsUseCase(ownedRepo),
            upsertOwnedMaterialUseCase = UpsertOwnedMaterialUseCase(ownedRepo),
            deleteOwnedMaterialUseCase = DeleteOwnedMaterialUseCase(ownedRepo),
            projectId = "project-1",
            contentResolver = null, // never exercised in these tests
            storage = null          // never exercised in these tests
        )
    }

    private fun buildDetailViewModel(
        materials: List<MaterialSpec> = listOf(mockMaterialSpec("mat-1")),
        quotes: List<PriceQuote> = emptyList(),
        materialId: String = "mat-1",
        throwOnMaterials: Boolean = false
    ): MaterialDetailViewModel {
        val repo = FakeMaterialRepository(
            materials = materials,
            quotes = quotes,
            throwOnMaterials = throwOnMaterials
        )
        return MaterialDetailViewModel(
            observeMaterialsUseCase = ObserveMaterialsUseCase(repo),
            observePriceQuotesUseCase = ObservePriceQuotesUseCase(repo),
            calculateMaterialQuantityUseCase = CalculateMaterialQuantityUseCase(),
            projectId = "project-1",
            materialId = materialId
        )
    }

    private class FakeMaterialRepository(
        private val materials: List<MaterialSpec> = emptyList(),
        private val quotes: List<PriceQuote> = emptyList(),
        private val throwOnMaterials: Boolean = false
    ) : MaterialRepository {
        override fun observeMaterials(projectId: String): Flow<List<MaterialSpec>> = flow {
            if (throwOnMaterials) throw RuntimeException("Firestore unavailable")
            emit(materials)
        }

        override fun observePriceQuotes(
            projectId: String,
            materialId: String
        ): Flow<List<PriceQuote>> = flowOf(quotes)

        override suspend fun seedDefaultMaterials(projectId: String) { /* no-op */ }
    }

    private class FakeOwnedMaterialRepository : OwnedMaterialRepository {
        override fun observeOwnedMaterials(projectId: String): Flow<List<OwnedMaterial>> = flowOf(emptyList())
        override suspend fun upsertOwnedMaterial(projectId: String, material: OwnedMaterial) { /* no-op */ }
        override suspend fun deleteOwnedMaterial(projectId: String, id: String) { /* no-op */ }
    }
}
