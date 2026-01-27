package com.team.jubjub.ui.mypage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.team.jubjub.data.model.Post
import com.team.jubjub.databinding.ItemMyPostBinding

class MyPostAdapter(
    private val onItemClick: (Post) -> Unit,
    private val onMenuClick: (anchor: View, post: Post) -> Unit
) : ListAdapter<Post, MyPostAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemMyPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding, onItemClick, onMenuClick)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    class VH(
        private val binding: ItemMyPostBinding,
        private val onItemClick: (Post) -> Unit,
        private val onMenuClick: (View, Post) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(post: Post) {
            binding.tvTitle.text = post.title
            binding.tvPreview.text = post.content
            binding.tvChat.text = post.commentCount.toString()

            val location = post.foundLocation ?: post.storageLocation ?: "장소 미정"
            binding.tvLocationTime.text = location // 시간표시는 이후 ViewModel에서 만들어도 됨

            binding.root.setOnClickListener { onItemClick(post) }
            binding.btnMenu.setOnClickListener { v -> onMenuClick(v, post) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Post>() {
            override fun areItemsTheSame(oldItem: Post, newItem: Post) =
                oldItem.postId == newItem.postId

            override fun areContentsTheSame(oldItem: Post, newItem: Post) =
                oldItem == newItem
        }
    }
}
