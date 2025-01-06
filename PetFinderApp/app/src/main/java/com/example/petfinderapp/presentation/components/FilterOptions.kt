package com.example.petfinderapp.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

        SubcategoryItem(
            category = category,
            onCategorySelectionChange = onCategorySelectionChange
        )
    }
}