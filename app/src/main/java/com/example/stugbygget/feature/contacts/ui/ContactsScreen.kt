package com.example.stugbygget.feature.contacts.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import com.example.stugbygget.ui.components.SommarTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stugbygget.R
import com.example.stugbygget.di.AppContainer
import com.example.stugbygget.domain.model.Contact
import com.example.stugbygget.domain.model.ContactRole
import com.example.stugbygget.ui.components.SommarBadge
import com.example.stugbygget.ui.components.SommarButton
import com.example.stugbygget.ui.components.SommarFilterChip
import com.example.stugbygget.ui.components.SommarInfoBox
import com.example.stugbygget.ui.components.SommarOutlineButton
import com.example.stugbygget.ui.theme.Border
import com.example.stugbygget.ui.theme.CreamBackground
import com.example.stugbygget.ui.theme.FaluRed
import com.example.stugbygget.ui.theme.Fraunces
import com.example.stugbygget.ui.theme.LakeBlue
import com.example.stugbygget.ui.theme.MeadowGreen
import com.example.stugbygget.ui.theme.MidsummerGold
import com.example.stugbygget.ui.theme.MonoStyles
import com.example.stugbygget.ui.theme.SommarShapes
import com.example.stugbygget.ui.theme.cardShadow
import com.example.stugbygget.ui.theme.staggeredFadeIn

private fun roleColor(role: ContactRole) = when (role) {
    ContactRole.CONTRACTOR -> FaluRed
    ContactRole.SUPPLIER -> LakeBlue
    ContactRole.TEAM_MEMBER -> MeadowGreen
    ContactRole.OTHER -> MidsummerGold
}

private fun roleLabel(role: ContactRole) = when (role) {
    ContactRole.CONTRACTOR -> "Contractor"
    ContactRole.SUPPLIER -> "Supplier"
    ContactRole.TEAM_MEMBER -> "Team"
    ContactRole.OTHER -> "Other"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(container: AppContainer) {
    val viewModel: ContactsViewModel = viewModel(factory = ContactsViewModelFactory(container))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = LakeBlue)
                }
            }

            uiState.errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    SommarInfoBox(
                        emoji = "⚠️",
                        title = stringResource(R.string.common_error_title),
                        text = uiState.errorMessage ?: stringResource(R.string.common_error_default),
                        accentColor = MaterialTheme.colorScheme.error
                    )
                }
            }

            else -> ContactsContent(
                uiState = uiState,
                onEditContact = viewModel::onShowEditSheet,
                onDeleteContact = viewModel::onRequestDelete
            )
        }

        SommarButton(
            text = stringResource(R.string.contacts_fab),
            onClick = viewModel::onShowAddSheet,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        )
    }

    if (uiState.showSheet) {
        ModalBottomSheet(
            onDismissRequest = viewModel::onDismissSheet,
            sheetState = sheetState,
            containerColor = CreamBackground
        ) {
            ContactSheet(
                uiState = uiState,
                onNameChanged = viewModel::onDraftNameChanged,
                onPhoneChanged = viewModel::onDraftPhoneChanged,
                onEmailChanged = viewModel::onDraftEmailChanged,
                onRoleChanged = viewModel::onDraftRoleChanged,
                onSubmit = viewModel::onSubmitContact,
                onDismiss = viewModel::onDismissSheet
            )
        }
    }

    if (uiState.pendingDeleteId != null) {
        AlertDialog(
            onDismissRequest = viewModel::onCancelDelete,
            title = { Text(stringResource(R.string.contacts_dialog_delete_title)) },
            text = { Text(stringResource(R.string.contacts_dialog_delete_message)) },
            confirmButton = {
                TextButton(onClick = viewModel::onConfirmDelete) {
                    Text(stringResource(R.string.common_delete), color = FaluRed)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onCancelDelete) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }
}

@Composable
private fun ContactsContent(
    uiState: ContactsUiState,
    onEditContact: (Contact) -> Unit,
    onDeleteContact: (String) -> Unit
) {
    if (uiState.contacts.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            SommarInfoBox(
                emoji = "👤",
                title = stringResource(R.string.contacts_empty_title),
                text = stringResource(R.string.contacts_empty_message),
                accentColor = LakeBlue
            )
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(uiState.contacts) { index, contact ->
            ContactRow(
                contact = contact,
                onEdit = { onEditContact(contact) },
                onDelete = { onDeleteContact(contact.id) },
                modifier = Modifier.staggeredFadeIn(index)
            )
        }
    }
}

@Composable
private fun ContactRow(
    contact: Contact,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .cardShadow()
            .clip(SommarShapes.card)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, Border, SommarShapes.card)
            .clickable(onClick = onEdit)
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = contact.name,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (contact.phone.isNotBlank()) {
                    Text(
                        text = contact.phone,
                        style = MonoStyles.dataSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
                if (contact.email.isNotBlank()) {
                    Text(
                        text = contact.email,
                        style = MonoStyles.dataSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
        }

        Spacer(Modifier.width(8.dp))

        SommarBadge(
            text = roleLabel(contact.role),
            color = roleColor(contact.role),
            backgroundColor = roleColor(contact.role).copy(alpha = 0.08f),
            borderColor = roleColor(contact.role).copy(alpha = 0.2f)
        )

        Spacer(Modifier.width(8.dp))

        Text(
            text = "🗑",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .clickable(onClick = onDelete)
                .padding(4.dp)
        )
    }
}

@Composable
private fun ContactSheet(
    uiState: ContactsUiState,
    onNameChanged: (String) -> Unit,
    onPhoneChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onRoleChanged: (ContactRole) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit
) {
    val isEditing = uiState.editingContact != null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (isEditing) stringResource(R.string.contacts_sheet_title_edit) else stringResource(R.string.contacts_sheet_title_add),
            style = MaterialTheme.typography.titleMedium.copy(fontFamily = Fraunces),
            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
        )

        SommarTextField(
            value = uiState.draftName,
            onValueChange = onNameChanged,
            label = { Text(stringResource(R.string.contacts_field_name)) },
            singleLine = true,
            isError = uiState.sheetError != null && uiState.draftName.isBlank(),
            modifier = Modifier.fillMaxWidth()
        )

        SommarTextField(
            value = uiState.draftPhone,
            onValueChange = onPhoneChanged,
            label = { Text(stringResource(R.string.contacts_field_phone)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        SommarTextField(
            value = uiState.draftEmail,
            onValueChange = onEmailChanged,
            label = { Text(stringResource(R.string.contacts_field_email)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = stringResource(R.string.contacts_field_role),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ContactRole.entries.forEach { role ->
                SommarFilterChip(
                    text = roleLabel(role),
                    selected = uiState.draftRole == role,
                    onClick = { onRoleChanged(role) },
                    activeColor = roleColor(role)
                )
            }
        }

        if (uiState.sheetError != null) {
            Text(
                text = uiState.sheetError,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(4.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SommarOutlineButton(
                text = stringResource(R.string.common_cancel),
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            )
            if (uiState.isSaving) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = LakeBlue, modifier = Modifier.padding(8.dp))
                }
            } else {
                SommarButton(
                    text = if (isEditing) stringResource(R.string.common_save) else stringResource(R.string.contacts_button_add),
                    onClick = onSubmit,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
