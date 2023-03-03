package com.example.quotify.view_models

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.quotify.models.MyQuote
import com.example.quotify.models.Result
import com.example.quotify.repository.QuoteRepository

class ViewModelDiaryQuotes(val repository: QuoteRepository, private var context: Context) :
    ViewModel() {

    //Defining LiveData
    private var quotesFromDiary: LiveData<List<MyQuote>>

    init {
        quotesFromDiary = repository.getQuotesFromDiaryLiveData()
    }

    //defining getters
    fun getQuotesFromDiaryLiveData(): LiveData<List<MyQuote>> {
        return quotesFromDiary
    }

    //Defining List And Objects for EachModes
    var quotesFromDiaryList: List<MyQuote>? = ArrayList()

    //writting setters for above list to take care while liveData Updates
    fun setquotesFromDiaryList(list: List<MyQuote>) {
        quotesFromDiaryList = list
    }

    //Adding logic for additions of quotes in databases in various modes
    suspend fun addQuote(myQuote: MyQuote): Boolean {
        repository.addQuoteInDiary(myQuote)
        Toast.makeText(context, "Added in Diary", Toast.LENGTH_SHORT).show()
        return false
    }

    //Delete function
    suspend fun deleteQuote(index:Int) {
        repository.deleteFromDiary(quotesFromDiaryList!![index])
    }

    //It can clear quotes from Favourites and Dairy only
    suspend fun clearQuotes() :Boolean{
        if(quotesFromDiaryList.isNullOrEmpty())return false
        repository.clearDiary()
        return true
    }

    //Edit functionality of Diary
    suspend fun editQuote(primaryKey:Int,newText:String,newAuthor:String){
        repository.editInDiary(primaryKey,newText,newAuthor)
    }
}