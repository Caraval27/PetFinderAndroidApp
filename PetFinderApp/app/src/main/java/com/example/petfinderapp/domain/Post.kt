package com.example.petfinderapp.domain

import android.net.Uri

data class Post(
    val title: String,
    val animalType: String,
    val race: String,
    val color: String,
    val userName: String,
    val phoneNumber: String,
    val description: String,
    val postType: PostType,
    var images: List<String>
)

enum class PostType {
    Found, Looking
}