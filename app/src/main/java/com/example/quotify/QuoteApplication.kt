package com.example.quotify

import android.app.Application
import com.example.quotify.database.QuoteDatabase
import com.example.quotify.live_quotes.QuotesAPI
import com.example.quotify.live_quotes.RetrofitHelper
import com.example.quotify.repository.QuoteRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class QuoteApplication : Application() {

    lateinit var quoteRepository: QuoteRepository

    override fun onCreate() {
        super.onCreate()
        initialise()
    }

    private fun initialise() {
        val quoteAPI = RetrofitHelper.getInstance().create(QuotesAPI::class.java)
        val database = QuoteDatabase.getDatabase(applicationContext)
        quoteRepository = QuoteRepository(quoteAPI, database, applicationContext)
    }
}