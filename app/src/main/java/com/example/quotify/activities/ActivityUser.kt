package com.example.quotify.activities

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.example.quotify.R
import com.example.quotify.database.PostDao
import com.example.quotify.database.QuoteBookDatabase
import com.example.quotify.database.UserDao
import com.example.quotify.databinding.ActivityUserBinding
import com.example.quotify.models.User
import com.example.quotify.models.UserName
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class ActivityUser : AppCompatActivity() {
    lateinit var quoteBookDatabase: QuoteBookDatabase
    lateinit var postDao: PostDao

    //Here its guarantee that firebase auth will be Non-Null
    lateinit var auth: FirebaseAuth
    lateinit var binding: ActivityUserBinding

    private var userExists = false
    private var serverBusy = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_user)

        postDao = PostDao()

        quoteBookDatabase = QuoteBookDatabase
        auth = Firebase.auth

        binding.logOutBtn.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.selectUserNameBtn.setOnClickListener {
            checkUserExistence(auth.currentUser!!)
        }

        binding.menu.setOnClickListener {
            if (userExists)
                openMenuDialog()
            else
                Toast.makeText(this, "Firstly create userName", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        checkUserExistence(auth.currentUser!!)
    }

    private fun checkUserExistence(firebaseUser: FirebaseUser) {
        blockInput()
        val userDao = quoteBookDatabase.getUserDao()
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val userTask = userDao.getUserById(firebaseUser.uid).await()
                if (userTask.exists()) {
                    //User Already Exist //Resume all functionalities here
                    Log.d("tag", "Found User exists")
                    userExists = true
                    setUI()
                } else {
                    startUserNameSelectionDialog()
                }
            } catch (e: Exception) {
                Log.d("tag", "Checking Existing User Task Failed")
                unblockInput()
            }
        }
    }

    private fun startUserNameSelectionDialog() {
        unblockInput()
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.unique_username_selection_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val fillUserNameET = dialog.findViewById<EditText>(R.id.fillUserName)
        val createUserNameBtn = dialog.findViewById<Button>(R.id.createUserNameBtn)
        val statusTV = dialog.findViewById<TextView>(R.id.usernameStatus)

        createUserNameBtn.setOnClickListener {
            //Here we will check if some userName is available or not
            blockInput()
            if (fillUserNameET.text.isNotEmpty()) {
                //checking username availability
                quoteBookDatabase.getUserNameDao().getUidByUserName(fillUserNameET.text.toString())
                    .addOnCompleteListener(this) {
                        if (it.isSuccessful) {
                            if (it.result.exists()) {
                                statusTV.text = "UserName Already exists"
                                Log.d("tag", "UserName Already Exists")
                                unblockInput()
                            } else {
                                GlobalScope.launch(Dispatchers.Main) {
                                    addNewUser(fillUserNameET.text.toString())
                                }
                                Log.d("tag", "UserName doesNot Exists")
                                dialog.dismiss()
                            }
                        }
                    }
            } else {
                Toast.makeText(this, "Enter a valid user name", Toast.LENGTH_SHORT).show()
                unblockInput()
            }
        }
        unblockInput()
        dialog.show()
    }

    private suspend fun addNewUser(userName: String) {
        var myPostsContainer: String
        try {
            val newPostsContainerTask = postDao.getNewPostContainer().await()
            myPostsContainer = newPostsContainerTask.id
        } catch (e: Exception) {
            binding.displayName.text = "Try Again Creating Unique USERNAME !!"
            Log.d("tag", "New myPostsContainer Creation failed")
            return
        }

        val firebaseUser = Firebase.auth.currentUser
        val currUser = User(
            firebaseUser!!.uid,
            firebaseUser.displayName,
            firebaseUser.photoUrl.toString(),
            userName,
            myPostsContainer
        )
        QuoteBookDatabase.getUserNameDao()
            .addUserNameWithUid(UserName(userName, firebaseUser.uid))
            .addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    QuoteBookDatabase.getUserDao().addUser(currUser).addOnCompleteListener(this) {
                        if (it.isSuccessful) {
                            setUI()
                            userExists = true
                        } else {
                            Toast.makeText(
                                this,
                                "UserName Added But user Adding Failed",
                                Toast.LENGTH_SHORT
                            ).show()
                            unblockInput()
                        }
                    }
                } else {
                    Log.d("tag", "UserName Addition Failed")
                    unblockInput()
                }
            }
    }

    private fun setUI() {
        blockInput()
        binding.selectUserNameBtn.visibility = View.GONE
        quoteBookDatabase.getUserDao().getUserById(auth.currentUser!!.uid)
            .addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    val user = it.result.toObject(User::class.java)!!
                    Glide.with(this)
                        .load(user.imageUrl)
                        .into(binding.profilePhoto)
                    binding.displayName.text = user.displayName
                    binding.userNameTV.text = user.userName
                    binding.emailTV.text = auth.currentUser?.email

                    //setting movement scroll of textViews
                    binding.emailTV.movementMethod = ScrollingMovementMethod()
                    binding.displayName.movementMethod = ScrollingMovementMethod()
                    binding.userNameTV.movementMethod = ScrollingMovementMethod()
                } else {
                    Toast.makeText(
                        this,
                        "Some Internal Connection Error Occured",
                        Toast.LENGTH_LONG
                    ).show()
                }
                unblockInput()
            }
    }

    private fun openMenuDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.activity_user_menu_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()

        val homeBtn = dialog.findViewById<Button>(R.id.homeBtn)
        val createPostBtn = dialog.findViewById<Button>(R.id.createPostBtn)
        val myPostsBtn = dialog.findViewById<Button>(R.id.myPostsBtn)
        val quotifyModeBtn=dialog.findViewById<Button>(R.id.quotifyMode)

        homeBtn.setOnClickListener {
            val intentToHome = Intent(this, HomePostsViewer::class.java)
            intentToHome.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intentToHome)
            dialog.dismiss()
        }

        createPostBtn.setOnClickListener {
            if (!serverBusy) {
                openCreatePostActivityForResult()
                dialog.dismiss()
            } else
                Toast.makeText(this, "Server is busy creating another Post", Toast.LENGTH_SHORT)
                    .show()
        }

        myPostsBtn.setOnClickListener {
            val intentToMyPosts = Intent(this, MyPostsActivity::class.java)
            intentToMyPosts.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intentToMyPosts)
            dialog.dismiss()
        }

        quotifyModeBtn.setOnClickListener {
            val intentToQuotify = Intent(this, OnlineQuotesActivity::class.java)
            intentToQuotify.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intentToQuotify)
            dialog.dismiss()
        }
    }

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

    //Disable and Enable Functions of UserInterface
    private fun AppCompatActivity.blockInput() {
        binding.progressBar.visibility = View.VISIBLE
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }

    private fun AppCompatActivity.unblockInput() {
        binding.progressBar.visibility = View.GONE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }
}