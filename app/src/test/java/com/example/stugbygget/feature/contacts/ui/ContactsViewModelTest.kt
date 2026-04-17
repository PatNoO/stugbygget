package com.example.stugbygget.feature.contacts.ui

import com.example.stugbygget.domain.model.Contact
import com.example.stugbygget.domain.model.ContactRole
import com.example.stugbygget.domain.repository.ContactRepository
import com.example.stugbygget.domain.usecase.DeleteContactUseCase
import com.example.stugbygget.domain.usecase.ObserveContactsUseCase
import com.example.stugbygget.domain.usecase.UpsertContactUseCase
import com.example.stugbygget.domain.usecase.mockContact
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
class ContactsViewModelTest {

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
    fun `contacts loaded into state`() = runTest {
        val contacts = listOf(mockContact("c1"), mockContact("c2"))
        val vm = buildViewModel(contacts = contacts)

        assertEquals(contacts, vm.uiState.value.contacts)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `error from repository sets errorMessage`() = runTest {
        val vm = buildViewModel(throwOnObserve = true)

        assertNotNull(vm.uiState.value.errorMessage)
        assertFalse(vm.uiState.value.isLoading)
    }

    // ── Add sheet ─────────────────────────────────────────────────────────────

    @Test
    fun `onShowAddSheet opens sheet with blank draft`() = runTest {
        val vm = buildViewModel()
        vm.onDraftNameChanged("Old name")

        vm.onShowAddSheet()

        assertTrue(vm.uiState.value.showSheet)
        assertEquals("", vm.uiState.value.draftName)
        assertNull(vm.uiState.value.editingContact)
        assertNull(vm.uiState.value.sheetError)
    }

    @Test
    fun `onDismissSheet closes sheet and clears error`() = runTest {
        val vm = buildViewModel()
        vm.onShowAddSheet()

        vm.onDismissSheet()

        assertFalse(vm.uiState.value.showSheet)
        assertNull(vm.uiState.value.sheetError)
    }

    // ── Edit sheet ────────────────────────────────────────────────────────────

    @Test
    fun `onShowEditSheet populates draft fields with contact data`() = runTest {
        val contact = mockContact("c1", name = "Bob", phone = "070", email = "bob@x.com", role = ContactRole.SUPPLIER)
        val vm = buildViewModel(contacts = listOf(contact))

        vm.onShowEditSheet(contact)

        assertTrue(vm.uiState.value.showSheet)
        assertEquals(contact, vm.uiState.value.editingContact)
        assertEquals("Bob", vm.uiState.value.draftName)
        assertEquals("070", vm.uiState.value.draftPhone)
        assertEquals("bob@x.com", vm.uiState.value.draftEmail)
        assertEquals(ContactRole.SUPPLIER, vm.uiState.value.draftRole)
    }

    // ── Draft fields ──────────────────────────────────────────────────────────

    @Test
    fun `draft field updates reflected in state`() = runTest {
        val vm = buildViewModel()

        vm.onDraftNameChanged("Eva")
        vm.onDraftPhoneChanged("073-999")
        vm.onDraftEmailChanged("eva@test.com")
        vm.onDraftRoleChanged(ContactRole.TEAM_MEMBER)

        assertEquals("Eva", vm.uiState.value.draftName)
        assertEquals("073-999", vm.uiState.value.draftPhone)
        assertEquals("eva@test.com", vm.uiState.value.draftEmail)
        assertEquals(ContactRole.TEAM_MEMBER, vm.uiState.value.draftRole)
    }

    @Test
    fun `onDraftNameChanged clears sheetError`() = runTest {
        val vm = buildViewModel()
        vm.onSubmitContact() // triggers blank error

        vm.onDraftNameChanged("Eva")

        assertNull(vm.uiState.value.sheetError)
    }

    // ── Validation ────────────────────────────────────────────────────────────

    @Test
    fun `submit with blank name sets sheetError`() = runTest {
        val vm = buildViewModel()

        vm.onSubmitContact()

        assertNotNull(vm.uiState.value.sheetError)
        assertEquals("Name is required.", vm.uiState.value.sheetError)
    }

    // ── Successful submit ─────────────────────────────────────────────────────

    @Test
    fun `valid submit calls upsert and closes sheet`() = runTest {
        val repo = FakeContactRepository()
        val vm = buildViewModel(repo = repo)
        vm.onDraftNameChanged("Eva")
        vm.onDraftPhoneChanged("073")
        vm.onDraftEmailChanged("eva@test.com")

        vm.onSubmitContact()

        assertNotNull(repo.lastUpserted)
        assertEquals("Eva", repo.lastUpserted?.name)
        assertFalse(vm.uiState.value.showSheet)
        assertNull(vm.uiState.value.sheetError)
        assertFalse(vm.uiState.value.isSaving)
    }

    @Test
    fun `edit submit reuses existing contact id`() = runTest {
        val repo = FakeContactRepository()
        val existingContact = mockContact("existing-id", name = "Old Name")
        val vm = buildViewModel(repo = repo, contacts = listOf(existingContact))
        vm.onShowEditSheet(existingContact)
        vm.onDraftNameChanged("New Name")

        vm.onSubmitContact()

        assertEquals("existing-id", repo.lastUpserted?.id)
        assertEquals("New Name", repo.lastUpserted?.name)
    }

    @Test
    fun `submit trims name whitespace`() = runTest {
        val repo = FakeContactRepository()
        val vm = buildViewModel(repo = repo)
        vm.onDraftNameChanged("  Eva  ")

        vm.onSubmitContact()

        assertEquals("Eva", repo.lastUpserted?.name)
    }

    @Test
    fun `submit failure sets sheetError`() = runTest {
        val vm = buildViewModel(throwOnUpsert = true)
        vm.onDraftNameChanged("Eva")

        vm.onSubmitContact()

        assertNotNull(vm.uiState.value.sheetError)
        assertFalse(vm.uiState.value.isSaving)
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @Test
    fun `onRequestDelete sets pendingDeleteId`() = runTest {
        val vm = buildViewModel()

        vm.onRequestDelete("contact-99")

        assertEquals("contact-99", vm.uiState.value.pendingDeleteId)
    }

    @Test
    fun `onCancelDelete clears pendingDeleteId`() = runTest {
        val vm = buildViewModel()
        vm.onRequestDelete("contact-99")

        vm.onCancelDelete()

        assertNull(vm.uiState.value.pendingDeleteId)
    }

    @Test
    fun `onConfirmDelete calls repository and clears isDeleting`() = runTest {
        val repo = FakeContactRepository()
        val vm = buildViewModel(repo = repo)
        vm.onRequestDelete("contact-42")

        vm.onConfirmDelete()

        assertEquals("contact-42", repo.lastDeletedId)
        assertFalse(vm.uiState.value.isDeleting)
        assertNull(vm.uiState.value.pendingDeleteId)
    }

    @Test
    fun `onConfirmDelete does nothing when no pending delete`() = runTest {
        val repo = FakeContactRepository()
        val vm = buildViewModel(repo = repo)

        vm.onConfirmDelete()

        assertNull(repo.lastDeletedId)
    }

    @Test
    fun `delete failure sets errorMessage`() = runTest {
        val vm = buildViewModel(throwOnDelete = true)
        vm.onRequestDelete("contact-1")

        vm.onConfirmDelete()

        assertNotNull(vm.uiState.value.errorMessage)
        assertFalse(vm.uiState.value.isDeleting)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun buildViewModel(
        contacts: List<Contact> = emptyList(),
        throwOnObserve: Boolean = false,
        throwOnUpsert: Boolean = false,
        throwOnDelete: Boolean = false,
        repo: FakeContactRepository = FakeContactRepository(
            contacts = contacts,
            throwOnObserve = throwOnObserve,
            throwOnUpsert = throwOnUpsert,
            throwOnDelete = throwOnDelete
        )
    ): ContactsViewModel = ContactsViewModel(
        observeContactsUseCase = ObserveContactsUseCase(repo),
        upsertContactUseCase = UpsertContactUseCase(repo),
        deleteContactUseCase = DeleteContactUseCase(repo),
        projectId = "project-1"
    )

    private class FakeContactRepository(
        private val contacts: List<Contact> = emptyList(),
        private val throwOnObserve: Boolean = false,
        private val throwOnUpsert: Boolean = false,
        private val throwOnDelete: Boolean = false
    ) : ContactRepository {
        var lastUpserted: Contact? = null
        var lastDeletedId: String? = null

        override fun observeContacts(projectId: String): Flow<List<Contact>> = flow {
            if (throwOnObserve) throw RuntimeException("Firestore unavailable")
            emit(contacts)
        }

        override suspend fun upsertContact(projectId: String, contact: Contact) {
            if (throwOnUpsert) throw RuntimeException("Write failed")
            lastUpserted = contact
        }

        override suspend fun deleteContact(projectId: String, contactId: String) {
            if (throwOnDelete) throw RuntimeException("Delete failed")
            lastDeletedId = contactId
        }
    }
}
