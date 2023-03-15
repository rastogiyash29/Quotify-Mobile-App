package com.example.quotify.models

data class Post(
    val text:String="",
    val imageUrl: String ="",
    val createdBy:User=User(),
    val likedBy: ArrayList<String> = ArrayList(),
    val commentBox:String="",
    val createdAt:Long=0L,
    val docId:String=""
)
