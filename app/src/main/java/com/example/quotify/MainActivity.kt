package com.example.quotify

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.quotify.live_quotes.QuotesAPI
import com.example.quotify.live_quotes.RetrofitHelper
import com.example.quotify.repository.QuoteRepository
import com.example.quotify.view_models.MainViewModel
import com.example.quotify.view_models.MainViewModelFactory

class MainActivity : AppCompatActivity() {
    lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val repository=(application as QuoteApplication).quoteRepository
        //do entry of QuoteApplication in Manifest file also

        mainViewModel =
            ViewModelProvider(this, MainViewModelFactory(repository)).get(MainViewModel::class.java)

        mainViewModel.quotes.observe(this,{
            Toast.makeText(this,it.results.size.toString(),Toast.LENGTH_SHORT).show()
        })
    }
}