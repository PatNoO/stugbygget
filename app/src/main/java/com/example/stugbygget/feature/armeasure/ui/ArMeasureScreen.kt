package com.example.stugbygget.feature.armeasure.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stugbygget.di.AppContainer
import java.util.concurrent.Executor

@Composable
fun ArMeasureScreen(container: AppContainer) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mainExecutor: Executor = ContextCompat.getMainExecutor(context)
    val viewModel: ArMeasureViewModel = viewModel(factory = ArMeasureViewModelFactory(container))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = viewModel::onCameraPermissionResult
    )

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        viewModel.onCameraPermissionResult(granted)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("AR Measurement", style = MaterialTheme.typography.headlineSmall)
        Text(uiState.accuracyNote, style = MaterialTheme.typography.bodySmall)

        if (!uiState.hasCameraPermission) {
            Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                Text("Grant Camera Access")
            }
            return
        }

        if (!uiState.sessionReady) {
            Text(
                text = uiState.errorMessage ?: "Starting AR session...",
                color = if (uiState.errorMessage != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            return
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
                .background(Color.Black)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener(
                            {
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(surfaceProvider)
                                }
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview
                                )
                            },
                            mainExecutor
                        )
                    }
                }
            )

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { position ->
                            viewModel.onOverlayTapped(position.x, position.y)
                        }
                    }
            ) {
                uiState.points.forEach { point ->
                    drawCircle(
                        color = Color(0xFFD4A843),
                        radius = 12f,
                        center = Offset(point.x, point.y)
                    )
                }
                if (uiState.points.size == 2) {
                    drawLine(
                        color = Color(0xFFD4A843),
                        start = Offset(uiState.points[0].x, uiState.points[0].y),
                        end = Offset(uiState.points[1].x, uiState.points[1].y),
                        strokeWidth = 4f
                    )
                }
            }
        }

        uiState.measuredDistanceMeters?.let { distance ->
            Text(
                text = "Measured distance: %.2f m".format(distance),
                style = MaterialTheme.typography.titleMedium
            )
        } ?: Text("Tap two points in the camera preview to measure.")

        OutlinedTextField(
            value = uiState.measurementLabel,
            onValueChange = viewModel::onMeasurementLabelChanged,
            label = { Text("Measurement label") },
            modifier = Modifier.fillMaxWidth()
        )

        MeasurementTypeDropdown(
            selected = uiState.selectedType,
            onTypeSelected = viewModel::onMeasurementTypeSelected
        )

        androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = viewModel::onSaveMeasurement,
                enabled = uiState.measuredDistanceMeters != null && !uiState.isSaving
            ) {
                Text("Save")
            }
            Button(
                onClick = viewModel::onExportToRoomWidth,
                enabled = uiState.measuredDistanceMeters != null
            ) {
                Text("Export Width")
            }
            Button(
                onClick = viewModel::onExportToRoomHeight,
                enabled = uiState.measuredDistanceMeters != null
            ) {
                Text("Export Height")
            }
        }

        uiState.statusMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        uiState.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Spacer(modifier = Modifier.height(4.dp))
        Text("This is a baseline estimate until full ARCore hit-testing is wired.")
    }
}

@Composable
private fun MeasurementTypeDropdown(
    selected: String,
    onTypeSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("WALL", "WINDOW", "DOOR", "CUSTOM")

    Box {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text("Type") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = { expanded = true }, modifier = Modifier.padding(top = 8.dp, start = 8.dp)) {
            Text("Change")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onTypeSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
