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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.example.stugbygget.R
import com.example.stugbygget.ui.theme.Border
import com.example.stugbygget.ui.theme.LakeBlue

private val thumbShape = RoundedCornerShape(8.dp)
private val descShape = RoundedCornerShape(4.dp)
private val thumbSize = 72.dp

private class PhotoEntry(val uri: Uri) {
    var description by mutableStateOf("")
}

/**
 * A reusable photo picker row for forms.
 * Displays selected photo thumbnails with a description field below each one,
 * and an add button that opens the image picker.
 *
 * @param onPhotosChanged Called whenever the photo list or any description changes.
 *   Passes the current list of (uri, description) pairs. Defaults to no-op.
 */
@Composable
fun SommarPhotoPicker(
    modifier: Modifier = Modifier,
    onPhotosChanged: (List<Pair<Uri, String>>) -> Unit = {},
) {
    val photos = remember { mutableStateListOf<PhotoEntry>() }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
    ) { uris ->
        uris.forEach { photos.add(PhotoEntry(it)) }
        onPhotosChanged(photos.map { it.uri to it.description })
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
            // Selected thumbnails with description fields
            items(photos) { entry ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    SubcomposeAsyncImage(
                        model = entry.uri,
                        contentDescription = entry.description.ifBlank { null },
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(thumbSize)
                            .aspectRatio(1f)
                            .clip(thumbShape)
                            .border(1.dp, Border, thumbShape),
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

                    // Description field
                    val hint = stringResource(R.string.photo_description_hint)
                    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
                    val onSurface = MaterialTheme.colorScheme.onSurface
                    val surface = MaterialTheme.colorScheme.surface
                    Box(
                        modifier = Modifier
                            .width(thumbSize)
                            .clip(descShape)
                            .border(1.dp, Border, descShape)
                            .background(surface)
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                    ) {
                        if (entry.description.isEmpty()) {
                            Text(
                                text = hint,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = onSurfaceVariant.copy(alpha = 0.6f),
                                ),
                            )
                        }
                        BasicTextField(
                            value = entry.description,
                            onValueChange = {
                                entry.description = it
                                onPhotosChanged(photos.map { p -> p.uri to p.description })
                            },
                            textStyle = MaterialTheme.typography.labelSmall.copy(color = onSurface),
                            maxLines = 2,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            // Add button
            item {
                Box(
                    modifier = Modifier
                        .size(thumbSize)
                        .clip(thumbShape)
                        .background(LakeBlue.copy(alpha = 0.06f))
                        .border(1.dp, LakeBlue.copy(alpha = 0.3f), thumbShape)
                        .clickable { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "📷\n+",
                        style = MaterialTheme.typography.labelMedium.copy(color = LakeBlue),
                    )
                }
            }
        }
    }
}
