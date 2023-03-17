package com.example.quotify.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.example.quotify.R
import com.example.quotify.adapters.RecyclerViewFirebaseAdapterHome
import com.example.quotify.database.PostDao
import com.example.quotify.database.UserDao
import com.example.quotify.databinding.ActivityShowUserProfileOthersBinding
import com.example.quotify.databinding.ActivitySpecificPostViewerBinding
import com.example.quotify.models.Post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ActivityShowUserProfileOthers : AppCompatActivity() {
    private lateinit var user: com.example.quotify.models.User
    private var userDao = UserDao()

    private lateinit var id: String

    private lateinit var binding: ActivityShowUserProfileOthersBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_show_user_profile_others)

        GlobalScope.launch(Dispatchers.Main) {
            initialise()
        }

        binding.homeBtn.setOnClickListener {
            goToHomeActivity()
        }

        binding.showPostsBtn.setOnClickListener {
            showThisUserPosts()
        }
    }

    private fun showThisUserPosts() {
        val intentToMyPosts = Intent(this, SpecificPostViewer::class.java)
        intentToMyPosts.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        intentToMyPosts.putExtra("mode", "person")
        intentToMyPosts.putExtra("id", "${user.uid}")
        startActivity(intentToMyPosts)
    }

    private fun goToHomeActivity() {
        val intentToHome = Intent(this, HomePostsViewer::class.java)
        intentToHome.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        startActivity(intentToHome)
        finish()
    }

    private suspend fun initialise() {
        id = intent.extras!!.getString("id").toString()
        if (id.isEmpty()) finish()
        user = userDao.getUserById(id).await()
            .toObject(com.example.quotify.models.User::class.java)!!
        Glide.with(this).load(user.imageUrl).into(binding.profilePhoto)
        binding.displayName.text = user.displayName
        binding.userNameTV.text = user.userName
    }
}