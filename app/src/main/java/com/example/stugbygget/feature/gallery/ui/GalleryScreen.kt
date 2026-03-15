package com.example.stugbygget.feature.gallery.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import com.example.stugbygget.di.AppContainer
import com.example.stugbygget.domain.model.PhotoItem
import com.example.stugbygget.domain.model.PhotoPhase
import com.example.stugbygget.ui.components.SommarBadge
import com.example.stugbygget.ui.components.SommarCard
import com.example.stugbygget.ui.components.SommarFilterChip
import com.example.stugbygget.ui.components.SommarHeaderCard
import com.example.stugbygget.ui.components.SommarInfoBox
import com.example.stugbygget.ui.components.SommarSectionTitle
import com.example.stugbygget.ui.theme.GalleryAfter
import com.example.stugbygget.ui.theme.GalleryBefore
import com.example.stugbygget.ui.theme.GalleryDuring
import com.example.stugbygget.ui.theme.LakeBlue
import com.example.stugbygget.ui.theme.MonoStyles
import com.example.stugbygget.ui.theme.SommarGradients
import com.example.stugbygget.ui.theme.SommarShapes
import com.example.stugbygget.ui.theme.staggeredFadeIn
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val monthFormatter = DateTimeFormatter
    .ofPattern("MMM yyyy", Locale.ENGLISH)
    .withZone(ZoneId.systemDefault())

private fun phaseColor(phase: PhotoPhase): Color = when (phase) {
    PhotoPhase.BEFORE -> GalleryBefore
    PhotoPhase.DURING -> GalleryDuring
    PhotoPhase.AFTER -> GalleryAfter
}

private fun phaseLabel(phase: PhotoPhase): String = when (phase) {
    PhotoPhase.BEFORE -> "Before"
    PhotoPhase.DURING -> "During"
    PhotoPhase.AFTER -> "After"
}

@Composable
fun GalleryScreen(container: AppContainer) {
    val viewModel: GalleryViewModel = viewModel(factory = GalleryViewModelFactory(container))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when {
        uiState.isLoading -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator(color = LakeBlue)
            }
        }

        uiState.errorMessage != null -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                SommarInfoBox(
                    emoji = "⚠️",
                    title = "Error",
                    text = uiState.errorMessage ?: "Something went wrong.",
                    accentColor = MaterialTheme.colorScheme.error,
                )
            }
        }

        else -> GalleryContent(
            uiState = uiState,
            onRoomSelected = viewModel::onRoomFilterSelected,
            onPhaseSelected = viewModel::onPhaseFilterSelected,
        )
    }
}

@Composable
private fun GalleryContent(
    uiState: GalleryUiState,
    onRoomSelected: (String?) -> Unit,
    onPhaseSelected: (PhotoPhase?) -> Unit,
) {
    val grouped = uiState.photos.groupBy { it.phase }

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        contentPadding = PaddingValues(18.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalItemSpacing = 10.dp,
    ) {
        // ── Header ──
        item(span = StaggeredGridItemSpan.FullLine) {
            SommarHeaderCard(
                gradient = SommarGradients.lakeBlue,
                title = "Renovation Journey 📸",
                modifier = Modifier.padding(bottom = 8.dp),
            ) {
                Text(
                    text = "${uiState.photos.size} photos · Before · During · After",
                    style = MonoStyles.dataSmall.copy(color = Color.White.copy(alpha = 0.8f)),
                )
            }
        }

        // ── Room filter chips ──
        item(span = StaggeredGridItemSpan.FullLine) {
            val rooms = listOf(null) + uiState.availableRooms
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 4.dp),
            ) {
                items(rooms) { room ->
                    SommarFilterChip(
                        text = room ?: "All rooms",
                        selected = uiState.selectedRoom == room,
                        onClick = { onRoomSelected(room) },
                        activeColor = LakeBlue,
                    )
                }
            }
        }

        // ── Phase filter chips ──
        item(span = StaggeredGridItemSpan.FullLine) {
            val phases = listOf(null) + PhotoPhase.entries
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 8.dp),
            ) {
                items(phases) { phase ->
                    SommarFilterChip(
                        text = if (phase == null) "All phases" else phaseLabel(phase),
                        selected = uiState.selectedPhase == phase,
                        onClick = { onPhaseSelected(phase) },
                        activeColor = if (phase == null) LakeBlue else phaseColor(phase),
                    )
                }
            }
        }

        // ── Empty state ──
        if (uiState.photos.isEmpty()) {
            item(span = StaggeredGridItemSpan.FullLine) {
                SommarInfoBox(
                    emoji = "📸",
                    title = if (uiState.selectedRoom != null || uiState.selectedPhase != null)
                        "No photos match this filter"
                    else
                        "No photos yet",
                    text = if (uiState.selectedRoom != null || uiState.selectedPhase != null)
                        "Try selecting a different filter."
                    else
                        "Photos will appear here once uploaded to the project.",
                    accentColor = LakeBlue,
                )
            }
        }

        // ── Photos grouped by phase ──
        PhotoPhase.entries.forEach { phase ->
            val photosInPhase = grouped[phase].orEmpty()
            if (photosInPhase.isNotEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    SommarSectionTitle(
                        text = phaseLabel(phase),
                        color = phaseColor(phase),
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                photosInPhase.forEachIndexed { index, photo ->
                    item {
                        PhotoCard(
                            photo = photo,
                            modifier = Modifier.staggeredFadeIn(index),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoCard(
    photo: PhotoItem,
    modifier: Modifier = Modifier,
) {
    val month = monthFormatter.format(photo.takenAt)
    val color = phaseColor(photo.phase)

    SommarCard(modifier = modifier) {
        // ── Photo image ──
        SubcomposeAsyncImage(
            model = photo.downloadUrl,
            contentDescription = photo.description.ifBlank { photo.roomName },
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(SommarShapes.thumbnail),
            error = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .background(color.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("📷", style = MaterialTheme.typography.headlineLarge)
                }
            },
        )

        Spacer(Modifier.height(8.dp))

        // ── Phase badge ──
        SommarBadge(
            text = phaseLabel(photo.phase).uppercase(),
            color = color,
            backgroundColor = color.copy(alpha = 0.08f),
            borderColor = color.copy(alpha = 0.2f),
        )

        Spacer(Modifier.height(4.dp))

        // ── Description & meta ──
        Text(
            text = photo.description.ifBlank { photo.roomName },
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = "$month · ${photo.roomName}",
            style = MonoStyles.dataSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
        )
    }
}
