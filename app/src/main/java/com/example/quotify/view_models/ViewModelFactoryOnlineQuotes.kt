package com.example.quotify.view_models

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.quotify.adapters.ViewPagerAdapterOnlineQuotes
import com.example.quotify.repository.QuoteRepository

class ViewModelFactoryOnlineQuotes(
    private val repository: QuoteRepository,
    private var context: Context
) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ViewModelOnlineQuotes(repository, context) as T
    }
}