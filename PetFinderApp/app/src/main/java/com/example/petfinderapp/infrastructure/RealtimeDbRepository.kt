package com.example.petfinderapp.infrastructure

import android.util.Log
import com.example.petfinderapp.domain.Post
import com.example.petfinderapp.domain.PostType
import com.google.firebase.Firebase
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDateTime

class RealtimeDbRepository {
    private val postsRef = Firebase.database.getReference("posts")
    private val postFeedListener : ChildEventListener = createPostFeedListener()
    private val postDetailsListener : ValueEventListener = createPostDetailsListener()
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>>
        get() = _posts
    private val _post = MutableStateFlow(Post())
    val post: StateFlow<Post>
        get() = _post
    private val _insertSucceeded = MutableStateFlow<Boolean?>(null)
    val insertSucceeded: StateFlow<Boolean?>
        get() = _insertSucceeded

    init {
        postsRef.keepSynced(true)
    }

    fun insertPost(post : Post) {
        _insertSucceeded.value = null
        val newPostRef = postsRef.push()
        val task = newPostRef.setValue(post)
        task.addOnSuccessListener {
            _insertSucceeded.value = true
        }
        task.addOnFailureListener {
            _insertSucceeded.value = false
        }
    }

    private fun createPostFeedListener() : ChildEventListener {
        return object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                val post = dataSnapshot.getValue(Post::class.java)
                val postId = dataSnapshot.key
                if (post != null && postId != null) {
                    post.id = dataSnapshot.key!!
                    val newPosts = _posts.value + post
                    _posts.value = newPosts.sortedByDescending { LocalDateTime.parse(it.time) }
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

    private fun createPostDetailsListener() : ValueEventListener {
        return object  : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val post = snapshot.getValue(Post::class.java)
                val postId = snapshot.key
                if (post != null && postId != null) {
                    post.id = postId
                    _post.value = post
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        }
    }

    fun addPostFeedListener(postType : PostType) {
        _posts.value = emptyList()
        val postTypeQuery = postsRef.orderByChild("postType").equalTo(postType.toString())
        postTypeQuery.addChildEventListener(postFeedListener)
    }

    fun removePostFeedListener(postType: PostType) {
        val postTypeQuery = postsRef.orderByChild("postType").equalTo(postType.toString())
        postTypeQuery.removeEventListener(postFeedListener)
    }

    fun addPostDetailsListener(postId: String) {
        _post.value = Post()
        val postIdQuery = postsRef.child(postId)
        postIdQuery.addListenerForSingleValueEvent(postDetailsListener)
    }
}