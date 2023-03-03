package com.example.quotify.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.quotify.models.MyQuote

@Dao
interface DiaryDao {

    @Insert
    suspend fun insertQuote(myQuote: MyQuote)

    @Delete
    suspend fun deleteQuote(myQuote: MyQuote)

    @Query("SELECT * FROM my_quotes_table")
    fun getQuotes():LiveData<List<MyQuote>>

    @Query("DELETE FROM my_quotes_table")
    suspend fun clearAllQuotes()

    @Query("DELETE FROM my_quotes_table WHERE id = :primaryKey")
    suspend fun deleteByPrimaryKey(primaryKey:Int)

    @Query("UPDATE my_quotes_table SET text = :newText , author = :newAuthor WHERE id = :primaryKey")
    suspend fun updateByPrimaryKey(primaryKey:Int,newText:String,newAuthor:String)
}