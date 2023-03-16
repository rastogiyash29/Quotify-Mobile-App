package com.example.quotify.testing

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.quotify.R
import com.example.quotify.models.Post
import com.example.quotify.utils.TimePastCalculator
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RecyclerViewAdapter(var list: List<Post>, private var context: Context) :
    RecyclerView.Adapter<RecyclerViewAdapter.PostViewHolder>() {

    private var adapterCallback: AdapterCallback? = null

    init {
        adapterCallback = context as AdapterCallback
    }

    private val authUid = Firebase.auth.uid

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val authorNameTV = itemView.findViewById<TextView>(R.id.creatorNameTV)
        val profilePhotoIV = itemView.findViewById<ImageView>(R.id.profilePhoto)
        val postTextTV = itemView.findViewById<TextView>(R.id.postText)
        val postImageIV = itemView.findViewById<ImageView>(R.id.postImage)
        val likeBtn = itemView.findViewById<ImageView>(R.id.likeBtn)
        val likesTV = itemView.findViewById<TextView>(R.id.likesCount)
        val commentBox = itemView.findViewById<ImageView>(R.id.commentBtn)
        val timeStamp = itemView.findViewById<TextView>(R.id.timeStamp)
        val shareBtn = itemView.findViewById<ImageView>(R.id.shareBtn)

        var liked = false
        var likeCount = 0
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PostViewHolder {
        val viewHolder = PostViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.post_layout, parent, false)
        )
        return viewHolder
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = list[position]
        holder.authorNameTV.text = post.createdBy.displayName
        Glide.with(holder.profilePhotoIV.context).load(post.createdBy.imageUrl)
            .circleCrop().into(holder.profilePhotoIV)
        holder.postTextTV.text = post.text
        holder.likesTV.text = post.likedBy.size.toString()
        if (post.imageUrl.isNotEmpty())
            Glide.with(holder.postImageIV.context).load(post.imageUrl).into(holder.postImageIV)
        else {
            holder.postImageIV.setImageDrawable(null)
        }
        holder.likeCount = post.likedBy.size
        if (post.likedBy.contains(authUid)) {
            holder.likeBtn.setImageResource(R.drawable.ic_baseline_favorite_24)
            holder.liked = true
        } else {
            holder.likeBtn.setImageResource(R.drawable.ic_baseline_favorite_border_24)
            holder.liked = false
        }
        holder.likeBtn.setOnClickListener {
            if (holder.liked) {
                post.likedBy.remove(authUid)
                holder.likeBtn.setImageResource(R.drawable.ic_baseline_favorite_border_24)
            } else {
                post.likedBy.add(authUid!!)
                holder.likeBtn.setImageResource(R.drawable.ic_baseline_favorite_24)
            }
            holder.liked = !holder.liked
            holder.likesTV.text = post.likedBy.size.toString()
            adapterCallback?.onLiked(post)
        }
        holder.shareBtn.setOnClickListener {
            adapterCallback?.onShare(post)
        }
        holder.commentBox.setOnClickListener {
            adapterCallback?.onComment(post)
        }
        holder.timeStamp.text =
            TimePastCalculator.toDuration(System.currentTimeMillis() - post.createdAt)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    //Interface to call Activity Methods
    interface AdapterCallback {
        fun onLiked(post: Post)
        fun onShare(postId: Post)
        fun onComment(postId: Post)
    }

}