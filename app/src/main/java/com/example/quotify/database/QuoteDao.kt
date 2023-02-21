package com.example.quotify.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.quotify.models.Result

@Dao
interface QuoteDao {

    @Insert
    suspend fun addQuotes(result: Result)

    @Query("SELECT * FROM quote_table")
    fun getQuotes():LiveData<List<Result>>

    @Delete
    suspend fun deleteQuote(result:Result)
}