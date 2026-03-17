package com.example.stugbygget.feature.contacts.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stugbygget.domain.model.Contact
import com.example.stugbygget.domain.model.ContactRole
import com.example.stugbygget.domain.usecase.DeleteContactUseCase
import com.example.stugbygget.domain.usecase.ObserveContactsUseCase
import com.example.stugbygget.domain.usecase.UpsertContactUseCase
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ContactsViewModel(
    private val observeContactsUseCase: ObserveContactsUseCase,
    private val upsertContactUseCase: UpsertContactUseCase,
    private val deleteContactUseCase: DeleteContactUseCase,
    private val projectId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContactsUiState())
    val uiState: StateFlow<ContactsUiState> = _uiState.asStateFlow()

    init {
        observeContacts()
    }

    fun onShowAddSheet() {
        _uiState.update {
            it.copy(
                showSheet = true,
                editingContact = null,
                draftName = "",
                draftPhone = "",
                draftEmail = "",
                draftRole = ContactRole.CONTRACTOR,
                sheetError = null
            )
        }
    }

    fun onShowEditSheet(contact: Contact) {
        _uiState.update {
            it.copy(
                showSheet = true,
                editingContact = contact,
                draftName = contact.name,
                draftPhone = contact.phone,
                draftEmail = contact.email,
                draftRole = contact.role,
                sheetError = null
            )
        }
    }

    fun onDismissSheet() {
        _uiState.update { it.copy(showSheet = false, sheetError = null) }
    }

    fun onDraftNameChanged(value: String) = _uiState.update { it.copy(draftName = value, sheetError = null) }
    fun onDraftPhoneChanged(value: String) = _uiState.update { it.copy(draftPhone = value) }
    fun onDraftEmailChanged(value: String) = _uiState.update { it.copy(draftEmail = value) }
    fun onDraftRoleChanged(role: ContactRole) = _uiState.update { it.copy(draftRole = role) }

    fun onSubmitContact() {
        val state = _uiState.value
        if (state.draftName.isBlank()) {
            _uiState.update { it.copy(sheetError = "Name is required.") }
            return
        }
        val contact = Contact(
            id = state.editingContact?.id ?: UUID.randomUUID().toString(),
            name = state.draftName.trim(),
            phone = state.draftPhone.trim(),
            email = state.draftEmail.trim(),
            role = state.draftRole
        )
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, sheetError = null) }
            runCatching { upsertContactUseCase(projectId, contact) }
                .onSuccess { _uiState.update { it.copy(isSaving = false, showSheet = false) } }
                .onFailure { e ->
                    _uiState.update { it.copy(isSaving = false, sheetError = e.message ?: "Failed to save contact.") }
                }
        }
    }

    fun onRequestDelete(contactId: String) {
        _uiState.update { it.copy(pendingDeleteId = contactId) }
    }

    fun onCancelDelete() {
        _uiState.update { it.copy(pendingDeleteId = null) }
    }

    fun onConfirmDelete() {
        val contactId = _uiState.value.pendingDeleteId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, pendingDeleteId = null) }
            runCatching { deleteContactUseCase(projectId, contactId) }
                .onFailure { e ->
                    _uiState.update { it.copy(isDeleting = false, errorMessage = e.message ?: "Failed to delete contact.") }
                }
                .onSuccess { _uiState.update { it.copy(isDeleting = false) } }
        }
    }

    private fun observeContacts() {
        viewModelScope.launch {
            observeContactsUseCase(projectId)
                .onStart { _uiState.update { it.copy(isLoading = true) } }
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Failed to load contacts.") }
                }
                .collect { contacts ->
                    _uiState.update { it.copy(isLoading = false, contacts = contacts, errorMessage = null) }
                }
        }
    }
}
