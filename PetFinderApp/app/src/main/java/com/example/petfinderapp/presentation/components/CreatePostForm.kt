package com.example.petfinderapp.presentation.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@Composable
fun CreatePostForm(
    title: String,
    onTitleChange: (String) -> Unit,
    titleEmpty: Boolean,
    availableAnimalTypes: List<String>,
    animalType: MutableState<String>,
    availableAnimalBreeds: List<String>,
    selectedBreeds: SnapshotStateList<String>,
    availableColors: List<String>,
    selectedColors: SnapshotStateList<String>,
    userName: String,
    onUserNameChange: (String) -> Unit,
    usernameEmpty: Boolean,
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    phoneEmpty: Boolean,
    description: TextFieldValue,
    onDescriptionChange: (TextFieldValue) -> Unit,
) {
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = title,
        onValueChange = onTitleChange,
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
        onValueChange = onUserNameChange,
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
        onValueChange = onPhoneNumberChange,
        label = { Text("Phone number") },
        isError = phoneEmpty,
        modifier = Modifier.fillMaxWidth(),
        maxLines = 1,
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
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
        onValueChange = onDescriptionChange,
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
}