package com.example.petfinderapp.presentation.components

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.example.petfinderapp.R
import com.example.petfinderapp.presentation.utils.CameraUtils.openCamera
import com.example.petfinderapp.presentation.viewModel.PetFinderVM

@Composable
fun SearchByPictureButton(
    context: Context,
    petFinderVM: PetFinderVM
) {
    var expanded by remember { mutableStateOf(false) }
    var imageUri: Uri? by remember { mutableStateOf(null) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showClearIcon by remember { mutableStateOf(false) }

    val getPictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                petFinderVM.searchImage(context, uri)
                showClearIcon = true
            }
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            imageUri?.let { uri ->
                petFinderVM.searchImage(context, imageUri!!)
                showClearIcon = true
            }
        }
    }

    if (showPermissionDialog) {
        RequestCameraPermission(
            onPermissionGranted = {
                showPermissionDialog = false
                openCamera(context, takePictureLauncher) { uri ->
                    imageUri = uri
                }
            },
            onPermissionDenied = {
                showPermissionDialog = false
                Toast.makeText(context, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show()
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .clickable { expanded = !expanded }
                .fillMaxWidth()
                .padding(end = 16.dp)
                .height(40.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.image_search_icon),
                contentDescription = "Search by photo",
                modifier = Modifier.size(34.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Image search",
                style = MaterialTheme.typography.bodyLarge
            )

            if (showClearIcon) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        petFinderVM.loadFilterCategories(context)
                        showClearIcon = false
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear search",
                        tint = Color(0xFF9D2A2A)
                    )
                }
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = !expanded },
            modifier = Modifier.size(width = 180.dp, height = 110.dp),
            offset = DpOffset(x = 260.dp, y = 0.dp)
        ) {
            DropdownMenuItem(
                text = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.select_photo_icon),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select photo")
                    }
                },
                onClick = {
                    expanded = false
                    val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
                    getPictureLauncher.launch(intent)
                }
            )
            DropdownMenuItem(
                text = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.take_photo_icon),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Take photo")
                    }
                },
                onClick = {
                    expanded = false
                    showPermissionDialog = true
                }
            )
        }
    }
}