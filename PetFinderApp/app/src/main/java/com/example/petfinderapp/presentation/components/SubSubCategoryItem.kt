package com.example.petfinderapp.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
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
fun SubSubCategoryItem(
    category: Category,
    subcategory: Subcategory,
    onCategorySelectionChange: (Category) -> Unit
) {
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