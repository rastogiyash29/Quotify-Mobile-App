package com.example.quotify.database

import com.example.quotify.models.User
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore


class UserDao {

    private val db = FirebaseFirestore.getInstance()
    private val userCollection = db.collection("users")

    fun addUser(user: User): Task<Void> {
        return userCollection.document(user.uid).set(user)
    }

    fun getUserById(uid: String): Task<DocumentSnapshot> {
        return userCollection.document(uid).get()
    }
}


