package com.example.stugbygget.feature.contacts.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.stugbygget.di.AppContainer

class ContactsViewModelFactory(
    private val container: AppContainer
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactsViewModel::class.java)) {
            return ContactsViewModel(
                observeContactsUseCase = container.observeContactsUseCase,
                upsertContactUseCase = container.upsertContactUseCase,
                deleteContactUseCase = container.deleteContactUseCase,
                projectId = container.projectSessionRepository.getProjectId()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
