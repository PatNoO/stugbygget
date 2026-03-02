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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
        Text("AR-mätning", style = MaterialTheme.typography.headlineSmall)
        Text(uiState.accuracyNote, style = MaterialTheme.typography.bodySmall)

        if (!uiState.hasCameraPermission) {
            Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                Text("Ge kameratillgång")
            }
            return
        }

        if (!uiState.sessionReady) {
            Text(
                text = uiState.errorMessage ?: "Startar AR-session...",
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
                text = "Uppmätt avstånd: %.2f m".format(distance),
                style = MaterialTheme.typography.titleMedium
            )
        } ?: Text("Tryck två punkter i kameravyn för att mäta.")

        Spacer(modifier = Modifier.height(4.dp))
        Text("Mätningen är en baseline-estimering tills full ARCore hit-testing är inkopplad.")
    }
}
