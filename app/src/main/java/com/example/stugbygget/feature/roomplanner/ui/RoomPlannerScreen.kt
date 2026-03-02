package com.example.stugbygget.feature.roomplanner.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stugbygget.di.AppContainer

@Composable
fun RoomPlannerScreen(container: AppContainer) {
    val viewModel: RoomPlannerViewModel = viewModel(factory = RoomPlannerViewModelFactory(container))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedItem = uiState.furniture.firstOrNull { it.id == uiState.selectedFurnitureId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Rumsplanerare", style = MaterialTheme.typography.headlineSmall)
        Text("1 ruta = 50 cm", style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .background(Color(0xFFF7F3EC), shape = RoundedCornerShape(16.dp))
                .padding(8.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cellPx = size.width / (uiState.roomWidthCm / uiState.gridStepCm)

                for (x in 0..(uiState.roomWidthCm / uiState.gridStepCm)) {
                    val px = x * cellPx
                    drawLine(
                        color = Color(0xFFDFD8CC),
                        start = Offset(px, 0f),
                        end = Offset(px, size.height),
                        strokeWidth = 1f
                    )
                }
                val horizontalCount = (uiState.roomHeightCm / uiState.gridStepCm)
                for (y in 0..horizontalCount) {
                    val py = y * cellPx
                    drawLine(
                        color = Color(0xFFDFD8CC),
                        start = Offset(0f, py),
                        end = Offset(size.width, py),
                        strokeWidth = 1f
                    )
                }

                uiState.furniture.forEach { item ->
                    val left = (item.xCm / uiState.gridStepCm.toFloat()) * cellPx
                    val top = (item.yCm / uiState.gridStepCm.toFloat()) * cellPx
                    val width = (item.widthCm / uiState.gridStepCm.toFloat()) * cellPx
                    val height = (item.depthCm / uiState.gridStepCm.toFloat()) * cellPx
                    val isSelected = item.id == uiState.selectedFurnitureId

                    drawRect(
                        color = if (isSelected) Color(0xFF2E6B8A) else Color(0xFFA67D56),
                        topLeft = Offset(left, top),
                        size = Size(width, height)
                    )
                    if (isSelected) {
                        drawRect(
                            color = Color(0xFF173A4B),
                            topLeft = Offset(left, top),
                            size = Size(width, height),
                            style = Stroke(width = 4f)
                        )
                        val anchors = listOf(
                            Offset(left, top),
                            Offset(left + width, top),
                            Offset(left, top + height),
                            Offset(left + width, top + height)
                        )
                        anchors.forEach { point ->
                            drawCircle(color = Color.White, radius = 6f, center = point)
                        }
                    }
                }
            }
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
                                    yCm = touchOffset.y * cmPerPxY
                                )
                            },
                            onDrag = { change, _ ->
                                viewModel.onCanvasDragged(
                                    xCm = change.position.x * cmPerPxX,
                                    yCm = change.position.y * cmPerPxY
                                )
                                change.consume()
                            },
                            onDragEnd = {
                                viewModel.onCanvasDragEnd()
                            }
                        )
                    }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        selectedItem?.let {
            Text(
                text = "Vald möbel: ${it.label} (${it.widthCm / 100.0}m × ${it.depthCm / 100.0}m)",
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        uiState.errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Text("Möbelprimitiver", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(uiState.furniture, key = { item -> item.id }) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.onFurnitureSelected(item.id) }
                ) {
                    Row(modifier = Modifier.padding(12.dp)) {
                        Text(item.label, modifier = Modifier.weight(1f))
                        Text("${item.widthCm / 100.0} × ${item.depthCm / 100.0} m")
                    }
                }
            }
        }
    }
}
