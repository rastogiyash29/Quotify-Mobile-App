package com.example.quotify.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.quotify.models.Result

@Dao
interface QuoteDao {

    @Insert
    suspend fun addQuotes(quotes:List<Result>)

    @Query("SELECT * FROM quote_table")
    suspend fun getQuotes():List<Result>

    @Delete
    suspend fun deleteQuote(quote:Result)
}