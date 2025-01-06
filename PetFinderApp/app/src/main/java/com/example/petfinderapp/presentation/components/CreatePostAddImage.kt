package com.example.petfinderapp.presentation.components

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.rememberAsyncImagePainter
import com.example.petfinderapp.R
import com.example.petfinderapp.presentation.utils.ImageUtils.openGallery

@Composable
fun CreatePostAddImage(
    selectedImages: List<String>,
    imagesEmpty: Boolean,
    onImageSelected: (List<String>) -> Unit,
    getPictureLauncher: ActivityResultLauncher<Intent>,
    onShowPermissionDialogChange: (Boolean) -> Unit,
    onFullScreenImageIndexChange: (Int?) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.medium
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (selectedImages.isEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    IconButton(onClick = { openGallery(getPictureLauncher) }) {
                        Icon(
                            painter = painterResource(id = R.drawable.select_photo_icon),
                            contentDescription = "Select photos",
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Text("Select photos", style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    IconButton(onClick = { onShowPermissionDialogChange(true) }) {
                        Icon(
                            painter = painterResource(id = R.drawable.take_photo_icon),
                            contentDescription = "Take photo",
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Text("Take photo", style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(selectedImages.reversed()) { uri ->
                    Box(
                        modifier = Modifier.size(150.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(model = uri),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable {
                                    onFullScreenImageIndexChange(selectedImages.indexOf(uri))
                                }
                        )

                        IconButton(
                            onClick = {
                                onImageSelected(selectedImages.filter { it != uri })
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .padding(4.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.delete),
                                contentDescription = "Delete photo",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.size(150.dp)
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .padding(bottom = 16.dp)
                                .clickable { openGallery(getPictureLauncher) }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.select_photo_icon),
                                contentDescription = "Select photos",
                                modifier = Modifier.size(36.dp)
                            )
                            Text("Select photos", style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { onShowPermissionDialogChange(true) }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.take_photo_icon),
                                contentDescription = "Take photo",
                                modifier = Modifier.size(36.dp)
                            )
                            Text("Take photo", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
    if (imagesEmpty) {
        Text(
            "At least one photo is required",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
        )
    }
}