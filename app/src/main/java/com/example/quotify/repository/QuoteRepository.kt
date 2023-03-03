package com.example.quotify.repository

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.annotation.RestrictTo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Room
import com.example.quotify.database.QuoteDatabase
import com.example.quotify.live_quotes.QuotesAPI
import com.example.quotify.models.MyQuote
import com.example.quotify.models.QuoteList
import com.example.quotify.models.Result
import com.example.quotify.utils.NetworkUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class QuoteRepository(
    private val quotesAPI: QuotesAPI,
    private val quoteDatabase: QuoteDatabase,
    private val applicationContext: Context
) {

    //defining MutableliveData for Online Quotes
    private val quotesLiveData = MutableLiveData<QuoteList>()

    private lateinit var quotesFromDiary: LiveData<List<MyQuote>>
    private lateinit var quotesFromDatabase: LiveData<List<Result>>
    private val quotesFromInternet: LiveData<QuoteList>
        get() = quotesLiveData

    init {
        GlobalScope.launch {
            quotesFromDiary = quoteDatabase.diaryDao().getQuotes()
            quotesFromDatabase = quoteDatabase.quoteDao().getQuotes()
//            getQuotesByPage(1)
        }
    }

    //defining getters
    fun getQuotesFromInternetLiveData(): LiveData<QuoteList> {
        return quotesFromInternet
    }

    fun getQuotesFromDatabaseLiveData(): LiveData<List<Result>> {
        return quotesFromDatabase
    }

    fun getQuotesFromDiaryLiveData(): LiveData<List<MyQuote>> {
        return quotesFromDiary
    }


    suspend fun getQuotesByPage(page: Int):Boolean {
        if (NetworkUtils.isInternetAvailable(applicationContext)) {
            val quoteListResponse = quotesAPI.getQuotesbyPage(page)
            if (quoteListResponse != null && quoteListResponse.body() != null) {
                quotesLiveData.postValue(quoteListResponse.body())
                Log.d("tag","added on page-> ${quoteListResponse.body()!!.page}")
                return true
            }
        }
        return false
    }

    //Adding new Quotes in databases
    suspend fun addQuoteInDiary(myQuote: MyQuote) {
        quoteDatabase.diaryDao().insertQuote(myQuote)
    }

    suspend fun addQuoteInDB(result: Result) {
        quoteDatabase.quoteDao().addQuote(result)
    }

    //Edit quote in Diary
    suspend fun editInDiary(primaryKey:Int,newText:String,newAuthor:String){
        quoteDatabase.diaryDao().updateByPrimaryKey(primaryKey,newText,newAuthor)
    }

    //Delete Quote Queries in databases
    suspend fun deleteFromDiary(myQuote: MyQuote) {
        quoteDatabase.diaryDao().deleteByPrimaryKey(myQuote.id)
    }

    suspend fun deleteFromDB(result: Result) {
        quoteDatabase.quoteDao().deleteByPrimaryKey(result.primaryId)
    }


    //Clearing Database Queries
    suspend fun clearDiary() {
        quoteDatabase.diaryDao().clearAllQuotes()
    }

    suspend fun clearDB() {
        quoteDatabase.quoteDao().clearAllQuotes()
    }
}