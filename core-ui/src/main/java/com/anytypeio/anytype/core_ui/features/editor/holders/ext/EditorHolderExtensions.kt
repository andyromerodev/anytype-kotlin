package com.anytypeio.anytype.core_ui.features.editor.holders.ext

import android.view.ViewGroup
import androidx.core.view.updatePadding
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.features.editor.BlockAdapter
import com.anytypeio.anytype.core_ui.features.editor.EditorTouchProcessor
import com.anytypeio.anytype.core_ui.features.editor.holders.relations.RelationViewHolder
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

fun RelationViewHolder.setup(adapter: BlockAdapter): RelationViewHolder {
    with(itemView) {
        findViewById<ViewGroup?>(R.id.itemContainer)?.updatePadding(
            top = dimen(R.dimen.relation_view_padding_top),
            bottom = dimen(R.dimen.relation_view_padding_bottom)
        )
        val paddingStart = context.dimen(R.dimen.default_document_content_padding_start)
        val paddingEnd = context.dimen(R.dimen.default_document_content_padding_end)
        findViewById<ViewGroup>(R.id.content).apply {
            setBackgroundResource(R.drawable.item_block_code_multi_select_mode_selector)
            updatePadding(left = paddingStart.toInt(), right = paddingEnd.toInt())
        }
        setOnClickListener {
            val view = adapter.views[bindingAdapterPosition]
            check(view is BlockView.Relation)
            adapter.onClickListener(ListenerType.Relation.Related(view))
        }
    }
    return this
}

fun RelationViewHolder.setupPlaceholder(adapter: BlockAdapter): RelationViewHolder {
    with(itemView) {
        val paddingStart = context.dimen(R.dimen.default_document_content_padding_start)
        val paddingEnd = context.dimen(R.dimen.default_document_content_padding_end)
        findViewById<ViewGroup>(R.id.placeholderContainer).apply {
            setBackgroundResource(R.drawable.item_block_code_multi_select_mode_selector)
            updatePadding(left = paddingStart.toInt(), right = paddingEnd.toInt())
        }
        setOnClickListener {
            val view = adapter.views[bindingAdapterPosition]
            check(view is BlockView.Relation)
            adapter.onClickListener(ListenerType.Relation.Placeholder(view.id))
        }
        setOnLongClickListener {
            val view = adapter.views[bindingAdapterPosition]
            check(view is BlockView.Relation)
            this@setupPlaceholder.onBlockLongClick(
                root = this,
                target = view.id,
                clicked = adapter.onClickListener
            )
            true
        }
    }
    return this
}