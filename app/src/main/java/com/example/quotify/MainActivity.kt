package com.example.quotify

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.quotify.databinding.ActivityMainTempBinding
import com.example.quotify.models.Result
import com.example.quotify.view_models.MainViewModel
import com.example.quotify.view_models.MainViewModelFactory
import kotlinx.coroutines.*


class MainActivity : AppCompatActivity() {
    lateinit var mainViewModel: MainViewModel
    lateinit var binding: ActivityMainTempBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main_temp)
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
        setOnlineMode()

        //Setting onClick Listeners
        binding.addFavouriteButton.setOnClickListener {
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

        binding.clearAllButton.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                clearAllQuotes()
            }
        }

        binding.selectMode.setOnClickListener {
            startModeSelectionDialog()
        }

        binding.PreviousButton.setOnClickListener {
            prevQuote()
        }

        binding.NextButton.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                nextQuote()
            }
        }

        binding.shareButton.setOnClickListener {
            onShare()
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

    //Starting Mode Selection dialog
    private fun startModeSelectionDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.mode_selection_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()

        val onlineModeBtn=dialog.findViewById<ImageView>(R.id.onlineMode)
        val favModeBtn=dialog.findViewById<ImageView>(R.id.favouritesMode)
        val diaryModeBtn=dialog.findViewById<ImageView>(R.id.diaryMode)

        onlineModeBtn.setOnClickListener {
            setOnlineMode()
            dialog.dismiss()
        }
        favModeBtn.setOnClickListener {
            setFavouriteMode()
            dialog.dismiss()
        }
        diaryModeBtn.setOnClickListener {
            setDiaryMode()
            dialog.dismiss()
        }
    }

    private fun setOnlineMode(){
        val set=mainViewModel.setLiveMode()
        if(set){
            binding.selectMode.setImageResource(R.drawable.globe)
            setQuote()
        }
        Log.d("tag","online mode")
    }

    private fun setFavouriteMode(){
        val set=mainViewModel.setFavouritesMode()
        if(set){
            binding.selectMode.setImageResource(R.drawable.heart)
            setQuote()
        }
        Log.d("tag","Fav mode")
    }

    private fun setDiaryMode(){
        val set=mainViewModel.setDiaryMode()
        if(set){
            binding.selectMode.setImageResource(R.drawable.diary)
            setQuote()
        }
        Log.d("tag","Diary mode")
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
        var currentQuoteResult=mainViewModel.getCurrentQuote()
        binding.QuoteAuthor.text="~${currentQuoteResult.author} (${currentQuoteResult.primaryId})"
        binding.QuoteText.text="${currentQuoteResult.content}"
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

    //Adding share Button Functionality
    fun onShare() {
        val intent= Intent(Intent.ACTION_SEND)
        intent.setType("text/plain")
        intent.putExtra(Intent.EXTRA_TEXT,"${mainViewModel.getCurrentQuote().content} \n~${mainViewModel.getCurrentQuote().author}")
        startActivity(intent)
    }

}