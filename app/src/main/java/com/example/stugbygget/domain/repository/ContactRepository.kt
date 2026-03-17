package com.example.stugbygget.domain.repository

import com.example.stugbygget.domain.model.Contact
import kotlinx.coroutines.flow.Flow

interface ContactRepository {
    fun observeContacts(projectId: String): Flow<List<Contact>>
    suspend fun upsertContact(projectId: String, contact: Contact)
    suspend fun deleteContact(projectId: String, contactId: String)
}
