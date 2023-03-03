package com.example.quotify.view_models

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.quotify.models.MyQuote
import com.example.quotify.models.QuoteList
import com.example.quotify.models.Result
import com.example.quotify.repository.QuoteRepository

class ViewModelFavouriteQuotes(val repository: QuoteRepository, private var context: Context) :
    ViewModel() {

    //Defining LiveData For Fav Modes
    private var quotesFromDatabase: LiveData<List<Result>>

    init {
        quotesFromDatabase = repository.getQuotesFromDatabaseLiveData()
    }

    //defining getters
    fun getQuotesFromDatabaseLiveData(): LiveData<List<Result>> {
        return quotesFromDatabase
    }

    //Defining List And Objects for EachModes
    var quotesFromDatabaseList: List<Result> = ArrayList()

    //writting setters for above list to take care while liveData for eachMode Updates

    fun setquotesFromDatabaseList(list: List<Result>) {
        quotesFromDatabaseList = list
    }

    //Adding logic for additions of quotes in databases in various modes
    suspend fun addQuote(result: Result): Boolean {
        if (result.primaryId == -1) return false
        Toast.makeText(context, "Cannot create in Favourites", Toast.LENGTH_SHORT).show()
        return false
    }

    //Delete function
    suspend fun deleteQuote(index:Int) {
        repository.deleteFromDB(quotesFromDatabaseList!![index])
    }

    //It can clear quotes from Favourites and Dairy only
    suspend fun clearQuotes() :Boolean{
        if(quotesFromDatabaseList.isNullOrEmpty())return false
        repository.clearDB()
        return true
    }
}