package com.example.petfinderapp.domain

import com.google.firebase.database.Exclude

data class Post(
    @Exclude
    var id: String = "",
    val title: String = "",
    val time: String = "",
    val animalType: String = "",
    val breed: List<String> = emptyList(),
    val color: List<String> = emptyList(),
    val userName: String = "",
    val phoneNumber: String = "",
    val description: String = "",
    val postType: PostType? = null,
    var images: List<String> = emptyList()
)

enum class PostType {
    Found, Searching
}