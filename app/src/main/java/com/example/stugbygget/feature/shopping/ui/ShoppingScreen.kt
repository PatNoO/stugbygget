package com.example.stugbygget.feature.shopping.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stugbygget.di.AppContainer

@Composable
fun ShoppingScreen(container: AppContainer) {
    val viewModel: ShoppingViewModel = viewModel(factory = ShoppingViewModelFactory(container))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) { CircularProgressIndicator() }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Shopping Lists", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = uiState.listNameInput,
            onValueChange = viewModel::onListNameChanged,
            label = { Text("List name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = uiState.phaseInput,
            onValueChange = viewModel::onPhaseChanged,
            label = { Text("Phase id") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = viewModel::onCreateList) {
            Text("Create List")
        }

        uiState.errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(uiState.shoppingLists, key = { it.id }) { list ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(list.name, style = MaterialTheme.typography.titleMedium)
                        Text("Phase: ${list.phaseId}", style = MaterialTheme.typography.bodySmall)

                        Spacer(modifier = Modifier.height(8.dp))
                        Row {
                            OutlinedTextField(
                                value = uiState.itemNameInput,
                                onValueChange = viewModel::onItemNameChanged,
                                label = { Text("Item") },
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = uiState.itemQuantityInput,
                                onValueChange = viewModel::onItemQuantityChanged,
                                label = { Text("Qty") },
                                modifier = Modifier.width(90.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = uiState.itemUnitInput,
                                onValueChange = viewModel::onItemUnitChanged,
                                label = { Text("Unit") },
                                modifier = Modifier.width(80.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.onAddItem(list.id) }) {
                            Text("Add Item")
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        if (list.items.isEmpty()) {
                            Text("No items yet.")
                        } else {
                            list.items.forEach { item ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = item.purchased,
                                        onCheckedChange = { checked ->
                                            viewModel.onTogglePurchased(list.id, item.id, checked)
                                        }
                                    )
                                    Text("${item.name} · ${item.quantity} ${item.unit}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
