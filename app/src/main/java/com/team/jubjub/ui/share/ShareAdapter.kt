package com.team.jubjub.ui.share

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.team.jubjub.data.model.Post
import com.team.jubjub.databinding.ItemSharePostBinding

class ShareAdapter(
    private val posts: List<Post>,
    private val onClick: (Post) -> Unit
) : RecyclerView.Adapter<ShareAdapter.PostViewHolder>() {

    inner class PostViewHolder(
        private val binding: ItemSharePostBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(post: Post) {
            binding.tvTitle.text = post.title
            binding.tvContent.text = post.content
            binding.tvLocation.text = post.hopeLocation ?: ""
            binding.tvChatCount.text = post.commentCount.toString()

            binding.root.setOnClickListener {
                onClick(post)
            }
        }
    }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemSharePostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(posts[position])
    }

    override fun getItemCount(): Int = posts.size
}
