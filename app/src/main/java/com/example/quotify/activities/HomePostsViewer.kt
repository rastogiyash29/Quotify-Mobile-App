package com.example.quotify.activities

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quotify.R
import com.example.quotify.adapters.RecyclerViewFirebaseAdapterHome
import com.example.quotify.database.PostDao
import com.example.quotify.databinding.ActivityHomePostsViewerBinding
import com.example.quotify.models.Post
import com.example.quotify.models.User
import com.example.quotify.view_models.ViewModelFactoryHomePosts
import com.example.quotify.view_models.ViewModelHomePosts
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class HomePostsViewer : AppCompatActivity(), RecyclerViewFirebaseAdapterHome.AdapterCallback {


    private lateinit var viewModelHomePosts: ViewModelHomePosts
    private lateinit var recyclerViewFirebaseAdapterHome: RecyclerViewFirebaseAdapterHome

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

        recyclerViewFirebaseAdapterHome =
            RecyclerViewFirebaseAdapterHome(viewModelHomePosts.postList, this,false)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = recyclerViewFirebaseAdapterHome

        GlobalScope.launch(Dispatchers.Main) {
            refreshPosts()
        }
    }

    private suspend fun refreshPosts() {
        viewModelHomePosts.refreshPosts()
        recyclerViewFirebaseAdapterHome.list = viewModelHomePosts.postList
        recyclerViewFirebaseAdapterHome.notifyDataSetChanged()
        binding.swipeRefreshLayout.setRefreshing(false)
    }

    override fun onLiked(post: Post) {
        GlobalScope.launch(Dispatchers.IO) {
            postDao.updateLikesInPost(post.docId)
        }
    }

    override fun onShare(itemView: View, post: Post) {
        val bitmap = viewToImage(itemView)
        val uri = bitmap?.let { saveImage(it) }
        uri?.let { shareImageUri(it) }
    }

    private fun shareImageUri(uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.type = "image/png"
        startActivity(intent)
    }

    private fun saveImage(image: Bitmap): Uri? {
        val imagesFolder = File(cacheDir, "images")
        var uri: Uri? = null
        try {
            imagesFolder.mkdirs()
            val file = File(imagesFolder, "shared_image.png")
            val stream = FileOutputStream(file)
            image.compress(Bitmap.CompressFormat.PNG, 90, stream)
            stream.flush()
            stream.close()
            uri = FileProvider.getUriForFile(this, "com.mydomain.fileprovider", file)
        } catch (e: IOException) {
            Log.d(TAG, "IOException while trying to write file for sharing: " + e.message)
        }
        return uri
    }

    private fun viewToImage(view: View): Bitmap? {
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        val bgDrawable = view.background
        if (bgDrawable != null) bgDrawable.draw(canvas) else canvas.drawColor(Color.WHITE)
        view.draw(canvas)
        return returnedBitmap
    }


    override fun onComment(post: Post) {
        val intent = Intent(this, CommentsActivity::class.java)
        intent.putExtra("postId", post.docId)
        startActivity(intent)
    }

    override fun onProfileClicked(user: User) {
        val intentToViewProfile: Intent
        if (user.uid.compareTo(Firebase.auth.uid!!) != 0) {
            intentToViewProfile = Intent(this, ActivityShowUserProfileOthers::class.java)
            intentToViewProfile.putExtra("id", user.uid)
        } else {
            intentToViewProfile = Intent(this, ActivityUser::class.java)
        }
        intentToViewProfile.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        startActivity(intentToViewProfile)
    }

    override fun onDeletePost(post: Post) {
        //No deletion form home
    }
}