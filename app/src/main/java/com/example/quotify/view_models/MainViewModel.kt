package com.example.quotify.view_models

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quotify.models.Result
import com.example.quotify.models.MyQuote
import com.example.quotify.models.QuoteList
import com.example.quotify.repository.QuoteRepository
import com.example.quotify.utils.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel( val repository: QuoteRepository, private var context: Context) :
    ViewModel() {

    var mode = 0      //0 is online mode and 1 is offline mode 2 is diary mode
    private val totalModes = 3
    private val pointers = IntArray(totalModes)
    private val pageNumber = 1
    lateinit var quotesFromDBlateinit:LiveData<List<Result>>

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getQuotesByPage(1)
            quotesFromDBlateinit=repository.quoteDatabase.quoteDao().getQuotes()
        }
        mode = 2
    }

    val quotesFromInternet: LiveData<QuoteList>
        get() = repository.quotesFromInternet

    fun switchModes() {
        mode = (mode + 1) % totalModes
        if (mode == 0) {    //online mode
            if (NetworkUtils.isInternetAvailable(context)) {

            } else {
                switchModes()
            }
        } else if (mode == 1) {  //offline mode

        } else {  //diary mode

        }
    }

    fun addQuote() {
        if (mode == 1) {
            repository.addInDB()
        } else if (mode == 2) {
            repository.addInDiary()
        }
    }

}