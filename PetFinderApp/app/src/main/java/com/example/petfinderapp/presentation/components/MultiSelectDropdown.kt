package com.example.petfinderapp.presentation.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun MultiSelectDropdown(
    text: String,
    availableOptions: List<String>,
    selectedOptions: MutableList<String>? = null,
    selectedOption: MutableState<String>? = null,
    allowMultipleOptions: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                shape = MaterialTheme.shapes.extraSmall
            ),
        shape = MaterialTheme.shapes.extraSmall,
        tonalElevation = 0.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(vertical = 16.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val displayText = when {
                    allowMultipleOptions -> {
                        if (selectedOptions.isNullOrEmpty()) text
                        else selectedOptions.joinToString(", ")
                    } else -> {
                        selectedOption?.value.takeIf { it?.isNotBlank() == true } ?: text
                    }
                }
                Text(
                    text= displayText,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(20f)
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            }

            if (expanded) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .heightIn(max = 130.dp),
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = 4.dp
                ) {
                    LazyColumn {
                        items(availableOptions) { option ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (allowMultipleOptions) {
                                            selectedOptions?.let {
                                                if (option in it) {
                                                    it.remove(option)
                                                } else {
                                                    it.add(option)
                                                }
                                            }
                                        } else {
                                            selectedOption?.value = option
                                            expanded = false
                                        }
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = if (allowMultipleOptions) {
                                        option in (selectedOptions ?: emptyList())
                                    } else {
                                        selectedOption?.value == option
                                    },
                                    onCheckedChange = {
                                        if (allowMultipleOptions) {
                                            selectedOptions?.let { list ->
                                                if (it) list.add(option) else list.remove(option)
                                            }
                                        } else {
                                            selectedOption?.value = option
                                            expanded = false
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(option, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}