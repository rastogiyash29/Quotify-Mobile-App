package com.example.quotify.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quotify.models.QuoteList
import com.example.quotify.repository.QuoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(private val repository: QuoteRepository):ViewModel() {

    init{
        viewModelScope.launch(Dispatchers.IO){
            repository.getQuotesByPage(1)
        }
    }

    val quotes:LiveData<QuoteList>
    get()=repository.quotes

}