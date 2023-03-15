package com.example.quotify.adapters

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.quotify.R
import com.example.quotify.database.IPostInterface
import com.example.quotify.database.PostDao
import com.example.quotify.models.MyQuote
import com.example.quotify.models.Post
import com.example.quotify.utils.TimePastCalculator
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PostAdapter(options: FirestoreRecyclerOptions<Post>, val listener: IPostInterface) :
    FirestoreRecyclerAdapter<Post, PostAdapter.PostViewHolder>(
        options
    ) {

    val authUid=Firebase.auth.uid

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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val viewHolder = PostViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.post_layout, parent, false)
        )
//        viewHolder.likeBtn.setOnClickListener {
        //listener.onLikeClicked(snapshots.getSnapshot(viewHolder.adapterPosition).id)
//            GlobalScope.launch {
//                PostDao().updateLikesInPost(snapshots.getSnapshot(viewHolder.adapterPosition).id)
//            }
//        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int, post: Post) {
        Log.d("tag", "${post.toString()}")
        holder.authorNameTV.text = post.createdBy.displayName
        Glide.with(holder.profilePhotoIV.context).load(post.createdBy.imageUrl)
            .circleCrop().into(holder.profilePhotoIV)
        holder.postTextTV.text = post.text
        holder.likesTV.text = post.likedBy.size.toString()
//        holder.commentBox.text = post.commentBox
        if (post.imageUrl.isNotEmpty())
            Glide.with(holder.postImageIV.context).load(post.imageUrl).into(holder.postImageIV)
        else {
            holder.postImageIV.setImageDrawable(null)
        }
        if (post.likedBy.contains(authUid)) {
            holder.likeBtn.setImageResource(R.drawable.ic_baseline_favorite_24)
        }else{
            holder.likeBtn.setImageResource(R.drawable.ic_baseline_favorite_border_24)
        }
        holder.likeBtn.setOnClickListener {
            listener.onLikeClicked(post.docId)
        }
        holder.shareBtn.setOnClickListener {
            //Yet to be implemented
        }
//        holder.timeStamp.text=TimePastCalculator.covertTimeToText(post.createdAt.toString())
    }
}