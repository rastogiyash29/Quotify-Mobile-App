package com.example.quotify.database

import com.example.quotify.models.UserName
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class UserNameDao {

    private val db= FirebaseFirestore.getInstance()
    private val userNamesCollection=db.collection("userNamesToUids")

    fun getUidByUserName(userName:String): Task<DocumentSnapshot> {
        return userNamesCollection.document(userName).get()
    }

    fun addUserNameWithUid(userName:UserName): Task<Void> {
        return userNamesCollection.document(userName.userName).set(userName)
    }
}