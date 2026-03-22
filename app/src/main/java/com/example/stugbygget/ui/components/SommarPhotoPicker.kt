package com.example.stugbygget.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.SubcomposeAsyncImage
import com.example.stugbygget.R
import com.example.stugbygget.ui.theme.Border
import com.example.stugbygget.ui.theme.LakeBlue
import com.example.stugbygget.ui.theme.TextDark
import java.io.File

private val thumbShape = RoundedCornerShape(8.dp)
private val descShape = RoundedCornerShape(6.dp)
private val thumbSize = 72.dp

private class PhotoEntry(val uri: Uri) {
    var description by mutableStateOf("")
}

/**
 * A reusable photo picker row for forms.
 * Tapping "+" opens a dialog to choose between camera or gallery.
 * Thumbnails are shown in a horizontal row. Tapping a thumbnail selects it
 * and reveals a full-width description field below the row.
 *
 * @param onPhotosChanged Called whenever the photo list or any description changes.
 *   Passes the current list of (uri, description) pairs. Defaults to no-op.
 */
@Composable
fun SommarPhotoPicker(
    modifier: Modifier = Modifier,
    onPhotosChanged: (List<Pair<Uri, String>>) -> Unit = {},
) {
    val context = LocalContext.current
    val photos = remember { mutableStateListOf<PhotoEntry>() }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var showSourceDialog by remember { mutableStateOf(false) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
    ) { uris ->
        uris.forEach { photos.add(PhotoEntry(it)) }
        onPhotosChanged(photos.map { it.uri to it.description })
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { success ->
        if (success) {
            pendingCameraUri?.let { uri ->
                photos.add(PhotoEntry(uri))
                onPhotosChanged(photos.map { it.uri to it.description })
            }
        }
        pendingCameraUri = null
    }

    fun launchCamera() {
        val tmpFile = File.createTempFile("photo_", ".jpg", context.cacheDir)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tmpFile,
        )
        pendingCameraUri = uri
        cameraLauncher.launch(uri)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = stringResource(R.string.common_photos_label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(photos) { entry ->
                val isSelected = selectedUri == entry.uri
                SubcomposeAsyncImage(
                    model = entry.uri,
                    contentDescription = entry.description.ifBlank { null },
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(thumbSize)
                        .aspectRatio(1f)
                        .clip(thumbShape)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) LakeBlue else Border,
                            shape = thumbShape,
                        )
                        .clickable {
                            selectedUri = if (isSelected) null else entry.uri
                        },
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(LakeBlue.copy(alpha = 0.08f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("📷", style = MaterialTheme.typography.bodySmall)
                        }
                    },
                )
            }

            // Add button
            item {
                Box(
                    modifier = Modifier
                        .size(thumbSize)
                        .clip(thumbShape)
                        .background(LakeBlue.copy(alpha = 0.06f))
                        .border(1.dp, LakeBlue.copy(alpha = 0.3f), thumbShape)
                        .clickable { showSourceDialog = true },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "📷\n+",
                        style = MaterialTheme.typography.labelMedium.copy(color = LakeBlue),
                    )
                }
            }
        }

        // Description field — shown below the row when a photo is selected
        val selectedEntry = photos.firstOrNull { it.uri == selectedUri }
        if (selectedEntry != null) {
            Spacer(Modifier.height(2.dp))
            val hint = stringResource(R.string.photo_description_hint)
            val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
            val surface = MaterialTheme.colorScheme.surface
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(descShape)
                    .border(1.5.dp, LakeBlue.copy(alpha = 0.4f), descShape)
                    .background(surface)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
            ) {
                if (selectedEntry.description.isEmpty()) {
                    Text(
                        text = hint,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = onSurfaceVariant.copy(alpha = 0.6f),
                        ),
                    )
                }
                BasicTextField(
                    value = selectedEntry.description,
                    onValueChange = {
                        selectedEntry.description = it
                        onPhotosChanged(photos.map { p -> p.uri to p.description })
                    },
                    textStyle = MaterialTheme.typography.bodySmall.copy(color = TextDark),
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }

    // Source picker dialog
    if (showSourceDialog) {
        AlertDialog(
            onDismissRequest = { showSourceDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = stringResource(R.string.photo_source_title),
                    style = MaterialTheme.typography.titleMedium,
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SommarButton(
                        text = "📷  " + stringResource(R.string.photo_source_camera),
                        onClick = {
                            showSourceDialog = false
                            launchCamera()
                        },
                    )
                    SommarOutlineButton(
                        text = "🖼️  " + stringResource(R.string.photo_source_gallery),
                        onClick = {
                            showSourceDialog = false
                            galleryLauncher.launch("image/*")
                        },
                    )
                }
            },
            confirmButton = {},
        )
    }
}
