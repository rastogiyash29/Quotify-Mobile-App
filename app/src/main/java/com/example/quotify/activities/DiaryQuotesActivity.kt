package com.example.quotify.activities

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.quotify.QuoteApplication
import com.example.quotify.R
import com.example.quotify.adapters.ViewPagerAdapterDiaryQuotes
import com.example.quotify.databinding.ActivityDiaryQuotesBinding
import com.example.quotify.models.MyQuote
import com.example.quotify.view_models.ViewModelDiaryQuotes
import com.example.quotify.view_models.ViewModelFactoryDiaryQuotes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class DiaryQuotesActivity : AppCompatActivity(), ViewPagerAdapterDiaryQuotes.AdapterCallback {
    lateinit var binding: ActivityDiaryQuotesBinding
    lateinit var viewPagerAdapterDiaryQuotes: ViewPagerAdapterDiaryQuotes
    lateinit var viewModelDiaryQuotes: ViewModelDiaryQuotes
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_diary_quotes)

        val repository = (application as QuoteApplication).quoteRepository
        //do entry of QuoteApplication in Manifest file also

        viewModelDiaryQuotes =
            ViewModelProvider(
                this,
                ViewModelFactoryDiaryQuotes(
                    repository,
                    applicationContext,
                )
            ).get(
                ViewModelDiaryQuotes::class.java
            )

        viewPagerAdapterDiaryQuotes =
            ViewPagerAdapterDiaryQuotes(viewModelDiaryQuotes.quotesFromDiaryList!!, this)

        viewModelDiaryQuotes.getQuotesFromDiaryLiveData().observe(this, {
            viewModelDiaryQuotes.setquotesFromDiaryList(it)
            viewPagerAdapterDiaryQuotes.list = it
            viewPagerAdapterDiaryQuotes.notifyDataSetChanged()
            Log.d("tag", "${it.toString()}")
            unblockInput()
        })

        //setting PagerView2 and linking adapter
        binding.pagerDiary.adapter = viewPagerAdapterDiaryQuotes

        binding.clearAllButtonDiary.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                clearAllQuotes()
            }
        }

        binding.selectModeDiary.setOnClickListener {
            startModeSelectionDialog()
        }

        binding.addNewQuoteDiary.setOnClickListener {
            startAddQuoteDialog()
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
            val intentToFav = Intent(this, FavouritesQuotesActivity::class.java)
            intentToFav.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intentToFav)
            dialog.dismiss()
        }
        diaryModeBtn.setOnClickListener {
            Toast.makeText(this, "Already in Diary Mode", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
    }

    //    Setting Add Quote Dialog
    private fun startAddQuoteDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.add_quote_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.70).toInt()
        dialog.window?.setLayout(width, height)

        val cancelBtn = dialog.findViewById<ImageView>(R.id.cancelBtn)
        val saveBtn = dialog.findViewById<Button>(R.id.saveBtn)
        val quoteText = dialog.findViewById<EditText>(R.id.quoteInput)
        val authorText = dialog.findViewById<EditText>(R.id.authorInput)

        cancelBtn.setOnClickListener {
            dialog.dismiss()
        }
        saveBtn.setOnClickListener {
            if (quoteText.text.isNotEmpty() && authorText.text.isNotEmpty()) {
                GlobalScope.launch(Dispatchers.Main) {
                    viewModelDiaryQuotes.addQuote(
                        MyQuote(
                            0, quoteText.text.toString(), authorText.text.toString()
                        )
                    )
                }
                blockInput()
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Enter valid quote and author", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    //starting edit dialog
    override fun onEdit(index: Int) {
        var myQuote: MyQuote = viewModelDiaryQuotes.quotesFromDiaryList!![index]
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.add_quote_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.70).toInt()
        dialog.window?.setLayout(width, height)

        val cancelBtn = dialog.findViewById<ImageView>(R.id.cancelBtn)
        val saveBtn = dialog.findViewById<Button>(R.id.saveBtn)
        val quoteText = dialog.findViewById<EditText>(R.id.quoteInput)
        val authorText = dialog.findViewById<EditText>(R.id.authorInput)

        quoteText.setText(myQuote.text.toString())
        authorText.setText(myQuote.author.toString())

        cancelBtn.setOnClickListener {
            dialog.dismiss()
        }
        saveBtn.setOnClickListener {
            if (quoteText.text.isNotEmpty() && authorText.text.isNotEmpty()) {
                GlobalScope.launch(Dispatchers.Main) {
                    viewModelDiaryQuotes.editQuote(
                        myQuote.id,
                        quoteText.text.toString(),
                        authorText.text.toString()
                    )
                }
                blockInput()
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Enter valid quote and author", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    //Adding share Button Functionality
    override fun onShare(index: Int) {
        val intent = Intent(Intent.ACTION_SEND)
        var myQuote: MyQuote = viewModelDiaryQuotes.quotesFromDiaryList!![index]
        intent.setType("text/plain")
        intent.putExtra(
            Intent.EXTRA_TEXT,
            "${myQuote.text} \n~${myQuote.author}"
        )
        startActivity(intent)
    }

    override suspend fun onDelete(index: Int) {
        blockInput()
        withContext(Dispatchers.Main) {
            viewModelDiaryQuotes.deleteQuote(index)
            Toast.makeText(this@DiaryQuotesActivity, "Deleted From Here", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private suspend fun clearAllQuotes() {
        blockInput()
        withContext(Dispatchers.Main) {
            val job = viewModelDiaryQuotes.clearQuotes()
            if (!job) {
                unblockInput()
            }
        }
    }

    //Disable and Enable Functions of UserInterface
    fun AppCompatActivity.blockInput() {
        binding.progressBarDiary.visibility = View.VISIBLE
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }

    fun AppCompatActivity.unblockInput() {
        binding.progressBarDiary.visibility = View.GONE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    override fun onBackPressed() {
        startModeSelectionDialog()
    }
}