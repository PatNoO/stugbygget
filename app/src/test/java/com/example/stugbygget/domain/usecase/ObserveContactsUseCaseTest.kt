package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.Contact
import com.example.stugbygget.domain.model.ContactRole
import com.example.stugbygget.domain.repository.ContactRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveContactsUseCaseTest {

    @Test
    fun `returns contacts from repository`() = runBlocking {
        val expected = listOf(mockContact("c1"))
        val repo = FakeContactRepository(contacts = expected)
        val useCase = ObserveContactsUseCase(repo)

        val actual = useCase("project-1").first()

        assertEquals(expected, actual)
    }

    @Test
    fun `passes projectId to repository`() = runBlocking {
        val repo = FakeContactRepository()
        val useCase = ObserveContactsUseCase(repo)

        useCase("my-project").first()

        assertEquals("my-project", repo.lastProjectId)
    }

    @Test
    fun `returns empty list when no contacts exist`() = runBlocking {
        val repo = FakeContactRepository(contacts = emptyList())
        val useCase = ObserveContactsUseCase(repo)

        val actual = useCase("project-1").first()

        assertEquals(emptyList<Contact>(), actual)
    }

    // ── Fake ──────────────────────────────────────────────────────────────────

    private class FakeContactRepository(
        private val contacts: List<Contact> = emptyList()
    ) : ContactRepository {
        var lastProjectId: String? = null

        override fun observeContacts(projectId: String): Flow<List<Contact>> {
            lastProjectId = projectId
            return flowOf(contacts)
        }

        override suspend fun upsertContact(projectId: String, contact: Contact) = Unit
        override suspend fun deleteContact(projectId: String, contactId: String) = Unit
    }
}

internal fun mockContact(
    id: String = "contact-1",
    name: String = "Anna Svensson",
    phone: String = "070-123 45 67",
    email: String = "anna@example.com",
    role: ContactRole = ContactRole.CONTRACTOR
): Contact = Contact(id = id, name = name, phone = phone, email = email, role = role)
