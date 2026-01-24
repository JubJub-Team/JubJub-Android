package com.team.jubjub.ui.post

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.team.jubjub.databinding.ItemCommentBinding
import com.team.jubjub.databinding.ItemPostHeaderBinding
import android.view.View
import androidx.core.view.updateLayoutParams

class DetailAdapter(
    private var header: PostHeader,
    private val comments: MutableList<Comment>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private companion object {
        const val TYPE_HEADER = 0
        const val TYPE_COMMENT = 1
    }

    override fun getItemCount(): Int = 1 + comments.size

    override fun getItemViewType(position: Int): Int =
        if (position == 0) TYPE_HEADER else TYPE_COMMENT

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> HeaderVH(ItemPostHeaderBinding.inflate(inflater, parent, false))
            else -> CommentVH(ItemCommentBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderVH -> holder.bind(header)
            is CommentVH -> holder.bind(comments[position - 1]) // 헤더 때문에 -1
        }
    }

    fun setHeader(newHeader: PostHeader) {
        header = newHeader
        notifyItemChanged(0)
    }

    fun addComment(c: Comment) {
        comments.add(c)
        notifyItemInserted(comments.size) // 헤더가 0이라, 새 댓글 position = comments.size
    }

    class HeaderVH(private val binding: ItemPostHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(h: PostHeader) {
            // item_post_header.xml에서 id로 만든 뷰들에 값 세팅
            binding.tvSharePostDetailIdDate.text = h.idDate
            binding.tvSharePostDetailTitle.text = h.title
            binding.tvSharePostDetailCategory.text = h.category
            binding.tvSharePostDetailCondition.text = h.condition
            binding.tvSharePostDetailCount.text = h.count
            binding.tvSharePostDetailContent.text = h.content
            binding.tvSharePostDetailLocation.text = h.location

            // 나눔 방법 표시(선택 가능 여부)
            binding.tvSharePostDetailMethodDelivery.alpha = if (h.deliveryEnabled) 1f else 0.3f
            binding.tvSharePostDetailMethodDirect.alpha = if (h.directEnabled) 1f else 0.3f
        }
    }

    class CommentVH(private val binding: ItemCommentBinding)
        : RecyclerView.ViewHolder(binding.root) {

        // base marginStart 저장 (재활용 때문에 꼭 필요)
        private val baseRowMarginStart =
            (binding.llCommentRow.layoutParams as ViewGroup.MarginLayoutParams).marginStart
        private val baseBodyMarginStart =
            (binding.tvBody.layoutParams as ViewGroup.MarginLayoutParams).marginStart

        fun bind(item: Comment) {
            binding.tvNickname.text = item.nickname
            binding.tvTime.text = item.timeText
            binding.tvBody.text = item.body

            val isReply = item.isReply

            // 1) 화살표는 대댓글일 때만 보이게 (자리도 없어짐)
            binding.ivReplyArrow.visibility = if (isReply) View.VISIBLE else View.GONE

            // 2) 대댓글이면 옆으로 밀기 (스샷 느낌)
            val indent = if (isReply) dpToPx(24) else 0

            binding.llCommentRow.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                marginStart = baseRowMarginStart + indent
            }
            binding.tvBody.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                marginStart = baseBodyMarginStart + indent
            }
        }

        private fun dpToPx(dp: Int): Int =
            (dp * itemView.resources.displayMetrics.density).toInt()
    }
}
