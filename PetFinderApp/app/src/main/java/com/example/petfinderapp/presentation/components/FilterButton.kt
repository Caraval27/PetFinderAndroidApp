package com.example.petfinderapp.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.petfinderapp.R
import com.example.petfinderapp.domain.Category

@Composable
fun FilterButton(
    categories: List<Category>,
    onCategorySelectionChange: (Category) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .clickable { expanded = !expanded }
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.filter_icon),
                contentDescription = "Filter",
                modifier = Modifier.size(34.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Filter",
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                    val updatedCategory = category.copy(
                        isSelected = !category.isSelected,
                        subcategories = if (!category.isSelected) {
                            category.subcategories.map {
                                it.copy(
                                    isSelected = false,
                                    subcategories = it.subcategories.map { subSubcategory -> subSubcategory.copy(isSelected = false) }
                                )
                            }
                        } else {
                            category.subcategories.map { it.copy(isSelected = false) }
                        }
                    )
                    onCategorySelectionChange(updatedCategory)
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = category.isSelected,
                onCheckedChange = { isSelected ->
                    val updatedCategory = category.copy(
                        isSelected = isSelected,
                        subcategories = if (!category.isSelected) {
                            category.subcategories.map {
                                it.copy(
                                    isSelected = false,
                                    subcategories = it.subcategories.map { subSubcategory -> subSubcategory.copy(isSelected = false) }
                                )
                            }
                        } else {
                            category.subcategories.map { it.copy(isSelected = false) }
                        }
                    )
                    onCategorySelectionChange(updatedCategory)
                }
            )
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        if (category.isSelected && category.subcategories.isNotEmpty()) {
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
                                                it.subcategories.map { subSubcategory -> subSubcategory.copy(isSelected = false) }
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
                        Checkbox(
                            checked = subcategory.isSelected,
                            onCheckedChange = { isSelected ->
                                val updatedSubcategories = category.subcategories.map {
                                    if (it.name == subcategory.name) {
                                        it.copy(
                                            isSelected = !subcategory.isSelected,
                                            subcategories = if (!subcategory.isSelected) {
                                                it.subcategories.map { subSubcategory -> subSubcategory.copy(isSelected = false) }
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

                    if (subcategory.isSelected && subcategory.subcategories.isNotEmpty()) {
                        val scrollState = rememberScrollState()
                        val indicatorHeight = 70.dp
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
                                .heightIn(max = 200.dp)
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
                                            (indicatorOffset * (180.dp.toPx() - indicatorHeight.toPx())).toDp()
                                        })
                                        .background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.extraSmall)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}