package com.example.quotify.view_models

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.quotify.adapters.ViewPagerAdapterOnlineQuotes
import com.example.quotify.models.MyQuote
import com.example.quotify.models.QuoteList
import com.example.quotify.models.Result
import com.example.quotify.repository.QuoteRepository
import com.example.quotify.utils.NetworkUtils

class ViewModelOnlineQuotes(
    val repository: QuoteRepository,
    private var context: Context
) :
    ViewModel() {

    //Defining viewPager Adapter

    //Defining LiveData
    private var quotesFromInternet: LiveData<QuoteList>

    init {
        quotesFromInternet = repository.getQuotesFromInternetLiveData()
    }

    //defining getters
    fun getQuotesFromInternetLiveData(): LiveData<QuoteList> {
        return quotesFromInternet
    }

    //Defining List Online Mode
    var quotesFromInternetList: ArrayList<Result> = ArrayList()

    //writting setters for above list to take care while liveData for eachMode Updates
    fun setquotesFromInternetList(quoteList: QuoteList) {
        if (quoteList != null) {
            if (!downloadedPage.contains(quoteList.page)) {
                for (result in quoteList.results) {
                    quotesFromInternetList.add(result)
                }
            }
            downloadedPage.add(quoteList.page)
            pageNumber++
        }
    }

    //It Keeps Information of Pages Downloaded from Internet (Cache)
    private var pageNumber = 0
    private var downloadedPage: HashSet<Int> = HashSet()

    //Adding logic for additions of quotes in databases in various modes
    suspend fun addQuote(index: Int): Boolean {
        repository.addQuoteInDB(quotesFromInternetList[index])
        Toast.makeText(context, "Added to Favourites", Toast.LENGTH_SHORT).show()
        return true
    }

    fun deleteQuote(index: Int) {
        quotesFromInternetList.removeAt(index)
    }

    suspend fun addNewPageInOnlineMode(): Boolean {
        //Will add new downloaded page from here
        Toast.makeText(context, "Addition of New Page", Toast.LENGTH_SHORT).show()
        if (!downloadedPage.contains(pageNumber + 1)) {
            val job = repository.getQuotesByPage(pageNumber + 1)
            if (job) return true
        }
        return false
    }
}