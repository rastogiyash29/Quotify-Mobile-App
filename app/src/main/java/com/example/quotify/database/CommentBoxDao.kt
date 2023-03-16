package com.example.quotify.database

import com.example.tempapp.models.Comment
import com.example.tempapp.models.CommentBox
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class CommentBoxDao {
    val db = FirebaseFirestore.getInstance()
    val commentBoxCollections = db.collection("commentBoxes")

    fun getCommentBoxById(id: String): Task<DocumentSnapshot> {
        return commentBoxCollections.document(id).get()
    }

    fun getNewCommentBox(commentBox: CommentBox): Task<DocumentReference> {
        return commentBoxCollections.add(commentBox)
    }

    fun addCommentInBoxId(comment: Comment, boxId: String): Task<Void> {
        return commentBoxCollections.document(boxId).update("list", FieldValue.arrayUnion(comment))
    }
}