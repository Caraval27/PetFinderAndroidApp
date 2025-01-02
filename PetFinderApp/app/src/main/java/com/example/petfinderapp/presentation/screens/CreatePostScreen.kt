package com.example.petfinderapp.presentation.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.example.petfinderapp.domain.PostType
import com.example.petfinderapp.presentation.components.RequestCameraPermission
import com.example.petfinderapp.presentation.utils.CameraUtils.openCamera
import com.example.petfinderapp.presentation.utils.ImageUtils.handleGalleryResult
import com.example.petfinderapp.presentation.utils.ImageUtils.openGallery
import com.example.petfinderapp.presentation.viewModel.PetFinderVM

@Composable
fun CreatePostScreen(
    petFinderVM: PetFinderVM
) {
    var title by remember { mutableStateOf("") }
    var animalType by remember { mutableStateOf("") }
    var race by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var description by remember { mutableStateOf(TextFieldValue("")) }
    var postType by remember { mutableStateOf("Found") }
    var selectedImages by remember { mutableStateOf<List<String>>(emptyList()) }
    var imageUri: Uri? by remember { mutableStateOf(null) }
    var fullScreenImageIndex by remember { mutableStateOf<Int?>(null) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var titleEmpty by remember { mutableStateOf(false) }
    var usernameEmpty by remember { mutableStateOf(false) }
    var phoneEmpty by remember { mutableStateOf(false) }
    var pictureEmpty by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val getPictureLauncher: ActivityResultLauncher<Intent> = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        selectedImages = handleGalleryResult(
            resultCode = result.resultCode,
            data = result.data,
            existingImages = selectedImages
        )
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            imageUri?.let { uri ->
                selectedImages = selectedImages + uri.toString()
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

    fun savePost() {
        titleEmpty= title.isEmpty()
        usernameEmpty = userName.isEmpty()
        phoneEmpty = phoneNumber.isEmpty()
        pictureEmpty = selectedImages.isEmpty()

        if (!titleEmpty && !usernameEmpty && !phoneEmpty && !pictureEmpty) {
            petFinderVM.createPost(
                title = title,
                animalType = animalType,
                race = race,
                color = color,
                userName = userName,
                phoneNumber = phoneNumber,
                description = description.text,
                postType = PostType.valueOf(postType),
                images = selectedImages
            )
            title = ""
            animalType = ""
            race = ""
            color = ""
            userName = ""
            phoneNumber = ""
            description = TextFieldValue("")
            postType = "Found"
            selectedImages = emptyList()
            imageUri = null

            titleEmpty = false
            usernameEmpty = false
            phoneEmpty = false
            pictureEmpty = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Create post", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            isError = titleEmpty,
            modifier = Modifier.fillMaxWidth()
        )
        if (titleEmpty) {
            Text("Title is required", color = Color.Red, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = animalType,
            onValueChange = { animalType = it },
            label = { Text("Type of animal") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = race,
            onValueChange = { race = it },
            label = { Text("Race") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = color,
            onValueChange = { color = it },
            label = { Text("Color") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = userName,
            onValueChange = { userName = it },
            label = { Text("Your name") },
            isError = usernameEmpty,
            modifier = Modifier.fillMaxWidth()
        )
        if (usernameEmpty) {
            Text("Name is required", color = Color.Red, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { input ->
                phoneNumber = input.filter { it.isDigit() || it == '+' || it == '-' }
            },
            label = { Text("Phone number") },
            modifier = Modifier.fillMaxWidth(),
            isError = phoneEmpty,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )
        if (phoneEmpty) {
            Text("Phone number is required", color = Color.Red, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text("Post type", style = MaterialTheme.typography.bodyLarge)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = postType == "Found",
                    onClick = { postType = "Found" }
                )
                Text("Found")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = postType == "Looking",
                    onClick = { postType = "Looking" }
                )
                Text("Looking")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(onClick = { openGallery(getPictureLauncher) }) {
                Text("Select photos")
            }

            Button(onClick = { showPermissionDialog = true }) {
                Text("Take photo")
            }

            if (pictureEmpty) {
                Text("At least one photo is required", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(selectedImages.reversed()) { uri ->
                Box(
                    modifier = Modifier.size(150.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(model = uri),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { fullScreenImageIndex = selectedImages.indexOf(uri) }
                    )

                    IconButton(
                        onClick = {
                            selectedImages = selectedImages.filter { it != uri }
                        },
                        modifier = Modifier
                            .size(26.dp)
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { savePost() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save post")
        }
    }

    fullScreenImageIndex?.let { index ->
        Dialog(onDismissRequest = { fullScreenImageIndex = null }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(model = selectedImages[index]),
                        contentDescription = "Selected Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.5f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(selectedImages.reversed()) { uri ->
                            Image(
                                painter = rememberAsyncImagePainter(model = uri),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(100.dp)
                                    .clickable {
                                        fullScreenImageIndex = selectedImages.indexOf(uri)
                                    }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(onClick = { fullScreenImageIndex = null }) {
                        Text("Close")
                    }
                }
            }
        }
    }

    /*
    fullScreenImageIndex?.let { startIndex ->
        Dialog(onDismissRequest = { fullScreenImageIndex = null }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                LazyRow(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(selectedImages) { uri ->
                        Image(
                            painter = rememberAsyncImagePainter(model = uri),
                            contentDescription = "Selected Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(0.35f)
                                .padding(8.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Button(onClick = { fullScreenImageIndex = null }) {
                        Text("Close")
                    }
                }
            }
        }
    }
     */
}