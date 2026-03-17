package com.example.stugbygget.feature.contacts.ui

import com.example.stugbygget.domain.model.Contact
import com.example.stugbygget.domain.model.ContactRole

data class ContactsUiState(
    val isLoading: Boolean = true,
    val contacts: List<Contact> = emptyList(),
    val errorMessage: String? = null,
    // Add/edit sheet
    val showSheet: Boolean = false,
    val editingContact: Contact? = null,
    val draftName: String = "",
    val draftPhone: String = "",
    val draftEmail: String = "",
    val draftRole: ContactRole = ContactRole.CONTRACTOR,
    val isSaving: Boolean = false,
    val sheetError: String? = null,
    // Delete confirmation
    val pendingDeleteId: String? = null,
    val isDeleting: Boolean = false
)
