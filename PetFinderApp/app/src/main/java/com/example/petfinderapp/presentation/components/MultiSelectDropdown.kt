package com.example.petfinderapp.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
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
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    val indicatorHeight = 40.dp

    val indicatorOffset by remember {
        derivedStateOf {
            val totalScrollRange = scrollState.maxValue.toFloat()
            val scrollFraction = if (totalScrollRange > 0) {
                scrollState.value / totalScrollRange
            } else 0f
            scrollFraction.coerceIn(0f, 1f)
        }
    }

    val isOptionSelected = when {
        allowMultipleOptions -> selectedOptions?.isNotEmpty() == true
        else -> !selectedOption?.value.isNullOrEmpty()
    }

    val colorMap = mapOf(
        "Black" to Color.Black,
        "Brown" to Color(0xFF8B4513),
        "Cream" to Color(0xFFFFFDD0),
        "Fawn" to Color(0xFFE5AA70),
        "Gray" to Color.Gray,
        "Orange" to Color(0xFFFFA500),
        "Red" to Color(0xFFB55239),
        "White" to Color.White
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    shape = MaterialTheme.shapes.extraSmall
                ),
            shape = MaterialTheme.shapes.extraSmall,
            tonalElevation = 0.dp
        ) {
            Column(modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp)){
                if (isOptionSelected || expanded) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary),
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            expanded = !expanded
                            focusManager.clearFocus()
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val displayText = when {
                        allowMultipleOptions -> {
                            if (selectedOptions.isNullOrEmpty()) text
                            else selectedOptions.joinToString(", ")
                        }
                        else -> {
                            selectedOption?.value.takeIf { it?.isNotBlank() == true } ?: text
                        }
                    }
                    Text(
                        text = displayText,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
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
                Spacer(modifier = Modifier.height(2.dp))

                if (expanded) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 130.dp),
                    ) {
                        Column(
                            modifier = Modifier
                                .verticalScroll(scrollState)
                        )  {
                            availableOptions.forEach { option ->
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
                                        },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
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

                                    if (text == "Colors") {
                                        Box(
                                            modifier = Modifier
                                                .padding(end = 32.dp)
                                                .size(16.dp)
                                                .background(colorMap[option] ?: Color.Transparent)
                                                .border(1.dp, Color.DarkGray)
                                        )
                                    }
                                }
                            }
                        }

                        if (scrollState.maxValue > 0) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .fillMaxHeight()
                                    .align(Alignment.CenterEnd)
                                    .padding(vertical = 16.dp)
                                    .background(Color.LightGray.copy(alpha = 0.4f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height(indicatorHeight)
                                        .offset(y = with(LocalDensity.current) {
                                            (indicatorOffset * (100.dp.toPx() - indicatorHeight.toPx())).toDp()
                                        })
                                        .background(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.shapes.extraSmall
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}