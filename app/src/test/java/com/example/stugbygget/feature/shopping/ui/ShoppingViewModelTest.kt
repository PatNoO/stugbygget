package com.example.stugbygget.feature.shopping.ui

import com.example.stugbygget.domain.model.ShoppingItem
import com.example.stugbygget.domain.model.ShoppingList
import com.example.stugbygget.domain.repository.ShoppingRepository
import com.example.stugbygget.domain.usecase.AddShoppingItemUseCase
import com.example.stugbygget.domain.usecase.CreateShoppingListUseCase
import com.example.stugbygget.domain.usecase.ObserveShoppingListsUseCase
import com.example.stugbygget.domain.usecase.ToggleShoppingItemPurchasedUseCase
import com.example.stugbygget.domain.usecase.mockShoppingList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
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
class ShoppingViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── Loading ───────────────────────────────────────────────────────────────

    @Test
    fun `lists loaded into state`() = runTest {
        val lists = listOf(mockShoppingList("l1"), mockShoppingList("l2"))
        val vm = buildViewModel(lists = lists)

        assertEquals(lists, vm.uiState.value.shoppingLists)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `error from repository sets errorMessage`() = runTest {
        val vm = buildViewModel(throwOnObserve = true)

        assertNotNull(vm.uiState.value.errorMessage)
        assertFalse(vm.uiState.value.isLoading)
    }

    // ── List name / phase input ───────────────────────────────────────────────

    @Test
    fun `onListNameChanged updates input and clears error`() = runTest {
        val vm = buildViewModel()
        vm.onCreateList() // triggers blank error

        vm.onListNameChanged("Ny lista")

        assertEquals("Ny lista", vm.uiState.value.listNameInput)
        assertNull(vm.uiState.value.errorMessage)
    }

    @Test
    fun `onPhaseChanged updates phaseInput`() = runTest {
        val vm = buildViewModel()

        vm.onPhaseChanged("phase-3")

        assertEquals("phase-3", vm.uiState.value.phaseInput)
    }

    // ── Create list ───────────────────────────────────────────────────────────

    @Test
    fun `onCreateList with blank name sets errorMessage`() = runTest {
        val vm = buildViewModel()

        vm.onCreateList()

        assertNotNull(vm.uiState.value.errorMessage)
    }

    @Test
    fun `onCreateList with null userId sets errorMessage`() = runTest {
        val vm = buildViewModel(userId = null)
        vm.onListNameChanged("Ny lista")

        vm.onCreateList()

        assertNotNull(vm.uiState.value.errorMessage)
    }

    @Test
    fun `valid onCreateList clears input and error on success`() = runTest {
        val repo = FakeShoppingRepository()
        val vm = buildViewModel(repo = repo)
        vm.onListNameChanged("Materialinköp")

        vm.onCreateList()

        assertEquals("Materialinköp", repo.lastCreatedName)
        assertEquals("", vm.uiState.value.listNameInput)
        assertNull(vm.uiState.value.errorMessage)
        assertFalse(vm.uiState.value.isSubmitting)
    }

    @Test
    fun `onCreateList failure sets errorMessage`() = runTest {
        val vm = buildViewModel(throwOnCreate = true)
        vm.onListNameChanged("Ny lista")

        vm.onCreateList()

        assertNotNull(vm.uiState.value.errorMessage)
        assertFalse(vm.uiState.value.isSubmitting)
    }

    // ── Item drafts ───────────────────────────────────────────────────────────

    @Test
    fun `onItemNameChanged updates draft for list`() = runTest {
        val vm = buildViewModel(lists = listOf(mockShoppingList("l1")))

        vm.onItemNameChanged("l1", "Bräda")

        assertEquals("Bräda", vm.uiState.value.itemDrafts["l1"]?.name)
    }

    @Test
    fun `onItemQuantityChanged updates draft quantity`() = runTest {
        val vm = buildViewModel(lists = listOf(mockShoppingList("l1")))

        vm.onItemQuantityChanged("l1", "5")

        assertEquals("5", vm.uiState.value.itemDrafts["l1"]?.quantity)
    }

    @Test
    fun `onItemUnitChanged updates draft unit`() = runTest {
        val vm = buildViewModel(lists = listOf(mockShoppingList("l1")))

        vm.onItemUnitChanged("l1", "m")

        assertEquals("m", vm.uiState.value.itemDrafts["l1"]?.unit)
    }

    // ── Add item ──────────────────────────────────────────────────────────────

    @Test
    fun `onAddItem with blank name sets errorMessage`() = runTest {
        val vm = buildViewModel()

        vm.onAddItem("l1")

        assertNotNull(vm.uiState.value.errorMessage)
    }

    @Test
    fun `onAddItem with zero quantity sets errorMessage`() = runTest {
        val vm = buildViewModel()
        vm.onItemNameChanged("l1", "Bräda")
        vm.onItemQuantityChanged("l1", "0")

        vm.onAddItem("l1")

        assertNotNull(vm.uiState.value.errorMessage)
    }

    @Test
    fun `onAddItem with non-numeric quantity sets errorMessage`() = runTest {
        val vm = buildViewModel()
        vm.onItemNameChanged("l1", "Bräda")
        vm.onItemQuantityChanged("l1", "abc")

        vm.onAddItem("l1")

        assertNotNull(vm.uiState.value.errorMessage)
    }

    @Test
    fun `valid onAddItem calls repository and clears draft`() = runTest {
        val repo = FakeShoppingRepository()
        val vm = buildViewModel(repo = repo, lists = listOf(mockShoppingList("l1")))
        vm.onItemNameChanged("l1", "Skruvar")
        vm.onItemQuantityChanged("l1", "2")
        vm.onItemUnitChanged("l1", "box")

        vm.onAddItem("l1")

        assertNotNull(repo.lastAddedItem)
        assertEquals("Skruvar", repo.lastAddedItem?.name)
        assertEquals("", vm.uiState.value.itemDrafts["l1"]?.name)
        assertFalse(vm.uiState.value.isSubmitting)
    }

    // ── Toggle purchased ──────────────────────────────────────────────────────

    @Test
    fun `onTogglePurchased delegates to repository`() = runTest {
        val repo = FakeShoppingRepository()
        val vm = buildViewModel(repo = repo)

        vm.onTogglePurchased("l1", "item-1", true)

        assertEquals("item-1", repo.lastToggledItemId)
        assertEquals(true, repo.lastToggledPurchased)
    }

    @Test
    fun `toggle failure sets errorMessage`() = runTest {
        val vm = buildViewModel(throwOnToggle = true)

        vm.onTogglePurchased("l1", "item-1", true)

        assertNotNull(vm.uiState.value.errorMessage)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun buildViewModel(
        lists: List<ShoppingList> = emptyList(),
        userId: String? = "user-1",
        throwOnObserve: Boolean = false,
        throwOnCreate: Boolean = false,
        throwOnToggle: Boolean = false,
        repo: FakeShoppingRepository = FakeShoppingRepository(
            lists = lists,
            throwOnObserve = throwOnObserve,
            throwOnCreate = throwOnCreate,
            throwOnToggle = throwOnToggle
        )
    ): ShoppingViewModel = ShoppingViewModel(
        observeShoppingListsUseCase = ObserveShoppingListsUseCase(repo),
        createShoppingListUseCase = CreateShoppingListUseCase(repo),
        addShoppingItemUseCase = AddShoppingItemUseCase(repo),
        toggleShoppingItemPurchasedUseCase = ToggleShoppingItemPurchasedUseCase(repo),
        projectId = "project-1",
        currentUserIdProvider = { userId }
    )

    private class FakeShoppingRepository(
        private val lists: List<ShoppingList> = emptyList(),
        private val throwOnObserve: Boolean = false,
        private val throwOnCreate: Boolean = false,
        private val throwOnToggle: Boolean = false
    ) : ShoppingRepository {
        var lastCreatedName: String? = null
        var lastAddedItem: ShoppingItem? = null
        var lastToggledItemId: String? = null
        var lastToggledPurchased: Boolean? = null

        override fun observeShoppingLists(projectId: String): Flow<List<ShoppingList>> {
            if (throwOnObserve) throw RuntimeException("Firestore unavailable")
            return flowOf(lists)
        }

        override suspend fun createShoppingList(
            projectId: String, name: String, phaseId: String, createdBy: String
        ) {
            if (throwOnCreate) throw RuntimeException("Create failed")
            lastCreatedName = name
        }

        override suspend fun addItem(projectId: String, listId: String, item: ShoppingItem) {
            lastAddedItem = item
        }

        override suspend fun updateItemPurchased(
            projectId: String, listId: String, itemId: String, purchased: Boolean
        ) {
            if (throwOnToggle) throw RuntimeException("Toggle failed")
            lastToggledItemId = itemId
            lastToggledPurchased = purchased
        }
    }
}
