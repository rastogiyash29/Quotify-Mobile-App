package com.example.quotify.adapters

import android.content.Context
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.quotify.R
import com.example.quotify.models.MyQuote
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ViewPagerAdapterDiaryQuotes(var list: List<MyQuote>,private var context:Context):RecyclerView.Adapter<ViewPagerAdapterDiaryQuotes.PageViewHolder>() {

    private var adapterCallback: ViewPagerAdapterDiaryQuotes.AdapterCallback? = null

    init {
        adapterCallback = context as ViewPagerAdapterDiaryQuotes.AdapterCallback
    }

    class PageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val quoteText: TextView = itemView.findViewById(R.id.QuoteTextDiary)
        val quoteAuthor: TextView = itemView.findViewById(R.id.QuoteAuthorDiary)
        val deleteBtn: ImageView = itemView.findViewById(R.id.deleteButtonDiary)
        val shareBtn: FloatingActionButton = itemView.findViewById(R.id.shareButtonDiary)
        val editBtn:FloatingActionButton=itemView.findViewById(R.id.editButtonDiary)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        return ViewPagerAdapterDiaryQuotes.PageViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.quote_card_diary, parent, false)
        )
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        holder.quoteAuthor.text = list[position].author
        holder.quoteText.text = list[position].text
        holder.quoteText.setMovementMethod(ScrollingMovementMethod.getInstance());
        holder.deleteBtn.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                adapterCallback?.onDelete(position)
            }
        }
        holder.shareBtn.setOnClickListener {
            adapterCallback?.onShare(position)
        }
        holder.editBtn.setOnClickListener {
            adapterCallback?.onEdit(position)
        }
        Log.d("tag", "$position")
    }

    override fun getItemCount(): Int {
        return list.size
    }

    //Interface to call Activity Methods
    interface AdapterCallback {
        fun onShare(index: Int)

        suspend fun onDelete(index: Int)

        fun onEdit(index:Int)
    }
}