package com.anytypeio.anytype.core_ui.features.editor

import android.os.Build
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.features.editor.holders.text.*
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_BULLET
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_CHECKBOX
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_HEADER_ONE
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_HEADER_THREE
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_HEADER_TWO
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_HIGHLIGHT
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_NUMBERED
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_PARAGRAPH
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_TOGGLE
import com.anytypeio.anytype.test_utils.MockDataFactory
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
class BlockAdapterTextBindingTest : BlockAdapterTestSetup() {

    @Test
    fun `should not trigger text changed event when binding twice paragraph view holder`() {

        var textChangedTriggerCount = 0

        val a = BlockView.Text.Paragraph(
            id = MockDataFactory.randomUuid(),
            text = MockDataFactory.randomString(),
        )

        val views = listOf(a)

        val adapter = buildAdapter(
            views = views,
            onTextBlockTextChanged = { textChangedTriggerCount += 1 }
        )

        val recycler = RecyclerView(context).apply {
            this.layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }

        val holder = adapter.onCreateViewHolder(recycler, HOLDER_PARAGRAPH)

        check(holder is Paragraph)

        // TESTING

        adapter.onBindViewHolder(holder, 0)

        assertEquals(
            expected = 0,
            actual = textChangedTriggerCount
        )

        assertEquals(
            expected = a.text,
            actual = holder.content.text.toString()
        )

        adapter.onBindViewHolder(holder, 0)

        assertEquals(
            expected = 0,
            actual = textChangedTriggerCount
        )

        assertEquals(
            expected = a.text,
            actual = holder.content.text.toString()
        )
    }

    @Test
    fun `should not trigger text changed event when binding twice header1 view holder`() {

        var textChangedTriggerCount = 0

        val a = BlockView.Text.Header.One(
            id = MockDataFactory.randomUuid(),
            text = MockDataFactory.randomString(),
        )

        val views = listOf(a)

        val adapter = buildAdapter(
            views = views,
            onTextBlockTextChanged = { textChangedTriggerCount += 1 }
        )

        val recycler = RecyclerView(context).apply {
            this.layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }

        val holder = adapter.onCreateViewHolder(recycler, HOLDER_HEADER_ONE)

        check(holder is HeaderOne)

        // TESTING

        adapter.onBindViewHolder(holder, 0)

        assertEquals(
            expected = 0,
            actual = textChangedTriggerCount
        )

        assertEquals(
            expected = a.text,
            actual = holder.content.text.toString()
        )

        adapter.onBindViewHolder(holder, 0)

        assertEquals(
            expected = 0,
            actual = textChangedTriggerCount
        )

        assertEquals(
            expected = a.text,
            actual = holder.content.text.toString()
        )
    }

    @Test
    fun `should not trigger text changed event when binding twice header2 view holder`() {

        var textChangedTriggerCount = 0

        val a = BlockView.Text.Header.Two(
            id = MockDataFactory.randomUuid(),
            text = MockDataFactory.randomString(),
        )

        val views = listOf(a)

        val adapter = buildAdapter(
            views = views,
            onTextBlockTextChanged = { textChangedTriggerCount += 1 }
        )

        val recycler = RecyclerView(context).apply {
            this.layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }

        val holder = adapter.onCreateViewHolder(recycler, HOLDER_HEADER_TWO)

        check(holder is HeaderTwo)

        // TESTING

        adapter.onBindViewHolder(holder, 0)

        assertEquals(
            expected = 0,
            actual = textChangedTriggerCount
        )

        assertEquals(
            expected = a.text,
            actual = holder.content.text.toString()
        )

        adapter.onBindViewHolder(holder, 0)

        assertEquals(
            expected = 0,
            actual = textChangedTriggerCount
        )

        assertEquals(
            expected = a.text,
            actual = holder.content.text.toString()
        )
    }

    @Test
    fun `should not trigger text changed event when binding twice header3 view holder`() {

        var textChangedTriggerCount = 0

        val a = BlockView.Text.Header.Three(
            id = MockDataFactory.randomUuid(),
            text = MockDataFactory.randomString(),
        )

        val views = listOf(a)

        val adapter = buildAdapter(
            views = views,
            onTextBlockTextChanged = { textChangedTriggerCount += 1 }
        )

        val recycler = RecyclerView(context).apply {
            this.layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }

        val holder = adapter.onCreateViewHolder(recycler, HOLDER_HEADER_THREE)

        check(holder is HeaderThree)

        // TESTING

        adapter.onBindViewHolder(holder, 0)

        assertEquals(
            expected = 0,
            actual = textChangedTriggerCount
        )

        assertEquals(
            expected = a.text,
            actual = holder.content.text.toString()
        )

        adapter.onBindViewHolder(holder, 0)

        assertEquals(
            expected = 0,
            actual = textChangedTriggerCount
        )

        assertEquals(
            expected = a.text,
            actual = holder.content.text.toString()
        )
    }

    @Test
    fun `should not trigger text changed event when binding twice highlight view holder`() {

        var textChangedTriggerCount = 0

        val a = BlockView.Text.Highlight(
            id = MockDataFactory.randomUuid(),
            text = MockDataFactory.randomString(),
        )

        val views = listOf(a)

        val adapter = buildAdapter(
            views = views,
            onTextBlockTextChanged = { textChangedTriggerCount += 1 }
        )

        val recycler = RecyclerView(context).apply {
            this.layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }

        val holder = adapter.onCreateViewHolder(recycler, HOLDER_HIGHLIGHT)

        check(holder is Highlight)

        // TESTING

        adapter.onBindViewHolder(holder, 0)

        assertEquals(
            expected = 0,
            actual = textChangedTriggerCount
        )

        assertEquals(
            expected = a.text,
            actual = holder.content.text.toString()
        )

        adapter.onBindViewHolder(holder, 0)

        assertEquals(
            expected = 0,
            actual = textChangedTriggerCount
        )

        assertEquals(
            expected = a.text,
            actual = holder.content.text.toString()
        )
    }

    @Test
    fun `should not trigger text changed event when binding twice checkbox view holder`() {

        var textChangedTriggerCount = 0

        val a = BlockView.Text.Checkbox(
            id = MockDataFactory.randomUuid(),
            text = MockDataFactory.randomString(),
        )

        val views = listOf(a)

        val adapter = buildAdapter(
            views = views,
            onTextBlockTextChanged = { textChangedTriggerCount += 1 }
        )

        val recycler = RecyclerView(context).apply {
            this.layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }

        val holder = adapter.onCreateViewHolder(recycler, HOLDER_CHECKBOX)

        check(holder is Checkbox)

        // TESTING

        adapter.onBindViewHolder(holder, 0)

        assertEquals(
            expected = 0,
            actual = textChangedTriggerCount
        )

        assertEquals(
            expected = a.text,
            actual = holder.content.text.toString()
        )

        adapter.onBindViewHolder(holder, 0)

        assertEquals(
            expected = 0,
            actual = textChangedTriggerCount
        )

        assertEquals(
            expected = a.text,
            actual = holder.content.text.toString()
        )
    }

    @Test
    fun `should not trigger text changed event when binding twice bullet view holder`() {

        var textChangedTriggerCount = 0

        val a = BlockView.Text.Bulleted(
            id = MockDataFactory.randomUuid(),
            text = MockDataFactory.randomString(),
        )

        val views = listOf(a)

        val adapter = buildAdapter(
            views = views,
            onTextBlockTextChanged = { textChangedTriggerCount += 1 }
        )

        val recycler = RecyclerView(context).apply {
            this.layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }

        val holder = adapter.onCreateViewHolder(recycler, HOLDER_BULLET)

        check(holder is Bulleted)

        // TESTING

        adapter.onBindViewHolder(holder, 0)

        assertEquals(
            expected = 0,
            actual = textChangedTriggerCount
        )

        assertEquals(
            expected = a.text,
            actual = holder.content.text.toString()
        )

        adapter.onBindViewHolder(holder, 0)

        assertEquals(
            expected = 0,
            actual = textChangedTriggerCount
        )

        assertEquals(
            expected = a.text,
            actual = holder.content.text.toString()
        )
    }

    @Test
    fun `should not trigger text changed event when binding twice numbered view holder`() {

        var textChangedTriggerCount = 0

        val a = BlockView.Text.Numbered(
            id = MockDataFactory.randomUuid(),
            text = MockDataFactory.randomString(),
            number = 1
        )

        val views = listOf(a)

        val adapter = buildAdapter(
            views = views,
            onTextBlockTextChanged = { textChangedTriggerCount += 1 }
        )

        val recycler = RecyclerView(context).apply {
            this.layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }

        val holder = adapter.onCreateViewHolder(recycler, HOLDER_NUMBERED)

        check(holder is Numbered)

        // TESTING

        adapter.onBindViewHolder(holder, 0)

        assertEquals(
            expected = 0,
            actual = textChangedTriggerCount
        )

        assertEquals(
            expected = a.text,
            actual = holder.content.text.toString()
        )

        adapter.onBindViewHolder(holder, 0)

        assertEquals(
            expected = 0,
            actual = textChangedTriggerCount
        )

        assertEquals(
            expected = a.text,
            actual = holder.content.text.toString()
        )
    }

    @Test
    fun `should not trigger text changed event when binding twice toggle view holder`() {

        var textChangedTriggerCount = 0

        val a = BlockView.Text.Toggle(
            id = MockDataFactory.randomUuid(),
            text = MockDataFactory.randomString()
        )

        val views = listOf(a)

        val adapter = buildAdapter(
            views = views,
            onTextBlockTextChanged = { textChangedTriggerCount += 1 }
        )

        val recycler = RecyclerView(context).apply {
            this.layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }

        val holder = adapter.onCreateViewHolder(recycler, HOLDER_TOGGLE)

        check(holder is Toggle)

        // TESTING

        adapter.onBindViewHolder(holder, 0)

        assertEquals(
            expected = 0,
            actual = textChangedTriggerCount
        )

        assertEquals(
            expected = a.text,
            actual = holder.content.text.toString()
        )

        adapter.onBindViewHolder(holder, 0)

        assertEquals(
            expected = 0,
            actual = textChangedTriggerCount
        )

        assertEquals(
            expected = a.text,
            actual = holder.content.text.toString()
        )
    }
}