package com.anytypeio.anytype.core_ui.features.editor.holders.error

import android.view.View
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ext.indentize
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

class VideoError(view: View) : MediaError(view) {

    override val root: View = itemView

    override fun errorClick(item: BlockView.Error, clicked: (ListenerType) -> Unit) {
        clicked(ListenerType.Video.Error(item.id))
    }

    override fun indentize(item: BlockView.Indentable) {
        root.indentize(
            indent = item.indent,
            defIndent = dimen(R.dimen.indent),
            margin = dimen(R.dimen.bookmark_default_margin_start)
        )
    }

    override fun select(isSelected: Boolean) {
        root.isSelected = isSelected
    }
}