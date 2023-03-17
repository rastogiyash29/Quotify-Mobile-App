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
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class PostDao {

    val db = FirebaseFirestore.getInstance()
    val postCollection = db.collection("allPosts")
    val auth = Firebase.auth
    val storage = FirebaseStorage.getInstance()
    val commentBoxDao: CommentBoxDao = CommentBoxDao()
    val postContaninerCollection = db.collection("postContainerCollection")

    suspend fun createPost(text: String, imageUri: String) {
        var userTaskSuccess = true
        var imageTaskSuccess = true
        var commentBoxTaskSuccess = true
        var postUploadTaskSuccess = true

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

        var commentBoxRef = commentBoxDao.commentBoxCollections.document()
        val commentBox = CommentBox(commentBoxRef.id, ArrayList())
        val jobNewCommentBox = GlobalScope.launch(Dispatchers.IO) {
            try {
                commentBoxRef.set(commentBox).await()
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

        //Getting new document refrence
        val newPostDocRef = postCollection.document()

        //Creating and uploading image to Firebase server
        val post =
            Post(
                text,
                imageUrl,
                user!!,
                ArrayList(),
                commentBoxRef.id,
                System.currentTimeMillis(),
                newPostDocRef.id
            )
        val jobUploadPost = GlobalScope.launch {
            try {
                newPostDocRef.set(post).await()
                Log.d("tag", "Post Upload Successful ${newPostDocRef.id}")
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
                .update("myPosts", FieldValue.arrayUnion(post.docId)).await()
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

    suspend fun updateLikesInPost(postId: String) {
        var PostTaskSuccess = true

        var post = Post()
        val jobGetPost = GlobalScope.launch(Dispatchers.IO) {
            try {
                post = getPostById(postId).await().toObject(Post::class.java)!!
            } catch (e: Exception) {
                PostTaskSuccess = false
                Log.d("tag", "Post Fetch Failed")
            }
        }

        jobGetPost.join()
        if (!PostTaskSuccess) {
            return
        }

        try {
            if (post.likedBy.contains(auth.uid)) {
                postCollection.document(postId).update("likedBy", FieldValue.arrayRemove(auth.uid))
                    .await()
            } else {
                postCollection.document(postId)
                    .update("likedBy", FieldValue.arrayUnion(auth.uid)).await()
            }
            Log.d("tag", "PostUpdate Success")
        } catch (e: Exception) {
            Log.d("tag", "PostUpdate Failed")
        }
    }

    //return task of document with particular id
    fun getPostById(id: String): Task<DocumentSnapshot> {
        return postCollection.document(id).get()
    }

    suspend fun deletePost(post: Post) {
        val job = GlobalScope.launch {
            async {
                try {
                    postCollection.document(post.docId).delete().await()
                } catch (e: Exception) {
                    Log.d("tag", "Post deleted Failed")
                }
            }
            async {
                try {
                    commentBoxDao.commentBoxCollections.document(post.commentBox).delete().await()
                } catch (e: Exception) {
                    Log.d("tag", "Post commentBox deleted Failed")
                }
            }
            async {
                try {
                    val ref = storage.getReferenceFromUrl(post.imageUrl)
                    ref.delete().await()
                } catch (e: Exception) {
                    Log.d("tag", "Image deleted Failed")
                }
            }
            async {
                try {
                    postContaninerCollection.document(post.createdBy.myPostsContainer)
                        .update("myPosts", FieldValue.arrayRemove(post.docId)).await()
                } catch (e: Exception) {
                    Log.d("tag", "Post removed form User Failed")
                }
            }
        }
        job.join()
        Log.d("tag", "Post deleted completely ****")
    }
}

interface IPostInterface {
    fun onLikeClicked(postId: String)
}