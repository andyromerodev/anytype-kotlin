package com.anytypeio.anytype.core_ui.features.dataview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.databinding.ItemGridColumnHeaderBinding
import com.anytypeio.anytype.core_ui.databinding.ItemGridColumnHeaderPlusBinding
import com.anytypeio.anytype.presentation.sets.model.ColumnView

class ViewerGridHeaderAdapter() :
    ListAdapter<ColumnView, ViewerGridHeaderAdapter.HeaderViewHolder>(ColumnHeaderDiffCallback) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): HeaderViewHolder = when (viewType) {
        HEADER_TYPE -> HeaderViewHolder.DefaultHolder.create(parent)
        else -> throw IllegalStateException("Unexpected view type: $viewType")
    }

    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
        if (holder is HeaderViewHolder.DefaultHolder) holder.bind(getItem(position))
    }

//    override fun getItemViewType(position: Int) = if (position == super.getItemCount()) {
//        PLUS_TYPE
//    } else {
//        HEADER_TYPE
//    }

//    override fun getItemCount(): Int = if (super.getItemCount() == 0) 0 else super.getItemCount() + 1

    sealed class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        class DefaultHolder(val binding: ItemGridColumnHeaderBinding) : HeaderViewHolder(binding.root) {
            fun bind(item: ColumnView) {
                binding.cellText.text = item.text
            }

            companion object {
                fun create(
                    parent: ViewGroup
                ): DefaultHolder = DefaultHolder(
                    ItemGridColumnHeaderBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
        }

        class PlusHolder(val binding: ItemGridColumnHeaderPlusBinding) : HeaderViewHolder(binding.root) {
            companion object {
                fun create(
                    parent: ViewGroup
                ): PlusHolder = PlusHolder(
                    ItemGridColumnHeaderPlusBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
        }
    }

    object ColumnHeaderDiffCallback : DiffUtil.ItemCallback<ColumnView>() {
        override fun areItemsTheSame(
            oldItem: ColumnView,
            newItem: ColumnView
        ): Boolean = oldItem.key == newItem.key

        override fun areContentsTheSame(
            oldItem: ColumnView,
            newItem: ColumnView
        ): Boolean = oldItem == newItem
    }

    companion object {
        const val HEADER_TYPE = 0
        const val PLUS_TYPE = 1
    }
}