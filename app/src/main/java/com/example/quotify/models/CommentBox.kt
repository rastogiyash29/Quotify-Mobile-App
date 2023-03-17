package com.example.tempapp.models

data class CommentBox(
    var docId: String="",
    val list: ArrayList<Comment> = ArrayList()
)
