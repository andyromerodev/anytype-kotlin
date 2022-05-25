package com.anytypeio.anytype.core_ui.features.editor.holders.other

import android.view.View
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.setBlockBackgroundColor
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder
import com.anytypeio.anytype.core_ui.features.editor.EditorTouchProcessor
import com.anytypeio.anytype.core_ui.features.editor.SupportCustomTouchProcessor
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

abstract class Divider(view: View) : BlockViewHolder(view),
    BlockViewHolder.IndentableHolder,
    BlockViewHolder.DragAndDropHolder,
    SupportCustomTouchProcessor {

    val root : View get() = itemView
    val divider: View get() = itemView.findViewById(R.id.divider)
    abstract val container: View

    override val editorTouchProcessor = EditorTouchProcessor(
        fallback = { e -> itemView.onTouchEvent(e) }
    )

    init {
        itemView.setOnTouchListener { v, e -> editorTouchProcessor.process(v, e) }
    }

    fun bind(
        id: String,
        item: BlockView.Indentable,
        isItemSelected: Boolean,
        clicked: (ListenerType) -> Unit,
        background: String?
    ) = with(itemView) {
        indentize(item)
        container.isSelected = isItemSelected
        applyBackground(background)
        setOnClickListener { clicked(ListenerType.DividerClick(id)) }
    }

    override fun indentize(item: BlockView.Indentable) {
        divider.updateLayoutParams<FrameLayout.LayoutParams> {
            marginStart = item.indent * dimen(R.dimen.indent)
        }
    }

    private fun applyBackground(background: String?) {
        root.setBlockBackgroundColor(background)
    }
}