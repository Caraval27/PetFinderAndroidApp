package com.example.petfinderapp.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.petfinderapp.domain.Category

@Composable
fun FilterOptions(
    expanded: Boolean,
    categories: List<Category>,
    onCategorySelectionChange: (Category) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        if (expanded) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 2.dp
                ) {
                    LazyColumn(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { category ->
                            CategoryItem(
                                category = category,
                                onCategorySelectionChange = onCategorySelectionChange
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryItem(
    category: Category,
    onCategorySelectionChange: (Category) -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val updatedCategory = category.copy(isSelected = !category.isSelected)
                    onCategorySelectionChange(updatedCategory)
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .padding(16.dp)
                    .weight(1f)
            )
            Icon(
                imageVector = if (category.isSelected) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (category.isSelected) "Collapse" else "Expand",
                modifier = Modifier.padding(end = 16.dp)
            )
        }

        if (category.isSelected && category.subcategories.isNotEmpty()) {
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

            Column(modifier = Modifier.padding(start = 32.dp)) {
                category.subcategories.forEach { subcategory ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val updatedSubcategories = category.subcategories.map {
                                    if (it.name == subcategory.name) {
                                        it.copy(
                                            isSelected = !subcategory.isSelected,
                                            subcategories = if (!subcategory.isSelected) {
                                                it.subcategories.map { subSubcategory ->
                                                    subSubcategory.copy(
                                                        isSelected = false
                                                    )
                                                }
                                            } else {
                                                it.subcategories
                                            }
                                        )
                                    } else {
                                        it
                                    }
                                }
                                onCategorySelectionChange(category.copy(subcategories = updatedSubcategories))
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Checkbox(
                                checked = subcategory.isSelected,
                                onCheckedChange = { isSelected ->
                                    val updatedSubcategories = category.subcategories.map {
                                        if (it.name == subcategory.name) {
                                            it.copy(
                                                isSelected = !subcategory.isSelected,
                                                subcategories = if (!subcategory.isSelected) {
                                                    it.subcategories.map { subSubcategory ->
                                                        subSubcategory.copy(
                                                            isSelected = false
                                                        )
                                                    }
                                                } else {
                                                    it.subcategories
                                                }
                                            )
                                        } else {
                                            it
                                        }
                                    }
                                    onCategorySelectionChange(category.copy(subcategories = updatedSubcategories))
                                }
                            )
                            Text(
                                text = subcategory.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.DarkGray
                            )
                        }

                        if (category.name == "Color") {
                            Box(
                                modifier = Modifier
                                    .padding(end = 36.dp)
                                    .size(16.dp)
                                    .background(colorMap[subcategory.name] ?: Color.Transparent)
                                    .border(1.dp, Color.DarkGray)
                            )
                        }
                    }

                    if (subcategory.isSelected && subcategory.subcategories.isNotEmpty()) {
                        val scrollState = rememberScrollState()
                        val indicatorHeight = 40.dp
                        val totalScrollRange = scrollState.maxValue

                        val indicatorOffset by derivedStateOf {
                            val scrollFraction = if (totalScrollRange > 0) {
                                scrollState.value.toFloat() / totalScrollRange.toFloat()
                            } else 0f
                            scrollFraction.coerceIn(0f, 1f)
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 150.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .verticalScroll(scrollState)
                                    .padding(horizontal = 8.dp)
                            ) {
                                subcategory.subcategories.forEach { subSubcategory ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                val updatedSubSubcategories =
                                                    subcategory.subcategories.map {
                                                        if (it.name == subSubcategory.name) {
                                                            it.copy(isSelected = !it.isSelected)
                                                        } else {
                                                            it
                                                        }
                                                    }
                                                val updatedSubcategories =
                                                    category.subcategories.map {
                                                        if (it.name == subcategory.name) {
                                                            it.copy(subcategories = updatedSubSubcategories)
                                                        } else {
                                                            it
                                                        }
                                                    }
                                                onCategorySelectionChange(
                                                    category.copy(
                                                        subcategories = updatedSubcategories
                                                    )
                                                )
                                            },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = subSubcategory.isSelected,
                                            onCheckedChange = { isSelected ->
                                                val updatedSubSubcategories =
                                                    subcategory.subcategories.map {
                                                        if (it.name == subSubcategory.name) {
                                                            it.copy(isSelected = isSelected)
                                                        } else {
                                                            it
                                                        }
                                                    }
                                                val updatedSubcategories =
                                                    category.subcategories.map {
                                                        if (it.name == subcategory.name) {
                                                            it.copy(subcategories = updatedSubSubcategories)
                                                        } else {
                                                            it
                                                        }
                                                    }
                                                onCategorySelectionChange(
                                                    category.copy(
                                                        subcategories = updatedSubcategories
                                                    )
                                                )
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = subSubcategory.name,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.DarkGray
                                        )
                                    }
                                }
                            }

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
                                            (indicatorOffset * (120.dp.toPx() - indicatorHeight.toPx())).toDp()
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