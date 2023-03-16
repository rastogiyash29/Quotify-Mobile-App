package com.example.tempapp.models

import com.example.quotify.models.User

data class Comment(
    val user: User = User(),
    val text: String = "",
    val createdAt: Long = 0L
)
