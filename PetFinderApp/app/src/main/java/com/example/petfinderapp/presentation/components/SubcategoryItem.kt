package com.example.petfinderapp.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.petfinderapp.domain.Category
import com.example.petfinderapp.domain.Subcategory

@Composable
fun SubcategoryItem(
    category: Category,
    onCategorySelectionChange: (Category) -> Unit
) {
    if (category.isSelected && category.subcategories.isNotEmpty()) {
        if (category.name == "Color") {
            val scrollState = rememberScrollState()
            val indicatorHeight = 40.dp
            val totalScrollRange = scrollState.maxValue

            val indicatorOffset by derivedStateOf {
                val scrollFraction = if (totalScrollRange > 0) {
                    scrollState.value.toFloat() / totalScrollRange.toFloat()
                } else 0f
                scrollFraction.coerceIn(0f, 1f)
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
                    .heightIn(max = 150.dp)
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(scrollState)
                        .padding(horizontal = 8.dp)
                ) {
                    category.subcategories.forEach { subcategory ->
                        SubcategoryRow(
                            category = category,
                            subcategory = subcategory,
                            colorMap = colorMap,
                            isColorCategory = true,
                            onCategorySelectionChange = onCategorySelectionChange
                        )
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
        } else {
            Column(modifier = Modifier.padding(start = 32.dp)) {
                category.subcategories.forEach { subcategory ->
                    SubcategoryRow(
                        category = category,
                        subcategory = subcategory,
                        colorMap = emptyMap(),
                        isColorCategory = false,
                        onCategorySelectionChange = onCategorySelectionChange
                    )
                    SubSubCategoryItem(
                        subcategory = subcategory,
                        category = category,
                        onCategorySelectionChange = onCategorySelectionChange
                    )
                }
            }
        }
    }
}

@Composable
fun SubcategoryRow(
    category: Category,
    subcategory: Subcategory,
    colorMap: Map<String, Color>,
    isColorCategory: Boolean,
    onCategorySelectionChange: (Category) -> Unit
) {
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

        if (isColorCategory) {
            Box(
                modifier = Modifier
                    .padding(end = 36.dp)
                    .size(16.dp)
                    .background(colorMap[subcategory.name] ?: Color.Transparent)
                    .border(1.dp, Color.DarkGray)
            )
        }
    }
}