package com.team.jubjub.ui.mypage

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.team.jubjub.databinding.ItemScrapPostBinding

class MyScrapAdapter(
    private val onClick: (MyScrapItem) -> Unit
) : ListAdapter<MyScrapItem, MyScrapAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemScrapPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding, onClick)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    class VH(
        private val binding: ItemScrapPostBinding,
        private val onClick: (MyScrapItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MyScrapItem) {
            // item_scrap_post.xml id 기준
            binding.tvTitle1.text = item.title
            binding.tvText1.text = item.preview
            binding.tvLocation1.text = item.locationTime
            binding.tvChat1.text = item.chatCount.toString()

            // 이미지도 쓰고 싶으면 모델에 resId/url 추가해서 여기서 세팅
            // binding.icBackground1.setImageResource(item.imageResId)

            binding.root.setOnClickListener { onClick(item) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<MyScrapItem>() {
            override fun areItemsTheSame(oldItem: MyScrapItem, newItem: MyScrapItem): Boolean {
                // id 없으면 임시로 title+preview 같이 쓰는 게 그나마 안전
                return oldItem.title == newItem.title && oldItem.preview == newItem.preview
            }

            override fun areContentsTheSame(oldItem: MyScrapItem, newItem: MyScrapItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}
