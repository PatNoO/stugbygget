package com.example.stugbygget.feature.armeasure.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stugbygget.core.ui.CameraPreviewSurface
import com.example.stugbygget.di.AppContainer
import com.example.stugbygget.ui.components.SommarButton
import com.example.stugbygget.ui.components.SommarCard
import com.example.stugbygget.ui.components.SommarFilterChip
import com.example.stugbygget.ui.components.SommarHeaderCard
import com.example.stugbygget.ui.components.SommarInfoBox
import com.example.stugbygget.ui.components.SommarOutlineButton
import com.example.stugbygget.ui.components.SommarSectionTitle
import com.example.stugbygget.ui.theme.Border
import com.example.stugbygget.ui.theme.FaluRed
import com.example.stugbygget.ui.theme.Fraunces
import com.example.stugbygget.ui.theme.LakeBlue
import com.example.stugbygget.ui.theme.MeadowGreen
import com.example.stugbygget.ui.theme.MidsummerGold
import com.example.stugbygget.ui.theme.MonoStyles
import com.example.stugbygget.ui.theme.SommarGradients
import com.example.stugbygget.ui.theme.StugbyggetShapes
import com.example.stugbygget.ui.theme.fadeUpIn

private val MEASUREMENT_TYPES = listOf("WALL", "WINDOW", "DOOR", "CUSTOM")

@Composable
fun ArMeasureScreen(container: AppContainer) {
    val context = LocalContext.current
    val viewModel: ArMeasureViewModel = viewModel(factory = ArMeasureViewModelFactory(container))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = viewModel::onCameraPermissionResult,
    )

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA,
        ) == PackageManager.PERMISSION_GRANTED
        viewModel.onCameraPermissionResult(granted)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp),
    ) {
        // ── Header ──
        SommarHeaderCard(
            gradient = SommarGradients.lakeBlue,
            title = "Measure with Camera 📐",
            subtitle = uiState.accuracyNote,
            modifier = Modifier.padding(vertical = 12.dp),
        )

        // ── Camera permission gate ──
        if (!uiState.hasCameraPermission) {
            SommarInfoBox(
                emoji = "📷",
                title = "Camera access needed",
                text = "Camera permission is required to use AR measurement.",
                accentColor = LakeBlue,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            SommarButton(
                text = "Grant Camera Access",
                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
            )
            Spacer(Modifier.height(24.dp))
            return@Column
        }

        // ── ARCore unavailable / session starting ──
        if (!uiState.sessionReady) {
            SommarInfoBox(
                emoji = if (uiState.errorMessage != null) "🚫" else "⏳",
                title = if (uiState.errorMessage != null) "AR unavailable" else "Starting AR session…",
                text = uiState.errorMessage
                    ?: "Initialising ARCore. Point the camera at a flat surface.",
                accentColor = if (uiState.errorMessage != null) MaterialTheme.colorScheme.error else LakeBlue,
                modifier = Modifier.padding(bottom = 24.dp),
            )
            return@Column
        }

        // ── Camera + overlay ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .clip(StugbyggetShapes.medium)
                .background(Color.Black)
                .border(2.dp, LakeBlue.copy(alpha = 0.4f), StugbyggetShapes.medium),
        ) {
            CameraPreviewSurface(modifier = Modifier.fillMaxSize())

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { position ->
                            viewModel.onOverlayTapped(position.x, position.y)
                        }
                    },
            ) {
                uiState.points.forEach { point ->
                    drawCircle(
                        color = MidsummerGold,
                        radius = 14f,
                        center = Offset(point.x, point.y),
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 14f,
                        center = Offset(point.x, point.y),
                        style = Stroke(width = 2f),
                    )
                }
                if (uiState.points.size == 2) {
                    drawLine(
                        color = MidsummerGold,
                        start = Offset(uiState.points[0].x, uiState.points[0].y),
                        end = Offset(uiState.points[1].x, uiState.points[1].y),
                        strokeWidth = 3f,
                    )
                }
            }

            if (uiState.points.isEmpty()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                        .clip(StugbyggetShapes.extraSmall)
                        .background(Color.Black.copy(alpha = 0.55f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = "Tap two points to measure",
                        style = MonoStyles.dataSmall.copy(color = Color.White),
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Distance result ──
        if (uiState.measuredDistanceMeters != null) {
            SommarCard(modifier = Modifier.padding(bottom = 12.dp).fadeUpIn()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Distance",
                        style = MonoStyles.dataSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                    )
                    Text(
                        text = "%.2f m".format(uiState.measuredDistanceMeters),
                        style = MonoStyles.measurementMedium.copy(color = MidsummerGold),
                    )
                }
            }
        } else {
            Text(
                text = "Tap two points in the camera view to measure a distance.",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                modifier = Modifier.padding(bottom = 12.dp),
            )
        }

        // ── Label input ──
        SommarSectionTitle(text = "Label")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(StugbyggetShapes.small)
                .background(MaterialTheme.colorScheme.background)
                .border(1.dp, Border, StugbyggetShapes.small)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            if (uiState.measurementLabel.isEmpty()) {
                Text(
                    text = "e.g. Wall A-B",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                )
            }
            BasicTextField(
                value = uiState.measurementLabel,
                onValueChange = viewModel::onMeasurementLabelChanged,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(LakeBlue),
            )
        }

        // ── Type chips ──
        SommarSectionTitle(text = "Type", modifier = Modifier.padding(top = 12.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(bottom = 16.dp),
        ) {
            items(MEASUREMENT_TYPES) { type ->
                SommarFilterChip(
                    text = type,
                    selected = uiState.selectedType == type,
                    onClick = { viewModel.onMeasurementTypeSelected(type) },
                    activeColor = typeChipColor(type),
                )
            }
        }

        // ── Action buttons ──
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            SommarButton(
                text = "Scan room",
                onClick = { /* TODO: wire to onScanRoomClicked in ViewModel */ },
                modifier = Modifier.weight(1f),
            )
            SommarOutlineButton(
                text = "Measure",
                onClick = viewModel::onSaveMeasurement,
                color = if (uiState.measuredDistanceMeters != null && !uiState.isSaving)
                    LakeBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
        }

        // ── Status / error feedback ──
        uiState.statusMessage?.let { msg ->
            SommarInfoBox(
                emoji = "✅",
                title = "Done",
                text = msg,
                accentColor = MeadowGreen,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
        uiState.errorMessage?.let { error ->
            SommarInfoBox(
                emoji = "⚠️",
                title = "Error",
                text = error,
                accentColor = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 12.dp),
            )
        }

        // ── Saved measurements list ──
        // TODO: wire to ObserveMeasurementsUseCase — shows empty state until list is wired
        SommarSectionTitle(
            text = "Saved measurements",
            modifier = Modifier.padding(top = 20.dp),
        )
        SommarInfoBox(
            emoji = "📏",
            title = "No measurements yet",
            text = "Save a measurement above — it will appear here.",
            accentColor = LakeBlue,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        // ── Calculated area card ──
        // TODO: wire to CalculateAreaUseCase once wall measurements are persisted
        SommarSectionTitle(text = "Calculated area")
        SommarCard(modifier = Modifier.padding(bottom = 24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Total area",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                )
                Text(
                    text = "— m²",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontFamily = Fraunces,
                        color = MeadowGreen,
                    ),
                )
            }
            Text(
                text = "Add WALL measurements to calculate room area.",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun typeChipColor(type: String): Color = when (type) {
    "WALL" -> LakeBlue
    "WINDOW" -> MidsummerGold
    "DOOR" -> FaluRed
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}
