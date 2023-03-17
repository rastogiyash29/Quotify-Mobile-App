package com.example.quotify.activities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.quotify.R
import com.example.quotify.adapters.RecyclerViewCommentAdapter
import com.example.quotify.database.CommentBoxDao
import com.example.quotify.database.PostDao
import com.example.quotify.database.UserDao
import com.example.quotify.databinding.ActivityCommentsBinding
import com.example.quotify.models.Post
import com.example.quotify.models.User
import com.example.quotify.testing.RecyclerViewAdapter
import com.example.tempapp.models.Comment
import com.example.tempapp.models.CommentBox
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CommentsActivity : AppCompatActivity(), RecyclerViewCommentAdapter.AdapterCallback {
    lateinit var postId: String
    lateinit var postDao: PostDao
    lateinit var binding: ActivityCommentsBinding
    lateinit var recyclerViewCommentAdapter: RecyclerViewCommentAdapter

    lateinit var post: Post
    lateinit var commentBox: CommentBox
    lateinit var myUser: User

    lateinit var commentBoxDao: CommentBoxDao
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_comments)
        GlobalScope.launch(Dispatchers.Main) {
            initialise()
        }

        binding.postBtn.setOnClickListener {
            postComment()
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            GlobalScope.launch(Dispatchers.Main) {
                refreshComments()
            }
        }

        binding.backBtn.setOnClickListener {
            finish()
        }
    }

    private fun postComment() {
        if (binding.commentTextET.text.isNotEmpty()) {
            val comment =
                Comment(myUser, binding.commentTextET.text.toString(), System.currentTimeMillis())
            GlobalScope.launch {
                commentBoxDao.addCommentInBoxId(comment, post.commentBox).addOnSuccessListener {
                    Log.d("tag", "Comment added successfully")
                }
            }
            commentBox.list.add(0, comment)
            binding.recyclerView.adapter!!.notifyDataSetChanged()
            binding.commentTextET.text.clear()
        } else {
            Toast.makeText(this, "Enter valid non-empty Comment", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun refreshComments() {
        commentBox = CommentBoxDao().getCommentBoxById(post.commentBox).await()
            .toObject(CommentBox::class.java)!!
        commentBox.list.sortByDescending {
            it.createdAt
        }
        recyclerViewCommentAdapter.list = commentBox.list
        binding.recyclerView.adapter!!.notifyDataSetChanged()
        binding.swipeRefreshLayout.setRefreshing(false)
    }

    private suspend fun initialise() {
        postDao = PostDao()
        commentBoxDao = CommentBoxDao()
        postId = intent.extras!!.getString("postId").toString()
        GlobalScope.launch(Dispatchers.Main) {
            myUser = UserDao().getUserById(Firebase.auth.uid!!).await().toObject(User::class.java)!!
            Glide.with(this@CommentsActivity).load(myUser.imageUrl).circleCrop()
                .into(binding.profilePhoto)
        }
        post = postDao.getPostById(postId).await().toObject(Post::class.java)!!
        recyclerViewCommentAdapter = RecyclerViewCommentAdapter(emptyList(), this)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = recyclerViewCommentAdapter
        GlobalScope.launch(Dispatchers.Main) {
            refreshComments()
        }
    }

    override fun onDeleteComment(comment: Comment) {
        commentBox.list.remove(comment)
        binding.recyclerView.adapter!!.notifyDataSetChanged()
        GlobalScope.launch {
            commentBoxDao.deleteCommentFromBoxId(comment, commentBox.docId)
        }
    }
}