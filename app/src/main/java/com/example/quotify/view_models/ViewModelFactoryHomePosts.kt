package com.example.quotify.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.quotify.view_models.ViewModelDiaryQuotes

class ViewModelFactoryHomePosts: ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ViewModelHomePosts() as T
    }

}