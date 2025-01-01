package com.example.petfinderapp.infrastructure

import com.example.petfinderapp.domain.Post
import com.google.firebase.Firebase
import com.google.firebase.database.database

class FirebaseRepository {
    private val postRef = Firebase.database.getReference("post")

    fun insertPost(post : Post) {

    }
}