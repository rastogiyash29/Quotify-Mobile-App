package com.example.quotify.view_models
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.quotify.database.PostDao
import com.example.quotify.models.Post
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.collections.ArrayList

class ViewModelHomePosts : ViewModel() {

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
            postList.sortByDescending {
                it.createdAt
            }
        } catch (e: Exception) {
            Log.d("tag", "Post Updation Failed ${e}")
        }
    }
}