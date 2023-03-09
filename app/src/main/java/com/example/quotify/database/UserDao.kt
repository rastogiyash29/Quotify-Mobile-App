package com.example.quotify.database

import android.util.Log
import com.example.quotify.models.MyPostsContainer
import com.example.quotify.models.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserDao {

    private val db = FirebaseFirestore.getInstance()
    private val userCollection = db.collection("users")

    fun addUser(user: User): Task<Void> {
        return userCollection.document(user.uid).set(user)
    }

    fun getUserById(uid: String): Task<DocumentSnapshot> {
        return userCollection.document(uid).get()
    }

    private val postsContainerCollection = db.collection("postsContainers")

    suspend fun addPost(postId: String) {
        var myPostsContainer: String
        try {
            val user = getUserById(Firebase.auth.uid!!).await().toObject(User::class.java)!!
            myPostsContainer = user.myPostsContainer
        } catch (e: Exception) {
            Log.d("tag", "AddPost Failed Unable to fetch USER")
            return
        }
        try {
            postsContainerCollection.document(myPostsContainer)
                .update("myPosts", FieldValue.arrayUnion(postId)).await()
        } catch (e: Exception) {
            Log.d("tag", "Appending In MyCont.. Failed")
            return
        }
    }

    fun getNewPostsContainer(): Task<DocumentReference> {
        return postsContainerCollection.add(MyPostsContainer())
    }
}


