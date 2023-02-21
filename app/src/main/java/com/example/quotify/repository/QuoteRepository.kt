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

class QuoteRepository(
    private val quotesAPI: QuotesAPI,
    val quoteDatabase: QuoteDatabase,
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
                quotesLiveData.postValue(quoteListResponse.body())
            }
        }
    }

    //    Always keep primaryKey value as 0 to let it auto-regenerate
    fun addInDB() {
        GlobalScope.launch {
            quoteDatabase.quoteDao()
                .addQuotes(Result(0, "yoyo", "", "This is Result QUote", "", "", "", 1))
        }
    }

    fun addInDiary() {
        GlobalScope.launch {
            quoteDatabase.diaryDao().insertQuote(MyQuote(0, "Consistency is Key", "~yash"))
        }
    }

}