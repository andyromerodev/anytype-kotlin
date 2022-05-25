package com.anytypeio.anytype.core_ui.features.editor.holders.media

import android.text.Spannable
import android.text.SpannableString
import android.text.format.Formatter
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.View
import android.widget.TextView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.SearchHighlightSpan
import com.anytypeio.anytype.core_ui.common.SearchTargetHighlightSpan
import com.anytypeio.anytype.core_ui.databinding.ItemBlockFileBinding
import com.anytypeio.anytype.core_ui.extensions.color
import com.anytypeio.anytype.core_ui.extensions.getMimeIcon
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ext.indentize
import com.anytypeio.anytype.core_utils.ext.removeSpans
import com.anytypeio.anytype.presentation.editor.editor.Markup
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

class File(val binding: ItemBlockFileBinding) : Media(binding.root) {

    override val container = binding.root
    override val root: View = itemView
    override val clickContainer: View = binding.text
    private val icon = binding.graphic
    private val name = binding.text
    private val guideline = binding.guideline

    init {
        clickContainer.setOnTouchListener { v, e -> editorTouchProcessor.process(v, e) }
    }

    fun bind(item: BlockView.Media.File, clicked: (ListenerType) -> Unit) {
        super.bind(item, clicked)
        name.enableReadMode()
        if (item.size != null && item.name != null) {
            val size = Formatter.formatFileSize(itemView.context, item.size!!)
            val spannable = SpannableString("${item.name}  $size")
            val start = item.name!!.length + 2
            val end = item.name!!.length + 2 + size.length
            spannable.setSpan(
                RelativeSizeSpan(0.87f),
                start,
                end,
                Markup.DEFAULT_SPANNABLE_FLAG
            )
            spannable.setSpan(
                ForegroundColorSpan(itemView.context.color(R.color.text_secondary)),
                start,
                end,
                Markup.DEFAULT_SPANNABLE_FLAG
            )
            name.setText(spannable, TextView.BufferType.SPANNABLE)
        } else {
            name.setText(item.name, TextView.BufferType.SPANNABLE)
        }

        applySearchHighlight(item)

        val mimeIcon = item.mime.getMimeIcon(item.name)
        icon.setImageResource(mimeIcon)

        applyBackground(item.backgroundColor)
    }

    private fun applySearchHighlight(item: BlockView.Searchable) {
        item.searchFields.find { it.key == BlockView.Searchable.Field.DEFAULT_SEARCH_FIELD_KEY }
            ?.let { field ->
                applySearchHighlight(field, name)
            } ?: clearSearchHighlights()
    }

    private fun applySearchHighlight(field: BlockView.Searchable.Field, input: TextView) {
        input.editableText.removeSpans<SearchHighlightSpan>()
        input.editableText.removeSpans<SearchTargetHighlightSpan>()
        field.highlights.forEach { highlight ->
            input.editableText.setSpan(
                SearchHighlightSpan(),
                highlight.first,
                highlight.last,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        if (field.isTargeted) {
            input.editableText.setSpan(
                SearchTargetHighlightSpan(),
                field.target.first,
                field.target.last,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    private fun clearSearchHighlights() {
        name.editableText.removeSpans<SearchHighlightSpan>()
        name.editableText.removeSpans<SearchTargetHighlightSpan>()
    }

    override fun onMediaBlockClicked(item: BlockView.Media, clicked: (ListenerType) -> Unit) {
        clicked(ListenerType.File.View(item.id))
    }

    override fun indentize(item: BlockView.Indentable) {
        guideline.setGuidelineBegin(item.indent * dimen(R.dimen.indent))
    }

    override fun select(isSelected: Boolean) {
        itemView.isSelected = isSelected
    }

    override fun processChangePayload(payloads: List<BlockViewDiffUtil.Payload>, item: BlockView) {
        super.processChangePayload(payloads, item)
        check(item is BlockView.Media.File)
        payloads.forEach { payload ->
            if (payload.isSearchHighlightChanged) {
                applySearchHighlight(item)
            }
            if (payload.isBackgroundColorChanged) {
                applyBackground(item.backgroundColor)
            }
        }
    }
}