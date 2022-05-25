package com.anytypeio.anytype.core_ui.features.navigation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemListObjectBinding
import com.anytypeio.anytype.core_ui.widgets.ObjectIconWidget
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.navigation.DefaultObjectView
import com.anytypeio.anytype.presentation.navigation.ObjectView

@Deprecated("LEGACY SUSPECT")
class PageLinksAdapter(
    private var data: List<ObjectView>,
    private val onClick: (Id, ObjectType.Layout?) -> Unit
) : RecyclerView.Adapter<PageLinksAdapter.PageLinkHolder>() {

    fun updateLinks(links: List<ObjectView>) {
        data = links
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PageLinkHolder = PageLinkHolder(
        binding = ItemListObjectBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    ).apply {
        itemView.setOnClickListener {
            val pos = bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                onClick(data[pos].id, data[pos].layout)
            }
        }
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holderLink: PageLinkHolder, position: Int) {
        holderLink.bind(data[position])
    }

    class PageLinkHolder(val binding: ItemListObjectBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val untitled = itemView.resources.getString(R.string.untitled)
        private val title = binding.tvTitle
        private val subtitle = binding.tvSubtitle
        private val icon = binding.ivIcon

        fun bind(link: ObjectView) {
            title.text = link.title.ifEmpty { untitled }
            if (link.subtitle.isBlank()) {
                subtitle.gone()
            } else {
                subtitle.visible()
                subtitle.text = link.subtitle
            }
            icon.setIcon(link.icon)
        }
    }
}

class DefaultObjectViewAdapter(
    private val onClick: (Id, ObjectType.Layout?) -> Unit
) : ListAdapter<DefaultObjectView, DefaultObjectViewAdapter.ObjectViewHolder>(Differ) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ObjectViewHolder = ObjectViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_list_object, parent, false)
    ).apply {
        itemView.setOnClickListener {
            val pos = bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                onClick(getItem(pos).id, getItem(pos).layout)
            }
        }
    }

    override fun onBindViewHolder(holderLink: ObjectViewHolder, position: Int) {
        holderLink.bind(getItem(position))
    }

    class ObjectViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val title = itemView.findViewById<TextView>(R.id.tvTitle)
        private val subtitle = itemView.findViewById<TextView>(R.id.tvSubtitle)
        private val icon = itemView.findViewById<ObjectIconWidget>(R.id.ivIcon)

        fun bind(link: DefaultObjectView) {
            title.text = link.name
            subtitle.text = link.typeName
            icon.setIcon(link.icon)
        }
    }

    object Differ : DiffUtil.ItemCallback<DefaultObjectView>() {
        override fun areItemsTheSame(
            oldItem: DefaultObjectView,
            newItem: DefaultObjectView
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: DefaultObjectView,
            newItem: DefaultObjectView
        ): Boolean = oldItem == newItem
    }
}