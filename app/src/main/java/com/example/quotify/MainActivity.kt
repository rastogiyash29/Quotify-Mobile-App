package com.example.quotify

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.quotify.databinding.ActivityMainBinding
import com.example.quotify.models.Result
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

        //Setting LiveData Observers
        mainViewModel.getQuotesFromInternetLiveData().observe(this,{
            mainViewModel.setquotesFromInternetList(it)
            if(it!=null)
                binding.showData.text=it.toString()
        })

        mainViewModel.getQuotesFromDatabaseLiveData().observe(this,{
            mainViewModel.setquotesFromDatabaseList(it)
            if(it!=null)
                binding.showData.text=it.toString()
        })

        mainViewModel.getQuotesFromDiaryLiveData().observe(this,{
            mainViewModel.setquotesFromDiaryList(it)
            if(it!=null)
                binding.showData.text=it.toString()
        })

        binding.Mode.text="Online Mode"

        //Setting onClick Listeners
        binding.switchButton.setOnClickListener {
            switchMode()
        }

        binding.addButton.setOnClickListener {
            addQuote()
        }

        binding.deleteButton.setOnClickListener {
            deleteQuote()
        }

        binding.clearButton.setOnClickListener {
            clearAllQuotes()
        }
    }

    private fun clearAllQuotes() {
        mainViewModel.clearQuotes()
    }

    private fun deleteQuote() {
        mainViewModel.delete()
    }

    private fun addQuote() {
        mainViewModel.addQuote(Result(0,"","~yash","","Consistent in DB","","",1))
    }

    private fun switchMode() {
        binding.showData.text=mainViewModel.switchModes()
        if (mainViewModel.mode == 0) {
            binding.Mode.text = "Online Mode"
        } else if (mainViewModel.mode == 1) {
            binding.Mode.text = "Offline DB Mode"
        } else if (mainViewModel.mode == 2) {
            binding.Mode.text = "Diary Mode"
        }
    }
}