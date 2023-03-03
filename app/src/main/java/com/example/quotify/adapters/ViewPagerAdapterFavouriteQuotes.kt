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
import com.example.quotify.models.Result
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ViewPagerAdapterFavouriteQuotes(var list: List<Result>, private var context: Context) :
    RecyclerView.Adapter<ViewPagerAdapterFavouriteQuotes.PageViewHolder>() {
    private var adapterCallback: ViewPagerAdapterFavouriteQuotes.AdapterCallback? = null

    init {
        adapterCallback = context as ViewPagerAdapterFavouriteQuotes.AdapterCallback
    }

    class PageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val quoteText: TextView = itemView.findViewById(R.id.QuoteTextDB)
        val quoteAuthor: TextView = itemView.findViewById(R.id.QuoteAuthorDB)
        val deleteBtn: ImageView = itemView.findViewById(R.id.deleteButtonDB)
        val shareBtn: FloatingActionButton = itemView.findViewById(R.id.shareButtonDB)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        return PageViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.quote_card_favourites, parent, false)
        )
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        holder.quoteAuthor.text = list[position].author
        holder.quoteText.text = list[position].content
        holder.quoteText.setMovementMethod(ScrollingMovementMethod.getInstance());
        holder.deleteBtn.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                adapterCallback?.onDelete(position)
            }
        }
        holder.shareBtn.setOnClickListener {
            adapterCallback?.onShare(position)
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
    }
}