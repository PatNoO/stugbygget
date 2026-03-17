package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.Contact
import com.example.stugbygget.domain.repository.ContactRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class DeleteContactUseCaseTest {

    @Test
    fun `delegates delete to repository`() = runBlocking {
        val repo = FakeContactRepository()
        val useCase = DeleteContactUseCase(repo)

        useCase("project-1", "contact-abc")

        assertEquals("contact-abc", repo.lastDeletedId)
    }

    @Test
    fun `passes correct projectId`() = runBlocking {
        val repo = FakeContactRepository()
        val useCase = DeleteContactUseCase(repo)

        useCase("my-project", "contact-1")

        assertEquals("my-project", repo.lastProjectId)
    }

    // ── Fake ──────────────────────────────────────────────────────────────────

    private class FakeContactRepository : ContactRepository {
        var lastProjectId: String? = null
        var lastDeletedId: String? = null

        override fun observeContacts(projectId: String): Flow<List<Contact>> = flowOf(emptyList())
        override suspend fun upsertContact(projectId: String, contact: Contact) = Unit

        override suspend fun deleteContact(projectId: String, contactId: String) {
            lastProjectId = projectId
            lastDeletedId = contactId
        }
    }
}
