package com.example.petfinderapp.domain

import java.time.LocalDate

data class Post(
    val title: String = "",
    val date: String = LocalDate.now().toString(),
    val animalType: String = "",
    val race: String = "",
    val color: String = "",
    val userName: String = "",
    val phoneNumber: String = "",
    val description: String = "",
    val postType: PostType = PostType.Looking,
    var images: List<String> = emptyList()
)

enum class PostType {
    Found, Looking
}