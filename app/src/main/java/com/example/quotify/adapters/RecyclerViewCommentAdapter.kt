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
import com.bumptech.glide.Glide
import com.example.quotify.R
import com.example.quotify.utils.TimePastCalculator
import com.example.tempapp.models.Comment


class RecyclerViewCommentAdapter(var list: List<Comment>, private var context: Context) :
    RecyclerView.Adapter<RecyclerViewCommentAdapter.PostViewHolder>() {

    private var adapterCallback: AdapterCallback? = null

    init {
        adapterCallback = context as AdapterCallback
    }

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val authorNameTV = itemView.findViewById<TextView>(R.id.creatorNameTV)
        val profilePhotoIV = itemView.findViewById<ImageView>(R.id.profilePhoto)
        val commentTextTV = itemView.findViewById<TextView>(R.id.commentText)
        val timeStamp = itemView.findViewById<TextView>(R.id.timeStamp)
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PostViewHolder {
        val viewHolder = PostViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.comment_layout, parent, false)
        )
        viewHolder.commentTextTV.setMovementMethod(ScrollingMovementMethod())
        return viewHolder
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val comment = list[position]
        holder.authorNameTV.text = comment.user.displayName
        Glide.with(holder.profilePhotoIV.context).load(comment.user.imageUrl)
            .circleCrop().into(holder.profilePhotoIV)
        holder.commentTextTV.text = comment.text
        holder.timeStamp.text =
            TimePastCalculator.toDuration(System.currentTimeMillis() - comment.createdAt)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    //Interface to call Activity Methods
    interface AdapterCallback {

    }
}