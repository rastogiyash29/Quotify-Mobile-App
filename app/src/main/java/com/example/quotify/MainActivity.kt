package com.example.quotify

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.quotify.databinding.ActivityMainBinding
import com.example.quotify.live_quotes.QuotesAPI
import com.example.quotify.live_quotes.RetrofitHelper
import com.example.quotify.repository.QuoteRepository
import com.example.quotify.view_models.MainViewModel
import com.example.quotify.view_models.MainViewModelFactory

class MainActivity : AppCompatActivity() {
    lateinit var mainViewModel: MainViewModel
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val repository = (application as QuoteApplication).quoteRepository
        //do entry of QuoteApplication in Manifest file also

        mainViewModel =
            ViewModelProvider(this, MainViewModelFactory(repository, applicationContext)).get(
                MainViewModel::class.java
            )

        binding.showData.text=mainViewModel.repository.quoteDatabase.diaryDao().getQuotes().value.toString()

        mainViewModel.repository.quoteDatabase.diaryDao().getQuotes().observe(this, {
            if (it != null)
                binding.showData.text = it.toString()
        })

        mainViewModel.repository.quoteDatabase.quoteDao().getQuotes().observe(this,{
            if (it != null)
                binding.showData.text = it.toString()
        })

        binding.switchButton.setOnClickListener {
            switchMode()
        }

        binding.addButton.setOnClickListener {
            addQuote()
        }
//
//        binding.deleteButton.setOnClickListener {
//            deleteQuote()
//        }
//
//        binding.clearButton.setOnClickListener {
//            clearAllQuotes()
//        }
    }

    private fun clearAllQuotes() {

    }

    private fun deleteQuote() {

    }

    private fun addQuote() {
        mainViewModel.addQuote()
    }

    private fun switchMode() {
        mainViewModel.switchModes()
        if (mainViewModel.mode == 0) {
            binding.Mode.text = "Online Mode"
            if (mainViewModel.quotesFromInternet.value != null) {
                binding.showData.text = mainViewModel.quotesFromInternet.value!!.toString()
            }
        } else if (mainViewModel.mode == 1) {
            binding.Mode.text = "Offline DB Mode"
            binding.showData.text = mainViewModel.quotesFromDBlateinit.value.toString()
        } else if (mainViewModel.mode == 2) {
            binding.Mode.text = "Diary Mode"
            binding.showData.text = mainViewModel.repository.quoteDatabase.diaryDao().getQuotes().value.toString()
        }
    }


}