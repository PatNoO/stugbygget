package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.Contact
import com.example.stugbygget.domain.repository.ContactRepository

class UpsertContactUseCase(private val repository: ContactRepository) {
    suspend operator fun invoke(projectId: String, contact: Contact) =
        repository.upsertContact(projectId, contact)
}
