package com.example.quotify.testing

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.quotify.database.PostDao
import com.example.quotify.models.Post
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*
import kotlin.collections.ArrayList

class ViewModelFirebaseMyPosts : ViewModel() {

    var postList: MutableList<Post> = ArrayList()

    private val postDao = PostDao()

    init {
        GlobalScope.launch {
            refreshPosts()
        }
    }

    suspend fun refreshPosts() {
        try {
            postList=postDao.postCollection.get().await().toObjects(Post::class.java)
            postList.sortBy {
                it.createdAt
            }
        } catch (e: Exception) {
            Log.d("tag", "Post Updation Failed ${e}")
        }
    }
}