package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.Contact
import com.example.stugbygget.domain.repository.ContactRepository
import kotlinx.coroutines.flow.Flow

class ObserveContactsUseCase(private val repository: ContactRepository) {
    operator fun invoke(projectId: String): Flow<List<Contact>> =
        repository.observeContacts(projectId)
}
