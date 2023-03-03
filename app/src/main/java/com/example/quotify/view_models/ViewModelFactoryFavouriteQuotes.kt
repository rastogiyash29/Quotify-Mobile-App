package com.example.quotify.view_models

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.quotify.repository.QuoteRepository

class ViewModelFactoryFavouriteQuotes(
    private val repository: QuoteRepository,
    private var context: Context
):ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ViewModelFavouriteQuotes(repository, context) as T
    }

}