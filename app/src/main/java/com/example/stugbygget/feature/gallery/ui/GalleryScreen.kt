package com.example.stugbygget.feature.gallery.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stugbygget.di.AppContainer
import com.example.stugbygget.domain.model.PhotoItem
import com.example.stugbygget.domain.model.PhotoPhase
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun GalleryScreen(container: AppContainer) {
    val viewModel: GalleryViewModel = viewModel(factory = GalleryViewModelFactory(container))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when {
        uiState.isLoading -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) { CircularProgressIndicator() }
        }

        uiState.errorMessage != null -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Fel", style = MaterialTheme.typography.titleLarge)
                Text(uiState.errorMessage ?: "")
            }
        }

        else -> {
            val grouped = uiState.photos.groupBy { photo -> photo.phase }
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Galleri", style = MaterialTheme.typography.headlineSmall)
                        Text("Före · Under · Efter")

                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Rum")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                FilterChip(
                                    selected = uiState.selectedRoom == null,
                                    onClick = { viewModel.onRoomFilterSelected(null) },
                                    label = { Text("Alla") }
                                )
                            }
                            items(uiState.availableRooms) { room ->
                                FilterChip(
                                    selected = uiState.selectedRoom == room,
                                    onClick = { viewModel.onRoomFilterSelected(room) },
                                    label = { Text(room) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Fas")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                FilterChip(
                                    selected = uiState.selectedPhase == null,
                                    onClick = { viewModel.onPhaseFilterSelected(null) },
                                    label = { Text("Alla") }
                                )
                            }
                            items(PhotoPhase.entries) { phase ->
                                FilterChip(
                                    selected = uiState.selectedPhase == phase,
                                    onClick = { viewModel.onPhaseFilterSelected(phase) },
                                    label = { Text(phase.name) }
                                )
                            }
                        }
                    }
                }

                if (uiState.photos.isEmpty()) {
                    item {
                        Text(
                            text = "Inga bilder ännu",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                PhotoPhase.entries.forEach { phase ->
                    val itemsByPhase = grouped[phase].orEmpty()
                    if (itemsByPhase.isNotEmpty()) {
                        item {
                            Text(
                                text = phaseLabel(phase),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        items(itemsByPhase) { photo ->
                            PhotoCard(photo)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoCard(photo: PhotoItem) {
    val formatter = DateTimeFormatter.ofPattern("MMM yyyy")
    val month = formatter.format(photo.takenAt.atZone(ZoneId.systemDefault()))

    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color(0xFFECE7DE)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Bild: ${photo.roomName}")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(photo.description.ifBlank { "Ingen beskrivning" }, style = MaterialTheme.typography.bodyLarge)
            Text("$month · ${photo.roomName}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun phaseLabel(phase: PhotoPhase): String = when (phase) {
    PhotoPhase.BEFORE -> "Före"
    PhotoPhase.DURING -> "Under"
    PhotoPhase.AFTER -> "Efter"
}
