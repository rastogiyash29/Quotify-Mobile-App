package com.example.quotify.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.quotify.database.QuoteDatabase
import com.example.quotify.live_quotes.QuotesAPI
import com.example.quotify.models.QuoteList
import com.example.quotify.utils.NetworkUtils

class QuoteRepository(
    private val quotesAPI: QuotesAPI,
    private val quoteDatabase: QuoteDatabase,
    private val applicationContext: Context
) {

    //defining liveData
    private val quotesLiveData = MutableLiveData<QuoteList>()

    val quotesFromInternet: LiveData<QuoteList>
        get() = quotesLiveData

    suspend fun getQuotesByPage(page: Int) {
        if (NetworkUtils.isInternetAvailable(applicationContext)) {
            val quoteListResponse = quotesAPI.getQuotesbyPage(page)
            if (quoteListResponse != null && quoteListResponse.body() != null) {
//                quoteDatabase.quoteDao()
//                    .addQuotes(quoteListResponse.body()!!.results)  //adding all online quotes in database
                quotesLiveData.postValue(quoteListResponse.body())
            }
        }
    }

    //defining liveData
    private val DBQuoteList = MutableLiveData<QuoteList>()

    val quotesFromDB: LiveData<QuoteList>
        get() = DBQuoteList

    suspend fun getQuotesFromDB() {
        val quotesFromDB = quoteDatabase.quoteDao().getQuotes()
        val quoteList = QuoteList(1, 1, 1, quotesFromDB, 1, 1)
        DBQuoteList.postValue(quoteList)
    }
}