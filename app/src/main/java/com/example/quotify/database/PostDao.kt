package com.example.quotify.database

import android.net.Uri
import android.util.Log
import com.example.quotify.models.MyPostsContainer
import com.example.quotify.models.Post
import com.example.quotify.models.User
import com.example.tempapp.models.CommentBox
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PostDao {

    val db = FirebaseFirestore.getInstance()
    val postCollections = db.collection("allPosts")
    val auth = Firebase.auth
    val storage = FirebaseStorage.getInstance()
    val commentBoxDao: CommentBoxDao = CommentBoxDao()
    val postContaninerCollection = db.collection("postContainerCollection")

    suspend fun createPost(text: String, imageUri: String) {
        var userTaskSuccess = true
        var imageTaskSuccess = true
        var commentBoxTaskSuccess = true
        var postUploadTaskSuccess = true

        var postId = ""
        var user: User? = null
        val jobUser = GlobalScope.launch(Dispatchers.IO) {
            try {
                user = UserDao().getUserById(auth.uid!!).await().toObject(User::class.java)!!
                Log.d("tag", "Fetched User Successfully ${user!!.uid}")
            } catch (e: Exception) {
                userTaskSuccess = false
                Log.d("tag", "Fetch User Failed")
                throw e
            }
        }

        var imageUrl = ""
        val imageName = "images/${auth.uid}_${System.currentTimeMillis()}"
        val jobImgUrl = GlobalScope.launch(Dispatchers.IO) {
            if (imageUri.isNotEmpty()) {
                var added = false
                val jobImgUpload = GlobalScope.launch {
                    try {
                        storage.getReference(imageName).putFile(Uri.parse(imageUri)).await()
                        added = true
                        Log.d("tag", "Uploaded Image successfully")
                    } catch (e: Exception) {
                        imageTaskSuccess = false
                        Log.d("tag", "Upload Image Failed")
                        throw e
                    }
                }
                jobImgUpload.join()
                if (added) {
                    try {
                        imageUrl = storage.reference.child(imageName).downloadUrl.await().toString()
                        Log.d("tag", "Image URL successfully")
                    } catch (e: Exception) {
                        imageTaskSuccess = false
                        Log.d("tag", "Image URL Failed")
                        throw e
                    }
                }
            }
        }

        var commentBoxId: String = ""
        val jobNewCommentBox = GlobalScope.launch(Dispatchers.IO) {
            try {
                val commentBox = commentBoxDao.getNewCommentBox(CommentBox(ArrayList())).await()
                commentBoxId = commentBox.id
                Log.d("tag", "CommentBox Creation Success")
            } catch (e: Exception) {
                commentBoxTaskSuccess = false
                Log.d("tag", "CommentBox Creation Failed")
                throw e
            }
        }

        jobUser.join()
        jobImgUrl.join()
        jobNewCommentBox.join()
        if (!userTaskSuccess || !imageTaskSuccess || !commentBoxTaskSuccess) {
            return
        }

        //Creating and uploading image to Firebase server
        val jobUploadPost = GlobalScope.launch {
            val post =
                Post(text, imageUrl, user!!, ArrayList(), commentBoxId, System.currentTimeMillis())
            try {
                postId = postCollections.add(post).await().id
                Log.d("tag", "Post Upload Successful ${postId}")
            } catch (e: Exception) {
                Log.d("tag", "Post Upload Failed ")
                postUploadTaskSuccess = false
                throw e
            }
        }

        jobUploadPost.join()
        if (!postUploadTaskSuccess) return

        try {
            postContaninerCollection.document(user!!.myPostsContainer)
                .update("myPosts", FieldValue.arrayUnion(postId)).await()
            Log.d("tag", "AddPost In User Successfully ******")
        } catch (e: Exception) {
            Log.d("tag", "AddPost In User Failed ******")
            throw e
        }
    }

    fun getNewPostContainer(): Task<DocumentReference> {
        return postContaninerCollection.add(MyPostsContainer())
    }

    private fun addInPostContainer(containerId: String, postId: String): Task<Void> {
        return postContaninerCollection.document(containerId)
            .update("myPosts", FieldValue.arrayUnion(postId))
    }

}