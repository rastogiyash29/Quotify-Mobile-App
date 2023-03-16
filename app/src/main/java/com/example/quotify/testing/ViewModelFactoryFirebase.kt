package com.example.quotify.testing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.quotify.view_models.ViewModelDiaryQuotes

class ViewModelFactoryFirebase: ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ViewModelFirebaseMyPosts() as T
    }

}