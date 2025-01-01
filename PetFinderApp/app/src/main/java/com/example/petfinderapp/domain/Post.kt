package com.example.petfinderapp.domain

data class Post(
    val title: String,
    val animalType: String,
    val race: String,
    val color: String,
    val username: String,
    val phoneNumber: String,
    val description: String,
    val postType: PostType,
    val images: List<String>
)

enum class PostType {
    Found, Looking
}