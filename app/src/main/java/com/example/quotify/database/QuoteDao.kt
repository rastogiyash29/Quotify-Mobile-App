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
    suspend fun addQuote(result: Result)

    @Query("SELECT * FROM quote_table")
    fun getQuotes():LiveData<List<Result>>

    @Delete
    suspend fun deleteQuote(result:Result)

    @Query("SELECT * FROM quote_table")
    suspend fun getQuoteslist():List<Result>

    @Query("DELETE FROM quote_table WHERE primaryId = :primaryKey")
    suspend fun deleteByPrimaryKey(primaryKey:Int)

    @Query("DELETE FROM quote_table")
    suspend fun clearAllQuotes()
}