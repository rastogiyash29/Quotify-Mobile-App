package com.example.quotify.view_models

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quotify.models.QuoteList
import com.example.quotify.repository.QuoteRepository
import com.example.quotify.utils.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(private val repository: QuoteRepository,private var context:Context):ViewModel() {

    private var mode=0      //0 is online mode and 1 is offline mode
    private val totalModes=2
    var quotes:LiveData<QuoteList>


    init{
        viewModelScope.launch(Dispatchers.IO){
            repository.getQuotesByPage(1)
            repository.getQuotesFromDB()
        }
        mode=1
        quotes=quotesFromDB
        switchModes()
    }

    val quotesFromInternet:LiveData<QuoteList>
    get()=repository.quotesFromInternet

    val quotesFromDB:LiveData<QuoteList>
    get()=repository.quotesFromDB


    fun switchModes(){
        mode=(mode+1)%totalModes
        if(mode==0){    //online mode
            if(NetworkUtils.isInternetAvailable(context)){
                quotes=quotesFromInternet

            }else{
                switchModes()
            }
        }else if(mode==1){  //offline mode
            quotes=quotesFromDB
        }
    }


}