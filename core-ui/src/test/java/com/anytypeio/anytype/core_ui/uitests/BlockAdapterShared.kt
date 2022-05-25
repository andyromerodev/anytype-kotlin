package com.anytypeio.anytype.core_ui.uitests

import android.text.Editable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_ui.BlockAdapterTest
import com.anytypeio.anytype.core_ui.features.editor.BlockAdapter
import com.anytypeio.anytype.core_ui.features.editor.DragAndDropAdapterDelegate
import com.anytypeio.anytype.core_ui.features.editor.EditorDragAndDropListener
import com.anytypeio.anytype.core_ui.tools.ClipboardInterceptor
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import java.util.*

private val clipboardInterceptor: ClipboardInterceptor = object : ClipboardInterceptor {
    override fun onClipboardAction(action: ClipboardInterceptor.Action) {}
    override fun onUrlPasted(url: Url) {}
}

fun givenAdapter(
    views: List<BlockView>,
    onSplitLineEnterClicked: (String, Editable, IntRange) -> Unit = { _, _, _ -> },
    onFocusChanged: (String, Boolean) -> Unit = { _, _ -> },
    onTitleCheckboxClicked: (BlockView.Title.Todo) -> Unit = {},
    onTitleBlockTextChanged: (Id, String) -> Unit = { _, _ -> },
    onTextChanged: (String, Editable) -> Unit = { _, _ -> },
    lifecycle: Lifecycle = BlockAdapterTest.TestLifecycle(),
): BlockAdapter {
    return BlockAdapter(
        restore = LinkedList(),
        blocks = views,
        onNonEmptyBlockBackspaceClicked = { _, _ -> },
        onEmptyBlockBackspaceClicked = {},
        onSplitLineEnterClicked = onSplitLineEnterClicked,
        onSplitDescription = { _, _, _ -> },
        onTextChanged = onTextChanged,
        onCheckboxClicked = {},
        onTitleCheckboxClicked = onTitleCheckboxClicked,
        onFocusChanged = onFocusChanged,
        onSelectionChanged = { _, _ -> },
        onTextInputClicked = {},
        onPageIconClicked = {},
        onTogglePlaceholderClicked = {},
        onToggleClicked = {},
        onTextBlockTextChanged = {},
        onTitleBlockTextChanged = onTitleBlockTextChanged,
        onTitleTextInputClicked = {},
        onClickListener = {},
        clipboardInterceptor = clipboardInterceptor,
        onMentionEvent = {},
        onBackPressedCallback = { false },
        onCoverClicked = {},
        onSlashEvent = {},
        onKeyPressedEvent = {},
        onDragListener = EditorDragAndDropListener(
            onDragEnded = {},
            onDragExited = {},
            onDragLocation = { _, _ -> },
            onDrop = { _, _ -> }
        ),
        onDragAndDropTrigger = { _, _ -> false },
        dragAndDropSelector = DragAndDropAdapterDelegate(),
        lifecycle = lifecycle
    )
}

class TestLifecycle(
    val observers: MutableList<LifecycleObserver> = mutableListOf()
) : Lifecycle() {
    override fun addObserver(observer: LifecycleObserver) {
        observers.add(observer)
    }

    override fun removeObserver(observer: LifecycleObserver) {}
    override fun getCurrentState() = State.DESTROYED
}