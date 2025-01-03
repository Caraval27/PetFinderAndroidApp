package com.example.petfinderapp.infrastructure

import android.util.Log
import com.example.petfinderapp.domain.Post
import com.example.petfinderapp.domain.PostType
import com.google.firebase.Firebase
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.database
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.Console
import java.time.LocalDate

class RealtimeDbRepository {
    private val postsRef = Firebase.database.getReference("posts")
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    private val postListener : ChildEventListener = createPostListener()
    val posts: StateFlow<List<Post>>
        get() = _posts

    fun insertPost(post : Post) {
        val newPostRef = postsRef.push()
        val task = newPostRef.setValue(post)
        task.addOnSuccessListener {
            Log.d("RealtimeDbRepository", "Succeeded to insert post")
        }
        task.addOnFailureListener {
            Log.e("RealtimeDbRepository", "Failed to insert post", it)
        }
    }

    private fun createPostListener() : ChildEventListener {
        return object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                val post = dataSnapshot.getValue(Post::class.java)
                if (post != null) {
                    val newPosts = _posts.value + post
                    _posts.value = newPosts.sortedByDescending { LocalDate.parse(it.date) }
                    Log.d("RealtimeDbRepository","Post fetched: " + post.title)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {}
        }
    }

    fun addPostListener(postType : PostType) {
        _posts.value = emptyList()
        val postTypeQuery = postsRef.orderByChild("postType").equalTo(postType.toString())
        postTypeQuery.addChildEventListener(postListener)
    }

    fun removePostListener(postType: PostType) {
        val postTypeQuery = postsRef.orderByChild("postType").equalTo(postType.toString())
        postTypeQuery.removeEventListener(postListener)
    }
}