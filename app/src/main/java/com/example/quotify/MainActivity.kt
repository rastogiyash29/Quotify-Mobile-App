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

        mainViewModel.quotes.observe(this, {
            Toast.makeText(this, "${it.results.size.toString()} inits", Toast.LENGTH_SHORT).show()
            binding.showData.text = it.toString()
        })

        binding.switchButton.setOnClickListener {
            switchMode()
            if (mainViewModel.quotes.value != null) {
                Toast.makeText(
                    this,
                    mainViewModel.quotes.value!!.results.size.toString(),
                    Toast.LENGTH_SHORT
                ).show()
                binding.showData.text = mainViewModel.quotes.value!!.toString()
            }
        }


//        binding.addButton.setOnClickListener {
//            addQuote()
//        }
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

    }

    private fun switchMode() {
        mainViewModel.switchModes()
    }


}