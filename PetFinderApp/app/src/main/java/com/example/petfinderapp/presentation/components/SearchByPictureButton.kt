package com.example.petfinderapp.presentation.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.example.petfinderapp.R
import com.example.petfinderapp.presentation.utils.CameraUtils.openCamera
import com.example.petfinderapp.presentation.utils.ImageUtils.handleGalleryResult
import com.example.petfinderapp.presentation.viewModel.PetFinderVM

@Composable
fun SearchByPictureButton(
    context: Context,
    petFinderVM: PetFinderVM
) {
    var expanded by remember { mutableStateOf(false) }
    var imageUri: Uri? by remember { mutableStateOf(null) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    val getPictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val updatedImages = handleGalleryResult(
            resultCode = result.resultCode,
            data = result.data,
            existingImages = petFinderVM.searchImages.value
        )
        petFinderVM.updateSearchImages(updatedImages)
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            imageUri?.let { uri ->
                val updatedImages = petFinderVM.searchImages.value + uri.toString()
                petFinderVM.updateSearchImages(updatedImages)
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

    Box(
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.CenterEnd
    ) {
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.image_search_icon),
                contentDescription = "Search by photo",
                modifier = Modifier.size(34.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.wrapContentSize(),
            offset = DpOffset(x = 260.dp, y = 0.dp)
        ) {
            DropdownMenuItem(
                text = { Text("Select photo") },
                onClick = {
                    expanded = false
                    val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
                    getPictureLauncher.launch(intent)
                }
            )
            DropdownMenuItem(
                text = { Text("Take photo") },
                onClick = {
                    expanded = false
                    showPermissionDialog = true
                }
            )
        }
    }
}