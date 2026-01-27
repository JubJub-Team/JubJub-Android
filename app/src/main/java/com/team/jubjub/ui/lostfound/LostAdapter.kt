package com.team.jubjub.ui.lostfound

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.team.jubjub.data.model.Post
import com.team.jubjub.databinding.ItemLostPostBinding

class LostAdapter(
    private val posts: List<Post>,
    private val onClick: (Post) -> Unit
) : RecyclerView.Adapter<LostAdapter.PostViewHolder>() {

    inner class PostViewHolder(
        private val binding: ItemLostPostBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(post: Post) {
            binding.tvTitle.text = post.title
            binding.tvContent.text = post.content

            // 분실물은 foundLocation 사용
            binding.tvLocation.text = post.foundLocation ?: ""

            binding.tvChatCount.text = post.commentCount.toString()

            binding.root.setOnClickListener { onClick(post) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemLostPostBinding.inflate(
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
