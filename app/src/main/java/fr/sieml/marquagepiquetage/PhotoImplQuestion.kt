/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.sieml.marquagepiquetage

import android.content.res.Configuration
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import fr.sieml.marquagepiquetage.ui.theme.MarquagePiquetageTheme

@Composable
fun PhotoImplQuestion(
    imageUri: Uri?,
    getNewImageUri: () -> Uri,
    onPhotoTaken: (Uri) -> Unit,
    onPhotoDeleted: (Uri) -> Unit = {}
) {
    val hasPhoto = imageUri != null
    val iconResource = if (hasPhoto) {
        Icons.Filled.Face
    } else {
        Icons.Filled.Face
    }
    var newImageUri: Uri? by remember { mutableStateOf(null) }
    val context = LocalContext.current

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { _ ->
            newImageUri?.let { onPhotoTaken(it) }
        }
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let { onPhotoTaken(it) }
        }
    )

    val dialogState = remember { mutableStateOf(false) }

    if (dialogState.value) {
        AlertDialog(
            onDismissRequest = {
                dialogState.value = false
            },
            title = {
                Text(text = "Choisir une photo")
            },
            text = {
                Column {
                    Text("Voulez-vous prendre une photo ou choisir depuis votre galerie ?")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        newImageUri = getNewImageUri()
                        if(newImageUri == null) {
                            Toast.makeText(context, "Une erreur est survenue", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        cameraLauncher.launch(newImageUri)
                        dialogState.value = false
                    }) {
                    Text("Prendre une photo")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        galleryLauncher.launch("image/*")
                        dialogState.value = false
                    }) {
                    Text("Choisir depuis la galerie")
                }
            }
        )
    }

    OutlinedButton(
        onClick = {
            dialogState.value = true
        },
        shape = MaterialTheme.shapes.small,
        contentPadding = PaddingValues()
    ) {
        Column {
            if (hasPhoto) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(96.dp)
                        .aspectRatio(4 / 3f)
                )
            } else {
                PhotoDefaultImage(
                    modifier = Modifier.padding(
                        horizontal = 86.dp,
                        vertical = 74.dp
                    )
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentSize(Alignment.Center)
                    .padding(vertical = 26.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = iconResource, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(
                        id = if (hasPhoto) {
                            R.string.retake_photo
                        } else {
                            R.string.add_photo
                        }
                    )
                )
                if(hasPhoto){
                    Row (modifier = Modifier
                        .clickable {
                            onPhotoDeleted(imageUri!!)
                        }
                        .padding(12.dp) ){
                        Spacer(modifier = Modifier.width(16.dp))
                        Icon(imageVector = Icons.Filled.Delete, contentDescription = null,
                            tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(
                                id = R.string.deletePhoto
                            ),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoDefaultImage(
    modifier: Modifier = Modifier,
    lightTheme: Boolean = LocalContentColor.current.luminance() < 0.5f,
) {
    val assetId = R.drawable.constructor
    Image(
        painter = painterResource(id = assetId),
        modifier = modifier,
        contentDescription = null
    )
}

@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PhotoQuestionPreview() {
    MarquagePiquetageTheme{
        Surface {
            PhotoImplQuestion(
                imageUri = Uri.parse("https://example.bogus/wow"),
                getNewImageUri = { Uri.EMPTY },
                onPhotoTaken = {},
            )
        }
    }
}
