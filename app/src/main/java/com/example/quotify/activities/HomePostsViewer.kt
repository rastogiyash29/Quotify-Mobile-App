package com.example.quotify.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quotify.R
import com.example.quotify.database.PostDao
import com.example.quotify.databinding.ActivityHomePostsViewerBinding
import com.example.quotify.models.Post
import com.example.quotify.testing.RecyclerViewAdapter
import com.example.quotify.view_models.ViewModelFactoryHomePosts
import com.example.quotify.view_models.ViewModelHomePosts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class HomePostsViewer : AppCompatActivity(), RecyclerViewAdapter.AdapterCallback {


    private lateinit var viewModelHomePosts: ViewModelHomePosts
    private lateinit var recyclerViewAdapter: RecyclerViewAdapter

    private lateinit var binding: ActivityHomePostsViewerBinding
    private lateinit var postDao: PostDao

    private var serverBusy = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home_posts_viewer)

        binding.profileBtn.setOnClickListener {
            goToActivityUser()
        }

        binding.addPostBtn.setOnClickListener {
            if (!serverBusy) {
                openCreatePostActivityForResult()
            } else
                Toast.makeText(this, "Server is busy creating another Post", Toast.LENGTH_SHORT)
                    .show()
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            GlobalScope.launch(Dispatchers.Main) {
                refreshPosts()
            }
        }

        postDao = PostDao()
        setUpRecyclerView()
    }

    //Create Post Part
    fun openCreatePostActivityForResult() {
        val intent = Intent(this, CreatePostActivity::class.java)
        resultLauncher.launch(intent)
    }

    var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent = result.data!!
                val imageUri: String = data.getStringExtra("imageUri")!!
                val postText: String = data.getStringExtra("postText")!!

                GlobalScope.launch(Dispatchers.Main) {
                    createPost(postText, imageUri)
                }
            } else {
                Toast.makeText(this, "Retured Without Creating Post", Toast.LENGTH_SHORT).show()
            }
        }

    private suspend fun createPost(postText: String, imageUri: String) {
        serverBusy = true
        Toast.makeText(this, "Creating Post", Toast.LENGTH_SHORT).show()
        try {
            postDao.createPost(postText, imageUri)
            Toast.makeText(this, "Post Creation Successful", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Post Creation failed", Toast.LENGTH_SHORT).show()
        }
        serverBusy = false
    }

    private fun goToActivityUser() {
        val intentToActivityUser = Intent(this, ActivityUser::class.java)
        intentToActivityUser.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        startActivity(intentToActivityUser)
        finish()
    }

    private fun setUpRecyclerView() {
        viewModelHomePosts =
            ViewModelProvider(
                this,
                ViewModelFactoryHomePosts(
                )
            ).get(
                ViewModelHomePosts::class.java
            )

        recyclerViewAdapter = RecyclerViewAdapter(viewModelHomePosts.postList, this)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = recyclerViewAdapter

        GlobalScope.launch(Dispatchers.Main) {
            refreshPosts()
        }
    }

    private suspend fun refreshPosts() {
        viewModelHomePosts.refreshPosts()
        recyclerViewAdapter.list = viewModelHomePosts.postList
        recyclerViewAdapter.notifyDataSetChanged()
        binding.swipeRefreshLayout.setRefreshing(false)
    }

    override fun onLiked(post: Post) {
        GlobalScope.launch(Dispatchers.IO) {
            postDao.updateLikesInPost(post.docId)
        }
    }

    override fun onShare(post: Post) {
        Toast.makeText(this, "Share functionality is yet to be implemented", Toast.LENGTH_SHORT)
            .show()
    }

    override fun onComment(post: Post) {
        val intent = Intent(this, CommentsActivity::class.java)
        intent.putExtra("postId", post.docId)
        startActivity(intent)
    }
}