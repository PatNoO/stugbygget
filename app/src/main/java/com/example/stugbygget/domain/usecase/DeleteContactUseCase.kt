package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.repository.ContactRepository

class DeleteContactUseCase(private val repository: ContactRepository) {
    suspend operator fun invoke(projectId: String, contactId: String) =
        repository.deleteContact(projectId, contactId)
}
