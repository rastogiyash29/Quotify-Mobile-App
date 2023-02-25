package com.example.quotify

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.quotify.databinding.ActivityMainBinding
import com.example.quotify.models.Result
import com.example.quotify.view_models.MainViewModel
import com.example.quotify.view_models.MainViewModelFactory
import kotlinx.coroutines.*
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import kotlin.math.log


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
        mainViewModel.getQuotesFromInternetLiveData().observe(this, {
            mainViewModel.setquotesFromInternetList(it)
        })

        mainViewModel.getQuotesFromDatabaseLiveData().observe(this, {
            mainViewModel.setquotesFromDatabaseList(it)
        })

        mainViewModel.getQuotesFromDiaryLiveData().observe(this, {
            mainViewModel.setquotesFromDiaryList(it)
        })

        //Setting first mode
        binding.Mode.text = "Online Mode"
        setQuote()
        hideProgressBar()


        //Setting onClick Listeners
        binding.addButton.setOnClickListener {
            //Always launch on Main thread of UI interaction takes place.
            GlobalScope.launch(Dispatchers.Main) {
                addQuote()
            }
        }

        binding.deleteButton.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                deleteQuote()
            }
        }

        binding.clearButton.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                clearAllQuotes()
            }
        }

        binding.switchButton.setOnClickListener {
            switchMode()
        }

        binding.prevButton.setOnClickListener {
            prevQuote()
        }

        binding.nextButton.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                nextQuote()
            }
        }

        //Setting Current Quote Live Data Observers
        mainViewModel.LiveQuote.observe(this, {
            setQuote()
            hideProgressBar()
        })

        mainViewModel.FavouriteQuote.observe(this, {
            setQuote()
            hideProgressBar()
        })

        mainViewModel.DiaryQuote.observe(this, {
            setQuote()
            hideProgressBar()
        })
    }

    //Functionalities Implementations
    private suspend fun addQuote() {
        showProgressBar()
        withContext(Dispatchers.Main) {
            val job =
                mainViewModel.addQuote(Result(0, "", "~yash", "", "Consistent in DB", "", "", 1))
            if (!job) {
                hideProgressBar()
            }
        }
    }

    private suspend fun deleteQuote() {
        showProgressBar()
        withContext(Dispatchers.Main) {
            val job = mainViewModel.delete()
            if (!job) {
                hideProgressBar()
            }
        }
    }

    private suspend fun clearAllQuotes() {
        showProgressBar()
        withContext(Dispatchers.Main) {
            val job = mainViewModel.clearQuotes()
            if (!job) {
                hideProgressBar()
            }
        }
    }

    private fun switchMode() {
        showProgressBar()
        val job: Boolean
        if (mainViewModel.mode == 0) {
            binding.Mode.text = "Offline DB Mode"
            job = mainViewModel.setFavouritesMode()
        } else if (mainViewModel.mode == 1) {
            binding.Mode.text = "Diary Mode"
            job = mainViewModel.setDiaryMode()
        } else if (mainViewModel.mode == 2) {
            binding.Mode.text = "Online Mode"
            job = mainViewModel.setLiveMode()
        } else
            job = false
        if (!job) {
            hideProgressBar()
            return
        }
    }

    private suspend fun nextQuote() {
        showProgressBar()
        withContext(Dispatchers.Main) {
            val job = mainViewModel.nextQuote()
            if (!job) {           //job will be true only iff new page is added in online mode
                hideProgressBar()
            }
        }
    }

    private fun prevQuote() {
        showProgressBar()
        val job = mainViewModel.prevQuote()
        if (!job) {         //here if already first quote was set then LiveData will not reload this hideProgressBar yourSelf
            hideProgressBar()
        }
    }

    //ProgressBar Functions
    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
        //Disabling User Interface while progress bar is visible
        blockInput()
    }

    private fun hideProgressBar() {
        //Enabling User Interface while progress bar is Invisible
        unblockInput()
        binding.progressBar.visibility = View.GONE

    }

    //Setting current Quote in showtext
    private fun setQuote() {
        binding.showData.text = mainViewModel.getCurrentQuote().toString()
    }

    //Disable and Enable Functions of UserInterface
    fun AppCompatActivity.blockInput() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }

    fun AppCompatActivity.unblockInput() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }
}