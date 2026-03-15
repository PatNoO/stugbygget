package com.example.stugbygget.feature.roomplanner.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stugbygget.di.AppContainer
import com.example.stugbygget.domain.model.RoomFurniture
import com.example.stugbygget.ui.components.SommarCard
import com.example.stugbygget.ui.components.SommarHeaderCard
import com.example.stugbygget.ui.components.SommarInfoBox
import com.example.stugbygget.ui.components.SommarSectionTitle
import com.example.stugbygget.ui.components.SommarStatCard
import com.example.stugbygget.ui.theme.Border
import com.example.stugbygget.ui.theme.FaluRed
import com.example.stugbygget.ui.theme.LakeBlue
import com.example.stugbygget.ui.theme.MeadowGreen
import com.example.stugbygget.ui.theme.MonoStyles
import com.example.stugbygget.ui.theme.SommarGradients
import com.example.stugbygget.ui.theme.SommarShapes
import com.example.stugbygget.ui.theme.StugbyggetShapes
import com.example.stugbygget.ui.theme.TextDark
import com.example.stugbygget.ui.theme.TextLight
import com.example.stugbygget.ui.theme.WoodWarm
import com.example.stugbygget.ui.theme.fadeUpIn
import kotlin.math.roundToInt

private data class CatalogueItem(
    val emoji: String,
    val name: String,
    val widthCm: Int,
    val depthCm: Int,
)

private val FURNITURE_CATALOGUE = listOf(
    CatalogueItem("🛋️", "Sofa", 200, 90),
    CatalogueItem("🛏️", "Bed", 160, 200),
    CatalogueItem("🪑", "Dining table", 120, 80),
    CatalogueItem("🔥", "Wood stove", 60, 60),
    CatalogueItem("🛁", "Bathtub", 170, 80),
    CatalogueItem("🚿", "Sink", 60, 50),
    CatalogueItem("🧊", "Fridge", 60, 65),
    CatalogueItem("📚", "Bookshelf", 80, 30),
)

private val CanvasFloor = Color(0xFFF5ECD7)
private val CanvasGrid = Color(0xFFDFD8CC)

@Composable
fun RoomPlannerScreen(container: AppContainer) {
    val viewModel: RoomPlannerViewModel = viewModel(factory = RoomPlannerViewModelFactory(container))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when {
        uiState.isLoading -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator(color = WoodWarm)
            }
            return
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp),
    ) {
        // ── Header ──
        SommarHeaderCard(
            gradient = SommarGradients.woodWarm,
            title = "Drag & Drop Furniture 🪵",
            modifier = Modifier.padding(vertical = 12.dp),
        )

        // ── Room info stats ──
        val areaSqm = (uiState.roomWidthCm / 100.0) * (uiState.roomHeightCm / 100.0)
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .fadeUpIn(delay = 50),
        ) {
            SommarStatCard(
                value = uiState.roomId.replaceFirstChar { it.uppercase() },
                label = "room",
                color = WoodWarm,
                modifier = Modifier.weight(1f),
            )
            SommarStatCard(
                value = "${(areaSqm * 10).roundToInt() / 10.0}",
                label = "m²",
                color = WoodWarm,
                modifier = Modifier.weight(1f),
            )
            SommarStatCard(
                value = "${uiState.furniture.size}",
                label = "items",
                color = MeadowGreen,
                modifier = Modifier.weight(1f),
            )
        }

        // ── Canvas ──
        val selectedItem = uiState.furniture.firstOrNull { it.id == uiState.selectedFurnitureId }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(StugbyggetShapes.medium)
                .background(CanvasFloor)
                .border(3.dp, FaluRed, StugbyggetShapes.medium),
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cellPx = size.width / (uiState.roomWidthCm / uiState.gridStepCm)

                // ── Grid lines ──
                for (x in 0..(uiState.roomWidthCm / uiState.gridStepCm)) {
                    val px = x * cellPx
                    drawLine(CanvasGrid, Offset(px, 0f), Offset(px, size.height), 1f)
                }
                val hCount = uiState.roomHeightCm / uiState.gridStepCm
                for (y in 0..hCount) {
                    val py = y * cellPx
                    drawLine(CanvasGrid, Offset(0f, py), Offset(size.width, py), 1f)
                }

                // ── Door placeholder (WoodWarm, bottom wall centre) ──
                val doorPx = (90f / uiState.gridStepCm) * cellPx
                drawRect(
                    color = WoodWarm,
                    topLeft = Offset((size.width - doorPx) / 2f, size.height - 6f),
                    size = Size(doorPx, 6f),
                )

                // ── Window placeholder (LakeBlue, top wall centre) ──
                val windowPx = (120f / uiState.gridStepCm) * cellPx
                drawRect(
                    color = LakeBlue,
                    topLeft = Offset((size.width - windowPx) / 2f, 0f),
                    size = Size(windowPx, 6f),
                )

                // ── Furniture ──
                uiState.furniture.forEach { item ->
                    val left = (item.xCm / uiState.gridStepCm.toFloat()) * cellPx
                    val top = (item.yCm / uiState.gridStepCm.toFloat()) * cellPx
                    val w = (item.widthCm / uiState.gridStepCm.toFloat()) * cellPx
                    val h = (item.depthCm / uiState.gridStepCm.toFloat()) * cellPx
                    val isSelected = item.id == uiState.selectedFurnitureId

                    drawRect(
                        color = if (isSelected) LakeBlue.copy(alpha = 0.85f) else WoodWarm.copy(alpha = 0.7f),
                        topLeft = Offset(left, top),
                        size = Size(w, h),
                    )
                    drawRect(
                        color = if (isSelected) LakeBlue else WoodWarm,
                        topLeft = Offset(left, top),
                        size = Size(w, h),
                        style = Stroke(width = if (isSelected) 3f else 1.5f),
                    )

                    if (isSelected) {
                        // Corner anchors
                        listOf(
                            Offset(left, top),
                            Offset(left + w, top),
                            Offset(left, top + h),
                            Offset(left + w, top + h),
                        ).forEach { pt ->
                            drawCircle(Color.White, radius = 5f, center = pt)
                            drawCircle(LakeBlue, radius = 5f, center = pt, style = Stroke(2f))
                        }

                        // Dimension label
                        val label = "${item.widthCm / 100.0}m × ${item.depthCm / 100.0}m"
                        val paint = android.graphics.Paint().apply {
                            color = Color.White.toArgb()
                            textSize = 26f
                            isAntiAlias = true
                            textAlign = android.graphics.Paint.Align.CENTER
                            typeface = android.graphics.Typeface.MONOSPACE
                        }
                        drawContext.canvas.nativeCanvas.drawText(
                            label,
                            left + w / 2f,
                            top + h / 2f + 9f,
                            paint,
                        )
                    }
                }
            }

            // Touch layer for drag
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(uiState.furniture, uiState.selectedFurnitureId) {
                        val cmPerPxX = uiState.roomWidthCm.toFloat() / size.width.toFloat()
                        val cmPerPxY = uiState.roomHeightCm.toFloat() / size.height.toFloat()
                        detectDragGestures(
                            onDragStart = { touchOffset ->
                                viewModel.onCanvasDragStart(
                                    xCm = touchOffset.x * cmPerPxX,
                                    yCm = touchOffset.y * cmPerPxY,
                                )
                            },
                            onDrag = { change, _ ->
                                viewModel.onCanvasDragged(
                                    xCm = change.position.x * cmPerPxX,
                                    yCm = change.position.y * cmPerPxY,
                                )
                                change.consume()
                            },
                            onDragEnd = { viewModel.onCanvasDragEnd() },
                        )
                    },
            )
        }

        // ── Error message ──
        uiState.errorMessage?.let { error ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clip(StugbyggetShapes.small)
                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.08f))
                    .padding(10.dp),
            ) {
                Text(
                    text = "⚠️ $error",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.error,
                    ),
                )
            }
        }

        // ── Usage tip ──
        SommarInfoBox(
            emoji = "💡",
            title = "Tip",
            text = "Tap a furniture item to select it, then drag on the canvas to move it. Layout is saved automatically.",
            accentColor = WoodWarm,
            modifier = Modifier.padding(top = 16.dp),
        )

        // ── Placed furniture badges ──
        if (uiState.furniture.isNotEmpty()) {
            SommarSectionTitle(
                text = "Placed furniture",
                modifier = Modifier.padding(top = 20.dp, bottom = 0.dp),
            )
            uiState.furniture.chunked(3).forEach { rowItems ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                ) {
                    rowItems.forEach { item ->
                        PlacedFurnitureBadge(
                            item = item,
                            isSelected = item.id == uiState.selectedFurnitureId,
                            onSelect = { viewModel.onFurnitureSelected(item.id) },
                        )
                    }
                }
            }
        }

        // ── Furniture catalogue ──
        SommarSectionTitle(
            text = "Furniture catalogue",
            modifier = Modifier.padding(top = 20.dp, bottom = 0.dp),
        )
        FURNITURE_CATALOGUE.chunked(2).forEach { pair ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
            ) {
                pair.forEach { item ->
                    CatalogueCard(item = item, modifier = Modifier.weight(1f))
                }
                // Fill empty slot if odd number
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun PlacedFurnitureBadge(
    item: RoomFurniture,
    isSelected: Boolean,
    onSelect: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(SommarShapes.badge)
            .background(if (isSelected) LakeBlue.copy(alpha = 0.12f) else WoodWarm.copy(alpha = 0.08f))
            .border(
                1.dp,
                if (isSelected) LakeBlue.copy(alpha = 0.4f) else Border,
                SommarShapes.badge,
            )
            .clickable(onClick = onSelect)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = item.label,
            style = MonoStyles.dataSmall.copy(
                color = if (isSelected) LakeBlue else TextDark,
            ),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "×",
            style = MaterialTheme.typography.labelMedium.copy(color = TextLight),
            // TODO: implement removeFurniture in ViewModel and wire here
        )
    }
}

@Composable
private fun CatalogueCard(
    item: CatalogueItem,
    modifier: Modifier = Modifier,
) {
    SommarCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(item.emoji, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "${item.widthCm / 100.0}m × ${item.depthCm / 100.0}m",
                    style = MonoStyles.dataSmall.copy(color = TextLight),
                )
            }
        }
    }
}
