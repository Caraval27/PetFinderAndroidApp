package com.example.petfinderapp.presentation.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.example.petfinderapp.presentation.components.RequestCameraPermission
import com.example.petfinderapp.presentation.viewModel.PetFinderVM
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CreatePostScreen(
    petFinderVM: PetFinderVM
) {
    var title by remember { mutableStateOf("") }
    var animalType by remember { mutableStateOf("") }
    var race by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var description by remember { mutableStateOf(TextFieldValue("")) }
    var postType by remember { mutableStateOf("Found") }
    var selectedImages by remember { mutableStateOf<List<String>>(emptyList()) }
    var imageUri: Uri? by remember { mutableStateOf(null) }
    var fullScreenImageIndex by remember { mutableStateOf<Int?>(null) }

    var titleEmpty by remember { mutableStateOf(false) }
    var usernameEmpty by remember { mutableStateOf(false) }
    var phoneEmpty by remember { mutableStateOf(false) }
    var pictureEmpty by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val getPictureLauncher: ActivityResultLauncher<Intent> = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uris = result.data?.clipData
            val singleUri = result.data?.data

            selectedImages = when {
                uris != null -> {
                    selectedImages + (0 until uris.itemCount).map { i -> uris.getItemAt(i).uri.toString() }
                }
                singleUri != null -> {
                    selectedImages + listOf(singleUri.toString())
                }
                else -> selectedImages
            }

            pictureEmpty = false
        }
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

    fun openPhotoPicker() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        getPictureLauncher.launch(intent)
    }

    fun createImageFile(): File {
        val timestamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File? = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timestamp}_",
            ".jpg",
            storageDir
        )
    }

    fun openCamera(context: Context, takePictureLauncher: ActivityResultLauncher<Uri>, onImageUriCreated: (Uri) -> Unit) {
        try {
            val file = createImageFile()
            val uri = FileProvider.getUriForFile(
                context,
                "com.example.petfinderapp.provider",
                file
            )
            onImageUriCreated(uri)
            takePictureLauncher.launch(uri)
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to open camera: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera(context, takePictureLauncher) { uri ->
                imageUri = uri
            }
        } else {
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    fun savePost() {
        titleEmpty= title.isEmpty()
        usernameEmpty = username.isEmpty()
        phoneEmpty = phoneNumber.isEmpty()
        pictureEmpty = selectedImages.isEmpty()

        if (!titleEmpty && !usernameEmpty && !phoneEmpty && !pictureEmpty) {
            petFinderVM.createPost(
                title = title,
                animalType = animalType,
                race = race,
                color = color,
                username = username,
                phoneNumber = phoneNumber,
                description = description.text,
                postType = postType == "Found",
                images = selectedImages
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Create a Post", style = MaterialTheme.typography.headlineSmall)
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
            value = username,
            onValueChange = { username = it },
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

        Button(onClick = { openPhotoPicker() }) {
            Text("Select photos")
        }
        if (pictureEmpty) {
            Text("At least one picture is required", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            val permissionCheck =
                ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)

            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                openCamera(context, takePictureLauncher) { uri ->
                    imageUri = uri
                }
            } else {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }) {
            Text("Take photo")
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(selectedImages) { uri ->
                Image(
                    painter = rememberAsyncImagePainter(model = uri),
                    contentDescription = null,
                    modifier = Modifier
                        .size(150.dp)
                        .clickable { fullScreenImageIndex = selectedImages.indexOf(uri) }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { savePost() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Post")
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
                        items(selectedImages) { uri ->
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