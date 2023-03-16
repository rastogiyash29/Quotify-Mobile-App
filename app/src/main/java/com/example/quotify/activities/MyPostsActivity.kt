package com.example.quotify.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.quotify.R
import com.example.quotify.adapters.PostAdapter
import com.example.quotify.database.PostDao
import com.example.quotify.databinding.ActivityMyPostsBinding
import com.example.quotify.models.Post
import com.example.quotify.testing.RecyclerViewAdapter
import com.example.quotify.testing.ViewModelFactoryFirebase
import com.example.quotify.testing.ViewModelFirebaseMyPosts
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MyPostsActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener,
    RecyclerViewAdapter.AdapterCallback {

    private lateinit var adapter: PostAdapter
    private lateinit var binding: ActivityMyPostsBinding
    private lateinit var postDao: PostDao
    private lateinit var viewModel: ViewModelFirebaseMyPosts
    private lateinit var recyclerViewAdapter: RecyclerViewAdapter
    private var serverBusy = false

    private val authUid = Firebase.auth.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_my_posts)

        postDao = PostDao()

        viewModel =
            ViewModelProvider(
                this,
                ViewModelFactoryFirebase(
                )
            ).get(
                ViewModelFirebaseMyPosts::class.java
            )

        recyclerViewAdapter = RecyclerViewAdapter(viewModel.postList, this)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = recyclerViewAdapter
        GlobalScope.launch(Dispatchers.Main) {
            refreshPosts()
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            GlobalScope.launch(Dispatchers.Main) {
                refreshPosts()
            }
        }
    }

    private suspend fun refreshPosts() {
        viewModel.refreshPosts()
        recyclerViewAdapter.list = viewModel.postList
        recyclerViewAdapter.notifyDataSetChanged()
        binding.swipeRefreshLayout.setRefreshing(false)
    }

    override fun onRefresh() {
        Log.d("tag", "Refreshed")
    }

    override fun onLiked(post: Post) {
        GlobalScope.launch(Dispatchers.IO) {
            postDao.updateLikesInPost(post.docId)
        }
    }

    override fun onShare(postId: Post) {
        Toast.makeText(this, "Share functionality is yet to be implemented", Toast.LENGTH_SHORT)
            .show()
    }

    override fun onComment(postId: Post) {
        val intent = Intent(this, CommentsActivity::class.java)
        intent.putExtra("postId", postId.docId)
        startActivity(intent)
    }
}