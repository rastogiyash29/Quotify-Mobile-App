package com.example.quotify.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.example.quotify.R
import com.example.quotify.database.PostDao
import com.example.quotify.databinding.ActivityCreatePostBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CreatePostActivity : AppCompatActivity() {

    lateinit var binding: ActivityCreatePostBinding
    private var imageUri: Uri? = null
    lateinit var postDao: PostDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_post)

        postDao = PostDao()

        binding.selectedPostImage.setOnClickListener {
            selectImage()
        }

        binding.createPostBtn.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                createPost()
            }
        }
    }

    private fun selectImage() {
        val iGallery = Intent(Intent.ACTION_PICK)
        iGallery.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        launcher.launch(iGallery)
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                imageUri = result.data?.data!!
                if (imageUri != null) {
                    Glide.with(this).load(imageUri.toString()).into(binding.selectedPostImage)
                }
                Log.d("tag", "image URI -> ${imageUri}")
            }
        }

    private suspend fun createPost() {
        if (binding.postTextET.text.isEmpty() && imageUri == null) {
            Toast.makeText(this, "Atleast add some text or Image", Toast.LENGTH_SHORT).show()
        } else {
            val intent = Intent()
            if (imageUri != null) intent.putExtra("imageUri", imageUri.toString())
            else intent.putExtra("imageUri", "")
            intent.putExtra("postText", binding.postTextET.text.toString())
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    override fun onBackPressed() {
        setResult(RESULT_CANCELED, Intent())
        finish()
    }
}
