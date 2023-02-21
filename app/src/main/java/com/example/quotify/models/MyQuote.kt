package com.example.quotify.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "my_quotes_table")
data class MyQuote(
    @PrimaryKey(autoGenerate = true)
    var id:Int,
    var text:String,
    var author:String
)
