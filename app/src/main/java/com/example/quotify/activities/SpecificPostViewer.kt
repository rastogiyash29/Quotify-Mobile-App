package com.example.quotify.activities

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quotify.R
import com.example.quotify.adapters.RecyclerViewFirebaseAdapterHome
import com.example.quotify.database.PostDao
import com.example.quotify.database.UserDao
import com.example.quotify.databinding.ActivitySpecificPostViewerBinding
import com.example.quotify.models.MyPostsContainer
import com.example.quotify.models.Post
import com.example.quotify.models.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class SpecificPostViewer : AppCompatActivity(), RecyclerViewFirebaseAdapterHome.AdapterCallback {
    private lateinit var mode: String
    private lateinit var id: String
    private var personListMode = false
    private var singlePostMode = false

    private lateinit var user: User
    private lateinit var post: Post
    private var userDao = UserDao()
    private var postDao = PostDao()

    private lateinit var binding: ActivitySpecificPostViewerBinding
    private var postList = ArrayList<Post>()

    private lateinit var recyclerViewFirebaseAdapterHome: RecyclerViewFirebaseAdapterHome
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_specific_post_viewer)

        GlobalScope.launch(Dispatchers.Main) {
            initialise()
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            GlobalScope.launch(Dispatchers.Main) {
                refreshPosts()
            }
        }

        binding.homeBtn.setOnClickListener {
            goToHomeActivity()
        }
    }

    private fun goToHomeActivity() {
        val intentToHome = Intent(this, HomePostsViewer::class.java)
        intentToHome.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        startActivity(intentToHome)
    }

    private suspend fun initialise() {
        mode = intent.extras!!.getString("mode").toString()
        id = intent.extras!!.getString("id").toString()
        if (mode.compareTo("person") == 0) {
            personListMode = true
            user = userDao.getUserById(id).await()
                .toObject(com.example.quotify.models.User::class.java)!!
        } else if (mode.compareTo("single") == 0) {
            singlePostMode = true
            post = postDao.getPostById(id).await().toObject(Post::class.java)!!
        } else finish()
        recyclerViewFirebaseAdapterHome =
            RecyclerViewFirebaseAdapterHome(
                postList,
                this,
                (personListMode && id.compareTo(Firebase.auth.uid!!) == 0)
            )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = recyclerViewFirebaseAdapterHome
        GlobalScope.launch(Dispatchers.Main) {
            refreshPosts()
        }
    }

    private suspend fun refreshPosts() {
        if (personListMode) {
            val postIdList =
                postDao.postContaninerCollection.document(user.myPostsContainer).get().await()
                    .toObject(MyPostsContainer::class.java)!!
            postList.clear()
            binding.recyclerView.adapter!!.notifyDataSetChanged()
            for (el in postIdList.myPosts) {
                val obj=postDao.postCollection.document(el).get().await().toObject(Post::class.java)
                if(obj!=null){
                    postList.add(obj)
                }
            }
            postList.sortByDescending {
                it.createdAt
            }
        } else {
            postList.add(
                postDao.postCollection.document(id).get().await().toObject(Post::class.java)!!
            )
        }
        recyclerViewFirebaseAdapterHome.list = postList
        withContext(Dispatchers.Main) {
            if (personListMode) binding.heading.text = "${user.displayName}'s Posts"
            else binding.heading.text = "Post"
            binding.recyclerView.adapter!!.notifyDataSetChanged()
            binding.swipeRefreshLayout.setRefreshing(false)
        }
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
            Log.d(
                ContentValues.TAG,
                "IOException while trying to write file for sharing: " + e.message
            )
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
        createConfirmDeletionDialog(post)
    }

    private fun createConfirmDeletionDialog(post: Post) {
        val builder = AlertDialog.Builder(this)
        //set title for alert dialog
        builder.setTitle("Delete Post")
        //set message for alert dialog
        builder.setMessage("Do you really want to delete post?")
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        //performing positive action
        builder.setPositiveButton("Yes") { dialogInterface, which ->
            Toast.makeText(applicationContext, "Deleting Post", Toast.LENGTH_LONG).show()
            GlobalScope.launch(Dispatchers.IO) {
                postDao.deletePost(post)
            }
            postList.remove(post)
            binding.recyclerView.adapter!!.notifyDataSetChanged()
        }
        //performing negative action
        builder.setNegativeButton("No") { dialogInterface, which ->
            //Do nothing
        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false)
        alertDialog.show()
    }
}