package com.team.jubjub.ui.post

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.team.jubjub.R
import com.team.jubjub.data.model.Comment
import com.team.jubjub.databinding.ItemCommentBinding
import com.team.jubjub.databinding.ItemLostFoundPostDetailHeaderBinding
import com.team.jubjub.databinding.ItemSharePostDetailHeaderBinding
import java.text.SimpleDateFormat
import java.util.Locale

class DetailAdapter(
    private var header: DetailHeader,
    private val comments: MutableList<Comment>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private companion object {
        const val TYPE_HEADER_SHARE = 0
        const val TYPE_HEADER_LOST_FOUND = 1
        const val TYPE_COMMENT = 2
    }

    override fun getItemCount(): Int = 1 + comments.size

    override fun getItemViewType(position: Int): Int {
        if (position != 0) return TYPE_COMMENT

        return when (header) {
            is DetailHeader.Share -> TYPE_HEADER_SHARE
            is DetailHeader.LostFound -> TYPE_HEADER_LOST_FOUND
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER_SHARE ->
                ShareHeaderVH(ItemSharePostDetailHeaderBinding.inflate(inflater, parent, false))

            TYPE_HEADER_LOST_FOUND ->
                LostFoundHeaderVH(ItemLostFoundPostDetailHeaderBinding.inflate(inflater, parent, false))

            else ->
                CommentVH(ItemCommentBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ShareHeaderVH -> holder.bind(header as DetailHeader.Share)
            is LostFoundHeaderVH -> holder.bind(header as DetailHeader.LostFound)
            is CommentVH -> holder.bind(comments[position - 1])
        }
    }

    fun addComment(c: Comment) {
        comments.add(c)
        notifyItemInserted(comments.size)
    }

    class ShareHeaderVH(
        private val binding: ItemSharePostDetailHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(h: DetailHeader.Share) {
            binding.tvSharePostDetailIdDate.text = h.idDate
            binding.tvSharePostDetailTitle.text = h.title
            binding.tvSharePostDetailCategory.text = h.category
            binding.tvSharePostDetailCondition.text = h.condition
            binding.tvSharePostDetailCount.text = h.count
            binding.tvSharePostDetailContent.text = h.content
            binding.tvSharePostDetailLocation.text = h.location

            binding.tvSharePostDetailMethodDelivery.alpha = if (h.deliveryEnabled) 1f else 0.3f
            binding.tvSharePostDetailMethodDirect.alpha = if (h.directEnabled) 1f else 0.3f

            if (!h.imageUrl.isNullOrEmpty()) {
                binding.ivSharePostDetailImage.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(h.imageUrl)
                    .transform(CenterCrop(), RoundedCorners(16))
                    .into(binding.ivSharePostDetailImage)
            } else {
                binding.ivSharePostDetailImage.visibility = View.GONE
            }
        }
    }

    class LostFoundHeaderVH(
        private val binding: ItemLostFoundPostDetailHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(h: DetailHeader.LostFound) {
            binding.tvLostFoundPostDetailIdDate.text = h.idDate
            binding.tvLostFoundPostDetailTitle.text = h.title
            binding.tvLostFoundPostDetailFoundPlace.text = h.foundPlace
            binding.tvLostFoundPostDetailDetailPlace.text = h.detailPlace
            binding.tvLostFoundPostDetailFoundDate.text = h.foundDate
            binding.tvLostFoundPostDetailContent.text = h.content
            binding.tvLostFoundPostDetailEntrustedPlace.text = h.entrustedPlace

            if (!h.imageUrl.isNullOrEmpty()) {
                binding.ivLostFoundPostDetailImage.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(h.imageUrl)
                    .transform(CenterCrop(), RoundedCorners(16))
                    .into(binding.ivLostFoundPostDetailImage)
            } else {
                binding.ivLostFoundPostDetailImage.visibility = View.GONE
            }
        }
    }

    class CommentVH(private val binding: ItemCommentBinding) : RecyclerView.ViewHolder(binding.root) {

        private val baseRowMarginStart =
            (binding.llCommentRow.layoutParams as ViewGroup.MarginLayoutParams).marginStart
        private val baseBodyMarginStart =
            (binding.tvBody.layoutParams as ViewGroup.MarginLayoutParams).marginStart

        private val dateFormat = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())

        fun bind(item: Comment) {
            binding.tvNickname.text = item.writerNickname
            binding.tvTime.text = item.createdAt?.let { dateFormat.format(it) } ?: "방금 전"
            binding.tvBody.text = item.content

            // 기본 이미지: ic_launcher_round (앱 아이콘)
            if (!item.writerProfileImageUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(item.writerProfileImageUrl)
                    .circleCrop()
                    .placeholder(R.mipmap.ic_launcher_round)
                    .error(R.mipmap.ic_launcher_round)
                    .into(binding.ivProfile)
            } else {
                binding.ivProfile.setImageResource(R.mipmap.ic_launcher_round)
            }

            val isReply = item.parentId != null
            binding.ivReplyArrow.visibility = if (isReply) View.VISIBLE else View.GONE

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