package com.anytypeio.anytype.core_ui.features.dataview.modals

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.*
import com.anytypeio.anytype.presentation.sets.filter.FilterClick
import com.anytypeio.anytype.presentation.sets.model.FilterView
import com.anytypeio.anytype.presentation.sets.model.FilterView.Companion.HOLDER_CHECKBOX
import com.anytypeio.anytype.presentation.sets.model.FilterView.Companion.HOLDER_DATE
import com.anytypeio.anytype.presentation.sets.model.FilterView.Companion.HOLDER_EMAIL
import com.anytypeio.anytype.presentation.sets.model.FilterView.Companion.HOLDER_NUMBER
import com.anytypeio.anytype.presentation.sets.model.FilterView.Companion.HOLDER_OBJECT
import com.anytypeio.anytype.presentation.sets.model.FilterView.Companion.HOLDER_PHONE
import com.anytypeio.anytype.presentation.sets.model.FilterView.Companion.HOLDER_STATUS
import com.anytypeio.anytype.presentation.sets.model.FilterView.Companion.HOLDER_TAG
import com.anytypeio.anytype.presentation.sets.model.FilterView.Companion.HOLDER_TEXT
import com.anytypeio.anytype.presentation.sets.model.FilterView.Companion.HOLDER_TEXT_SHORT
import com.anytypeio.anytype.presentation.sets.model.FilterView.Companion.HOLDER_URL

class FilterByAdapter(
    private var items: List<FilterView> = listOf(),
    private val click: (FilterClick) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    fun update(newItems: List<FilterView>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            HOLDER_TEXT, HOLDER_TEXT_SHORT, HOLDER_URL, HOLDER_PHONE, HOLDER_EMAIL -> {
                FilterTextViewHolder(
                    ItemDvViewerFilterTextBinding.inflate(
                        inflater, parent, false
                    )
                ).apply {
                    binding.iconRemoveText.setOnClickListener {
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            click(FilterClick.Remove(pos))
                        }
                    }
                    itemView.setOnClickListener {
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            click(FilterClick.Value(pos))
                        }
                    }
                }
            }
            HOLDER_NUMBER -> {
                FilterNumberViewHolder(
                    ItemDvViewerFilterNumberBinding.inflate(
                        inflater, parent, false
                    )
                ).apply {
                    binding.iconRemoveNumber.setOnClickListener {
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            click(FilterClick.Remove(pos))
                        }
                    }
                    itemView.setOnClickListener {
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            click(FilterClick.Value(pos))
                        }
                    }
                }
            }
            HOLDER_STATUS -> {
                FilterStatusViewHolder(
                    ItemDvViewerFilterStatusBinding.inflate(
                        inflater, parent, false
                    )
                ).apply {
                    binding.iconRemoveStatus.setOnClickListener {
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            click(FilterClick.Remove(pos))
                        }
                    }
                    itemView.setOnClickListener {
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            click(FilterClick.Value(pos))
                        }
                    }
                }
            }
            HOLDER_TAG -> {
                FilterTagViewHolder(
                    ItemDvViewerFilterTagBinding.inflate(
                        inflater, parent, false
                    )
                ).apply {
                    binding.iconRemoveTag.setOnClickListener {
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            click(FilterClick.Remove(pos))
                        }
                    }
                    itemView.setOnClickListener {
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            click(FilterClick.Value(pos))
                        }
                    }
                }
            }
            HOLDER_DATE -> {
                FilterDateViewHolder(
                    ItemDvViewerFilterDateBinding.inflate(
                        inflater, parent, false
                    )
                ).apply {
                    itemView.setOnClickListener {
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            click(FilterClick.Value(pos))
                        }
                    }
                    binding.iconRemoveDate.setOnClickListener {
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            click(FilterClick.Remove(pos))
                        }
                    }
                }
            }
            HOLDER_OBJECT -> {
                val views = inflater.inflate(R.layout.item_dv_viewer_filter_object, parent, false)
                FilterObjectViewHolder(
                    ItemDvViewerFilterObjectBinding.inflate(
                        inflater, parent, false
                    )
                ).apply {
                    binding.iconRemoveObject.setOnClickListener {
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            click(FilterClick.Remove(pos))
                        }
                    }
                    itemView.setOnClickListener {
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            click(FilterClick.Value(pos))
                        }
                    }
                }
            }
            HOLDER_CHECKBOX -> {
                FilterCheckboxViewHolder(
                    ItemDvViewerFilterCheckboxBinding.inflate(
                        inflater, parent, false
                    )
                ).apply {
                    binding.iconRemoveCheckbox.setOnClickListener {
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            click(FilterClick.Remove(pos))
                        }
                    }
                    itemView.setOnClickListener {
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            click(FilterClick.Value(pos))
                        }
                    }
                }
            }
            else -> throw IllegalStateException("Unexpected view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is FilterTextViewHolder -> {
                holder.bind(items[position] as FilterView.Expression)
            }
            is FilterNumberViewHolder -> {
                holder.bind(items[position] as FilterView.Expression.Number)
            }
            is FilterStatusViewHolder -> {
                holder.bind(items[position] as FilterView.Expression.Status)
            }
            is FilterTagViewHolder -> {
                holder.bind(items[position] as FilterView.Expression.Tag)
            }
            is FilterDateViewHolder -> {
                holder.bind(items[position] as FilterView.Expression.Date)
            }
            is FilterObjectViewHolder -> {
                holder.bind(items[position] as FilterView.Expression.Object)
            }
            is FilterCheckboxViewHolder -> {
                holder.bind(items[position] as FilterView.Expression.Checkbox)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is FilterView.Expression.Text -> HOLDER_TEXT
        is FilterView.Expression.Number -> HOLDER_NUMBER
        is FilterView.Expression.Date -> HOLDER_DATE
        is FilterView.Expression.Status -> HOLDER_STATUS
        is FilterView.Expression.Tag -> HOLDER_TAG
        is FilterView.Expression.Object -> HOLDER_OBJECT
        is FilterView.Expression.Email -> HOLDER_EMAIL
        is FilterView.Expression.Phone -> HOLDER_PHONE
        is FilterView.Expression.TextShort -> HOLDER_TEXT_SHORT
        is FilterView.Expression.Url -> HOLDER_URL
        is FilterView.Expression.Checkbox -> HOLDER_CHECKBOX
    }
}