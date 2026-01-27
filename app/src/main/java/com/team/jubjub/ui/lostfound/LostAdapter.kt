package com.team.jubjub.ui.lostfound

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.team.jubjub.R
import com.team.jubjub.data.model.Post
import com.team.jubjub.databinding.ItemLostPostBinding
import java.text.SimpleDateFormat
import java.util.Locale

class LostAdapter(
    private val posts: List<Post>,
    private val onClick: (Post) -> Unit
) : RecyclerView.Adapter<LostAdapter.PostViewHolder>() {

    inner class PostViewHolder(
        private val binding: ItemLostPostBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("MM/dd", Locale.KOREA)

        fun bind(post: Post) {
            binding.tvTitle.text = post.title
            binding.tvContent.text = post.content

            val dateText = post.createdAt?.toDate()?.let { dateFormat.format(it) } ?: ""
            val location = post.foundLocation ?: "장소 미정"

            if (dateText.isNotBlank()) {
                binding.tvLocation.text = "$location · $dateText"
            } else {
                binding.tvLocation.text = location
            }

            binding.tvChatCount.text = post.commentCount.toString()

            val imageUrl = post.images.firstOrNull()

            if (!imageUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(imageUrl)
                    .transform(CenterCrop(), RoundedCorners(16))
                    .placeholder(R.drawable.ic_grid_blue)
                    .error(R.drawable.ic_grid_blue)
                    .into(binding.ivThumbnail)
            } else {
                binding.ivThumbnail.setImageResource(R.drawable.ic_grid_blue)
            }

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