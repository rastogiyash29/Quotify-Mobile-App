package com.example.quotify.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quotify.R
import com.example.quotify.adapters.PostAdapter
import com.example.quotify.database.IPostInterface
import com.example.quotify.database.PostDao
import com.example.quotify.databinding.ActivityHomePostsViewerBinding
import com.example.quotify.models.Post
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class HomePostsViewer : AppCompatActivity(), IPostInterface {

    private lateinit var adapter: PostAdapter
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

        postDao = PostDao()
        setUpRecyclerView()
        adapter.startListening()
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
        val postCollection = postDao.postCollection
        val query = postCollection.orderBy("text", Query.Direction.DESCENDING)
        val recyclerViewOptions =
            FirestoreRecyclerOptions.Builder<Post>().setQuery(query, Post::class.java).build()
        adapter = PostAdapter(recyclerViewOptions,this)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

//    override fun onStart() {
//        super.onStart()
//        adapter.startListening()
//    }
//
//    override fun onStop() {
//        super.onStop()
//        adapter.stopListening()
//    }

    override fun onLikeClicked(postId: String) {
        GlobalScope.launch {
            postDao.updateLikesInPost(postId)
        }
    }
}