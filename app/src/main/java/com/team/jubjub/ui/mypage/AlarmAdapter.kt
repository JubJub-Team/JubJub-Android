package com.team.jubjub.ui.mypage

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.team.jubjub.databinding.ItemAlarmBinding
import com.team.jubjub.databinding.ItemAlarmHeaderBinding

class AlarmAdapter(
    private val onClick: (Alarm) -> Unit
) : ListAdapter<AlarmUiItem, RecyclerView.ViewHolder>(DIFF) {

    companion object {
        private const val VT_HEADER = 0
        private const val VT_ROW = 1

        private val DIFF = object : DiffUtil.ItemCallback<AlarmUiItem>() {
            override fun areItemsTheSame(old: AlarmUiItem, new: AlarmUiItem): Boolean =
                when {
                    old is AlarmUiItem.Header && new is AlarmUiItem.Header -> old.title == new.title
                    old is AlarmUiItem.Row && new is AlarmUiItem.Row -> old.alarm.id == new.alarm.id
                    else -> false
                }

            override fun areContentsTheSame(old: AlarmUiItem, new: AlarmUiItem): Boolean = old == new
        }
    }

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is AlarmUiItem.Header -> VT_HEADER
            is AlarmUiItem.Row -> VT_ROW
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VT_HEADER -> HeaderVH(ItemAlarmHeaderBinding.inflate(inflater, parent, false))
            else -> RowVH(ItemAlarmBinding.inflate(inflater, parent, false), onClick)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is AlarmUiItem.Header -> (holder as HeaderVH).bind(item)
            is AlarmUiItem.Row -> (holder as RowVH).bind(item.alarm)
        }
    }

    class HeaderVH(
        private val binding: ItemAlarmHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AlarmUiItem.Header) {
            binding.tvHeader.text = item.title
        }
    }

    class RowVH(
        private val binding: ItemAlarmBinding,
        private val onClick: (Alarm) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(alarm: Alarm) {
            binding.tvTitle.text = alarm.title

            if (!alarm.isRead) {
                binding.root.setBackgroundColor(Color.parseColor("#20CD7C"))
                binding.tvTitle.setTextColor(Color.WHITE)
            } else {
                binding.root.setBackgroundColor(Color.TRANSPARENT)
                binding.tvTitle.setTextColor(Color.parseColor("#111111"))
            }

            binding.root.setOnClickListener { onClick(alarm) }
        }
    }
}
