package com.example.petfinderapp.domain

data class Category(
    val name: String,
    val isSelected: Boolean = false,
    val subcategories: List<Subcategory> = emptyList()
)

data class Subcategory(
    val name: String,
    val isSelected: Boolean = false
)