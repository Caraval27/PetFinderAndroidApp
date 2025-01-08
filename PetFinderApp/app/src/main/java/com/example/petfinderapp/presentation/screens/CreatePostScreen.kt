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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.navigation.NavHostController
import com.example.petfinderapp.domain.PostType
import com.example.petfinderapp.presentation.utils.Screen
import com.example.petfinderapp.presentation.components.CreatePostAddImage
import com.example.petfinderapp.presentation.components.CreatePostForm
import com.example.petfinderapp.presentation.components.FullScreenImageDialog
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
    var postType by remember { mutableStateOf(PostType.Found) }
    var selectedImages by remember { mutableStateOf<List<String>>(emptyList()) }
    var imageUri: Uri? by remember { mutableStateOf(null) }
    var fullScreenImageIndex by remember { mutableStateOf<Int?>(null) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var titleEmpty by remember { mutableStateOf(false) }
    var usernameEmpty by remember { mutableStateOf(false) }
    var phoneEmpty by remember { mutableStateOf(false) }
    var imagesEmpty by remember { mutableStateOf(false) }
    val insertSucceeded = petFinderVM.insertSucceeded.collectAsState()
    var loading by remember { mutableStateOf(false) }
    val context = LocalContext.current

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

    LaunchedEffect(insertSucceeded.value) {
        if (insertSucceeded.value == true) {

            when (postType) {
                PostType.Found -> navController.navigate(Screen.Found.route)
                PostType.Searching -> navController.navigate(Screen.Searching.route)
            }
            title = ""
            animalType.value = ""
            selectedBreeds.clear()
            selectedColors.clear()
            userName = ""
            phoneNumber = ""
            description = TextFieldValue("")
            postType = PostType.Found
            selectedImages = emptyList()
            imageUri = null

            titleEmpty = false
            usernameEmpty = false
            phoneEmpty = false
            imagesEmpty = false
            petFinderVM.setInsertSucceeded(null)
        }
        loading = false
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
            petFinderVM.createPost(
                title = title,
                animalType = animalType.value,
                breed = selectedBreeds.toList(),
                color = selectedColors.toList(),
                userName = userName,
                phoneNumber = phoneNumber,
                description = description.text,
                postType = postType,
                images = selectedImages
            )
            loading = true
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

        CreatePostAddImage(
            selectedImages = selectedImages,
            imagesEmpty = imagesEmpty,
            onImageSelected = { newImages -> selectedImages = newImages },
            getPictureLauncher = getPictureLauncher,
            onShowPermissionDialogChange = { showPermissionDialog = it },
            onFullScreenImageIndexChange = { fullScreenImageIndex = it }
        )
        Spacer(modifier = Modifier.height(16.dp))

        CreatePostForm(
            title = title,
            onTitleChange = { title = it },
            titleEmpty = titleEmpty,
            availableAnimalTypes = availableAnimalTypes,
            animalType = animalType,
            availableAnimalBreeds = availableAnimalBreeds,
            selectedBreeds = selectedBreeds,
            availableColors = availableColors,
            selectedColors = selectedColors,
            userName = userName,
            onUserNameChange = { userName = it },
            usernameEmpty = usernameEmpty,
            phoneNumber = phoneNumber,
            onPhoneNumberChange = { input -> phoneNumber = input.filter { it.isDigit() || it == '+' || it == '-' } },
            phoneEmpty = phoneEmpty,
            description = description,
            onDescriptionChange = { description = it }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Post type: ", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.width(8.dp))

                RadioButton(
                    selected = postType == PostType.Found,
                    onClick = { postType = PostType.Found }
                )
                Text(PostType.Found.toString())

                Spacer(modifier = Modifier.width(16.dp))

                RadioButton(
                    selected = postType == PostType.Searching,
                    onClick = { postType = PostType.Searching }
                )
                Text(PostType.Searching.toString())
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { savePost() },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = !loading
        ) {
            if (loading) {
                CircularProgressIndicator(
                    color = Color.LightGray,
                    trackColor = Color.DarkGray,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    text = "Save post"
                )
            }
        }
    }

    fullScreenImageIndex?.let { index ->
        FullScreenImageDialog(
            selectedImages = selectedImages,
            fullScreenImageIndex = fullScreenImageIndex!!,
            onClose = { fullScreenImageIndex = null },
            onImageSelected = { uri -> fullScreenImageIndex = uri }
        )
    }
}