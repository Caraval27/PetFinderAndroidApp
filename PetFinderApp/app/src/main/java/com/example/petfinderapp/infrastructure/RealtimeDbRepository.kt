package com.example.petfinderapp.infrastructure

import android.util.Log
import com.example.petfinderapp.domain.Post
import com.example.petfinderapp.domain.PostType
import com.google.firebase.Firebase
import com.google.firebase.database.database
import java.io.Console

class RealtimeDbRepository {
    private val postsRef = Firebase.database.getReference("posts")

    fun insertPost(post : Post) {
        val newPostRef = postsRef.push()
        val task = newPostRef.setValue(post)
        task.addOnSuccessListener {
            Log.d("RealtimeDbRepository", "Succeeded insert post")
        }
        task.addOnFailureListener {
            Log.e("RealtimeDbRepository", "Failed insert post", it)
        }
    }

    fun selectPostsByPostType(postType : PostType) {
    }
}