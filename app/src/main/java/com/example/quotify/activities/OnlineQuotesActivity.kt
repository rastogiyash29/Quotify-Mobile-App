package com.example.quotify.activities

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.quotify.QuoteApplication
import com.example.quotify.R
import com.example.quotify.adapters.ViewPagerAdapterOnlineQuotes
import com.example.quotify.databinding.ActivityOnlineQuotesBinding
import com.example.quotify.models.Result
import com.example.quotify.view_models.ViewModelFactoryOnlineQuotes
import com.example.quotify.view_models.ViewModelOnlineQuotes
import kotlinx.coroutines.*

class OnlineQuotesActivity : AppCompatActivity(), ViewPagerAdapterOnlineQuotes.AdapterCallback {
    lateinit var binding: ActivityOnlineQuotesBinding
    lateinit var viewPagerAdapterOnlineQuotes: ViewPagerAdapterOnlineQuotes
    lateinit var viewModelOnlineQuote: ViewModelOnlineQuotes

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_online_quotes)
        binding.progressBarOn.visibility=View.GONE
        val repository = (application as QuoteApplication).quoteRepository
        //do entry of QuoteApplication in Manifest file also

        viewModelOnlineQuote =
            ViewModelProvider(
                this,
                ViewModelFactoryOnlineQuotes(
                    repository,
                    applicationContext,
                )
            ).get(
                ViewModelOnlineQuotes::class.java
            )

        viewPagerAdapterOnlineQuotes =
            ViewPagerAdapterOnlineQuotes(viewModelOnlineQuote.quotesFromInternetList,this)

        //setting PagerView2 and linking adapter
        binding.pagerOn.adapter = viewPagerAdapterOnlineQuotes

        viewModelOnlineQuote.getQuotesFromInternetLiveData().observe(this, {
            viewModelOnlineQuote.setquotesFromInternetList(it)
            if(viewModelOnlineQuote.quotesFromInternetList.isNullOrEmpty()){
                it.results.forEach {
                    viewModelOnlineQuote.quotesFromInternetList.add(it)
                }
                viewPagerAdapterOnlineQuotes.notifyDataSetChanged()
            }
        })

        //adding first page in online mode
        GlobalScope.launch(Dispatchers.Main) {
            addNewQuotes()
        }

        binding.selectModeOn.setOnClickListener {
            startModeSelectionDialog()
        }

        binding.NextButtonOn.setOnClickListener {
            if (viewModelOnlineQuote.quotesFromInternetList.isEmpty() ||
                binding.pagerOn.currentItem == viewModelOnlineQuote.quotesFromInternetList.size-1
            ){
                //Trying to add newPageInOnlineMode
                GlobalScope.launch(Dispatchers.Main) {
                    addNewQuotes()
                }
            }
        }
    }

    private fun startModeSelectionDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.mode_selection_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()

        val onlineModeBtn = dialog.findViewById<ImageView>(R.id.onlineMode)
        val favModeBtn = dialog.findViewById<ImageView>(R.id.favouritesMode)
        val diaryModeBtn = dialog.findViewById<ImageView>(R.id.diaryMode)

        onlineModeBtn.setOnClickListener {
            Toast.makeText(this, "Already in Online Mode", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        favModeBtn.setOnClickListener {
            val intentToFav = Intent(this, FavouritesQuotesActivity::class.java)
            intentToFav.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intentToFav)
            dialog.dismiss()
        }
        diaryModeBtn.setOnClickListener {
            val intentToDiary = Intent(this, DiaryQuotesActivity::class.java)
            intentToDiary.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intentToDiary)
            dialog.dismiss()
        }
    }

    //Adding share Button Functionality
    override fun onShare(index: Int) {
        val intent = Intent(Intent.ACTION_SEND)
        var result: Result = viewModelOnlineQuote.quotesFromInternetList[index]
        intent.setType("text/plain")
        intent.putExtra(
            Intent.EXTRA_TEXT,
            "${result.content} \n~${result.author}"
        )
        startActivity(intent)
    }

    override suspend fun onDelete(index: Int) {
        blockInput()
        withContext(Dispatchers.Main) {
            viewModelOnlineQuote.deleteQuote(index)
            viewPagerAdapterOnlineQuotes.notifyDataSetChanged()
            Toast.makeText(this@OnlineQuotesActivity, "Deleted From Here", Toast.LENGTH_SHORT)
                .show()
            delay(200)
        }
        unblockInput()
    }

    override suspend fun onAddToFav(index: Int) {
        blockInput()
        withContext(Dispatchers.Main) {
            val job =
                viewModelOnlineQuote.addQuote(index)
            if (job) {
                Toast.makeText(this@OnlineQuotesActivity, "Added to Favourites", Toast.LENGTH_SHORT)
                    .show()
                delay(200)
            }
        }
        unblockInput()
    }

    suspend fun addNewQuotes() {
        blockInput()
        withContext(Dispatchers.Main) {
            val job = viewModelOnlineQuote.addNewPageInOnlineMode()
            if (job) {           //job will be true only iff new page is added in online mode
                viewPagerAdapterOnlineQuotes.notifyDataSetChanged()
                delay(200)
            }
        }
        unblockInput()
    }

    //Disable and Enable Functions of UserInterface
    fun AppCompatActivity.blockInput() {
        binding.progressBarOn.visibility = View.VISIBLE
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }

    fun AppCompatActivity.unblockInput() {
        binding.progressBarOn.visibility = View.GONE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    override fun onBackPressed() {
        startModeSelectionDialog()
    }
}