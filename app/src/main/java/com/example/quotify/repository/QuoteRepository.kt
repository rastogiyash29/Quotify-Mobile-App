package com.example.quotify.repository

import android.content.Context
import androidx.annotation.RestrictTo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
    private lateinit var quotesFromDatabase:LiveData<List<Result>>
    private val quotesFromInternet: LiveData<QuoteList>
        get() = quotesLiveData

    init {
        GlobalScope.launch {
            quotesFromDiary=quoteDatabase.diaryDao().getQuotes()
            quotesFromDatabase=quoteDatabase.quoteDao().getQuotes()
            getQuotesByPage(1)
        }
    }

    //defining getters
    fun getQuotesFromInternetLiveData():LiveData<QuoteList>{
        return quotesFromInternet
    }

    fun getQuotesFromDatabaseLiveData():LiveData<List<Result>>{
        return quotesFromDatabase
    }

    fun getQuotesFromDiaryLiveData():LiveData<List<MyQuote>>{
        return quotesFromDiary
    }


    suspend fun getQuotesByPage(page: Int){
        if (NetworkUtils.isInternetAvailable(applicationContext)) {
            val quoteListResponse = quotesAPI.getQuotesbyPage(page)
            if (quoteListResponse != null && quoteListResponse.body() != null) {
                quotesLiveData.postValue(quoteListResponse.body())
            }
        }
    }

    //Adding new Quotes in databases
    fun addQuoteInDiary(myQuote: MyQuote) {
        GlobalScope.launch {
            quoteDatabase.diaryDao().insertQuote(myQuote)
        }
    }

    fun addQuoteInDB(result: Result) {
        GlobalScope.launch {
            quoteDatabase.quoteDao().addQuote(result)
        }
    }

    //Delete Quote Queries in databases
    fun deleteFromDiary(myQuote: MyQuote){
        GlobalScope.launch {
            quoteDatabase.diaryDao().deleteByPrimaryKey(myQuote.id)
        }
    }

    fun deleteFromDB(result: Result){
        GlobalScope.launch {
            quoteDatabase.quoteDao().deleteByPrimaryKey(result.primaryId)
        }
    }


    //Clearing Database Queries
    fun clearDiary() {
        GlobalScope.launch {
            quoteDatabase.diaryDao().clearAllQuotes()
        }
    }

    fun clearDB(){
        GlobalScope.launch {
            quoteDatabase.quoteDao().clearAllQuotes()
        }
    }
}