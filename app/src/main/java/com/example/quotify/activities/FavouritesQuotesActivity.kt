package com.example.quotify.activities

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
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.quotify.QuoteApplication
import com.example.quotify.R
import com.example.quotify.SplashActivity
import com.example.quotify.adapters.ViewPagerAdapterFavouriteQuotes
import com.example.quotify.adapters.ViewPagerAdapterOnlineQuotes
import com.example.quotify.databinding.ActivityFavouritesQuotesBinding
import com.example.quotify.models.Result
import com.example.quotify.view_models.ViewModelFactoryFavouriteQuotes
import com.example.quotify.view_models.ViewModelFavouriteQuotes
import kotlinx.coroutines.*

class FavouritesQuotesActivity : AppCompatActivity(),
    ViewPagerAdapterFavouriteQuotes.AdapterCallback {
    lateinit var binding: ActivityFavouritesQuotesBinding
    lateinit var viewPagerAdapterFavouriteQuotes: ViewPagerAdapterFavouriteQuotes
    lateinit var viewModelFavouriteQuotes: ViewModelFavouriteQuotes
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_favourites_quotes)
        binding.progressBarDB.visibility = View.GONE
        val repository = (application as QuoteApplication).quoteRepository
        //do entry of QuoteApplication in Manifest file also

        viewModelFavouriteQuotes =
            ViewModelProvider(
                this,
                ViewModelFactoryFavouriteQuotes(
                    repository,
                    applicationContext,
                )
            ).get(
                ViewModelFavouriteQuotes::class.java
            )

        viewPagerAdapterFavouriteQuotes =
            ViewPagerAdapterFavouriteQuotes(viewModelFavouriteQuotes.quotesFromDatabaseList!!, this)

        viewModelFavouriteQuotes.getQuotesFromDatabaseLiveData().observe(this, {
            viewModelFavouriteQuotes.setquotesFromDatabaseList(it)
            viewPagerAdapterFavouriteQuotes.list=it
            viewPagerAdapterFavouriteQuotes.notifyDataSetChanged()
            Log.d("tag","${it.toString()}")
            unblockInput()
        })

        //setting PagerView2 and linking adapter
        binding.pagerDB.adapter = viewPagerAdapterFavouriteQuotes

        binding.clearAllButtonDB.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                clearAllQuotes()
            }
        }

        binding.selectModeDB.setOnClickListener {
            startModeSelectionDialog()
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
            val intentToOnline = Intent(this, OnlineQuotesActivity::class.java)
            intentToOnline.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intentToOnline)
            dialog.dismiss()
        }
        favModeBtn.setOnClickListener {
            Toast.makeText(this, "Already in Favourties Mode", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        diaryModeBtn.setOnClickListener {
            val intentToDiary = Intent(this, DiaryQuotesActivity::class.java)
            intentToDiary.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intentToDiary)
            dialog.dismiss()
        }
        val quoteBookMode=dialog.findViewById<ImageView>(R.id.quoteBookMode)
        quoteBookMode.setOnClickListener {
            val intentToQuoteBook = Intent(this, SplashActivity::class.java)
            intentToQuoteBook.putExtra("waitStatus","false")
            startActivity(intentToQuoteBook)
            dialog.dismiss()
        }
    }

    //Adding share Button Functionality
    override fun onShare(index: Int) {
        val intent = Intent(Intent.ACTION_SEND)
        var result: Result = viewModelFavouriteQuotes.quotesFromDatabaseList!![index]
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
            viewModelFavouriteQuotes.deleteQuote(index)
            Toast.makeText(this@FavouritesQuotesActivity, "Deleted From Here", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private suspend fun clearAllQuotes() {
        blockInput()
        withContext(Dispatchers.Main) {
            val job = viewModelFavouriteQuotes.clearQuotes()
            if (!job) {
                unblockInput()
            }
        }
    }

    //Disable and Enable Functions of UserInterface
    fun AppCompatActivity.blockInput() {
        binding.progressBarDB.visibility = View.VISIBLE
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }

    fun AppCompatActivity.unblockInput() {
        binding.progressBarDB.visibility = View.GONE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    override fun onBackPressed() {
        startModeSelectionDialog()
    }
}