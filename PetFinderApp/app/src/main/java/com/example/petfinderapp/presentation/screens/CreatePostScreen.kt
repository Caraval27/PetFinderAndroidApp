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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.petfinderapp.domain.PostType
import com.example.petfinderapp.presentation.Screen
import com.example.petfinderapp.presentation.components.AddPictureToPostBox
import com.example.petfinderapp.presentation.components.MultiSelectDropdown
import com.example.petfinderapp.presentation.components.RequestCameraPermission
import com.example.petfinderapp.presentation.utils.CameraUtils.openCamera
import com.example.petfinderapp.presentation.utils.ImageUtils.handleGalleryResult
import com.example.petfinderapp.presentation.viewModel.PetFinderVM

@Composable
fun CreatePostScreen(
    petFinderVM: PetFinderVM,
    navController: NavHostController
) {
    var title by remember { mutableStateOf("") }
    val availableAnimalTypes = remember { mutableStateListOf<String>() }
    val animalType = remember { mutableStateOf("") }
    val availableAnimalBreeds = remember { mutableStateListOf<String>() }
    val selectedBreeds = remember { mutableStateListOf<String>() }
    val availableColors = remember { mutableStateListOf<String>() }
    val selectedColors = remember { mutableStateListOf<String>() }
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
    var imagesEmpty by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        availableColors.clear()
        availableColors.addAll(petFinderVM.loadColors(context))
        availableAnimalTypes.clear()
        availableAnimalTypes.addAll(petFinderVM.loadAnimalTypes(context))
    }

    LaunchedEffect(animalType.value) {
        availableAnimalBreeds.clear()
        selectedBreeds.clear()
        availableAnimalBreeds.addAll(petFinderVM.loadAnimalBreeds(context, animalType.value))
        if (animalType.value == "Dog") {
            availableAnimalBreeds.removeAt(10)
        }
    }

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
                Toast.makeText(
                    context,
                    "Camera permission is required to take photos",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    fun savePost() {
        titleEmpty = title.isEmpty()
        usernameEmpty = userName.isEmpty()
        phoneEmpty = phoneNumber.isEmpty()
        imagesEmpty = selectedImages.isEmpty()

        if (!titleEmpty && !usernameEmpty && !phoneEmpty && !imagesEmpty) {
            val postTypeValue = PostType.valueOf(postType)
            petFinderVM.createPost(
                title = title,
                animalType = animalType.value,
                breed = selectedBreeds.toList(),
                color = selectedColors.toList(),
                userName = userName,
                phoneNumber = phoneNumber,
                description = description.text,
                postType = postTypeValue,
                images = selectedImages
            )
            when (PostType.valueOf(postType)) {
                PostType.Found -> navController.navigate(Screen.Found.route)
                PostType.Looking -> navController.navigate(Screen.Looking.route)
            }
            title = ""
            animalType.value = ""
            selectedBreeds.clear()
            selectedColors.clear()
            userName = ""
            phoneNumber = ""
            description = TextFieldValue("")
            postType = "Found"
            selectedImages = emptyList()
            imageUri = null

            titleEmpty = false
            usernameEmpty = false
            phoneEmpty = false
            imagesEmpty = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Create post",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontFamily = FontFamily.Monospace
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        AddPictureToPostBox(
            selectedImages = selectedImages,
            imagesEmpty = imagesEmpty,
            onImageSelected = { newImages -> selectedImages = newImages },
            getPictureLauncher = getPictureLauncher,
            onShowPermissionDialogChange = { showPermissionDialog = it },
            onFullScreenImageIndexChange = { fullScreenImageIndex = it }
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            isError = titleEmpty,
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            )
        )
        if (titleEmpty) {
            Text(
                "Title is required",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        MultiSelectDropdown(
            text = "Type of animal",
            availableOptions = availableAnimalTypes,
            selectedOption = animalType,
            allowMultipleOptions = false
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (animalType.value.isNotEmpty()) {
            MultiSelectDropdown(
                text = animalType.value + " breed",
                availableOptions = availableAnimalBreeds,
                selectedOptions = selectedBreeds
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        MultiSelectDropdown(
            text = "Colors",
            availableOptions = availableColors,
            selectedOptions = selectedColors
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = userName,
            onValueChange = { userName = it },
            label = { Text("Your name") },
            isError = usernameEmpty,
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            )
        )
        if (usernameEmpty) {
            Text(
                "Name is required",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
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
            maxLines = 1,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            )
        )
        if (phoneEmpty) {
            Text(
                "Phone number is required",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            maxLines = Int.MAX_VALUE,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Post type: ", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.width(8.dp))

                RadioButton(
                    selected = postType == "Found",
                    onClick = { postType = "Found" }
                )
                Text("Found")

                Spacer(modifier = Modifier.width(16.dp))

                RadioButton(
                    selected = postType == "Looking",
                    onClick = { postType = "Looking" }
                )
                Text("Looking")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

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
                        contentDescription = "Selected photo",
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
                                contentScale = ContentScale.Crop,
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
}