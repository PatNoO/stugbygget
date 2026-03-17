package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.Contact
import com.example.stugbygget.domain.repository.ContactRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class UpsertContactUseCaseTest {

    @Test
    fun `delegates upsert to repository`() = runBlocking {
        val repo = FakeContactRepository()
        val useCase = UpsertContactUseCase(repo)
        val contact = mockContact("c1")

        useCase("project-1", contact)

        assertEquals(contact, repo.lastUpserted)
    }

    @Test
    fun `passes correct projectId`() = runBlocking {
        val repo = FakeContactRepository()
        val useCase = UpsertContactUseCase(repo)

        useCase("my-project", mockContact())

        assertEquals("my-project", repo.lastProjectId)
    }

    @Test
    fun `can upsert multiple contacts independently`() = runBlocking {
        val repo = FakeContactRepository()
        val useCase = UpsertContactUseCase(repo)
        val contactA = mockContact("a")
        val contactB = mockContact("b")

        useCase("project-1", contactA)
        useCase("project-1", contactB)

        assertNotNull(repo.lastUpserted)
        assertEquals(contactB, repo.lastUpserted)
    }

    // ── Fake ──────────────────────────────────────────────────────────────────

    private class FakeContactRepository : ContactRepository {
        var lastProjectId: String? = null
        var lastUpserted: Contact? = null

        override fun observeContacts(projectId: String): Flow<List<Contact>> = flowOf(emptyList())

        override suspend fun upsertContact(projectId: String, contact: Contact) {
            lastProjectId = projectId
            lastUpserted = contact
        }

        override suspend fun deleteContact(projectId: String, contactId: String) = Unit
    }
}
