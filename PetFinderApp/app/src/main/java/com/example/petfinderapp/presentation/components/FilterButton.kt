package com.example.petfinderapp.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
                    val updatedSubcategories = if (!category.isSelected) {
                        category.subcategories.map { it.copy(isSelected = false) }
                    } else {
                        category.subcategories
                    }

                    onCategorySelectionChange(
                        category.copy(
                            isSelected = !category.isSelected,
                            subcategories = updatedSubcategories
                        )
                    )
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = category.isSelected,
                onCheckedChange = { isSelected ->
                    val updatedSubcategories = if (!isSelected) {
                        category.subcategories.map { it.copy(isSelected = false) }
                    } else {
                        category.subcategories
                    }

                    onCategorySelectionChange(
                        category.copy(
                            isSelected = isSelected,
                            subcategories = updatedSubcategories
                        )
                    )
                }
            )
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        if (category.isSelected) {
            Column(modifier = Modifier.padding(start = 32.dp)) {
                category.subcategories.forEach { subcategory ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val updatedSubcategories = category.subcategories.map {
                                    if (it.name == subcategory.name) it.copy(isSelected = !subcategory.isSelected) else it
                                }
                                onCategorySelectionChange(category.copy(subcategories = updatedSubcategories))
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = subcategory.isSelected,
                            onCheckedChange = { isSelected ->
                                val updatedSubcategories = category.subcategories.map {
                                    if (it.name == subcategory.name) it.copy(isSelected = isSelected) else it
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
                }
            }
        }
    }
}