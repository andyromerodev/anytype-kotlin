package com.anytypeio.anytype.core_ui.features.editor

import android.os.Build
import android.text.method.ArrowKeyMovementMethod
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.features.editor.holders.text.Paragraph
import com.anytypeio.anytype.core_ui.tools.CustomBetterLinkMovementMethod
import com.anytypeio.anytype.presentation.editor.editor.Markup
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_PARAGRAPH
import com.anytypeio.anytype.test_utils.MockDataFactory
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
class TextBlockSetTextTest : BlockAdapterTestSetup() {

    @Test
    fun `set empty text, empty marks`() {

        val paragraph = BlockView.Text.Paragraph(
            text = "",
            marks = listOf(),
            id = MockDataFactory.randomUuid(),
            isFocused = true
        )

        val adapter = buildAdapter(views = listOf(paragraph))
        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }
        val holder = adapter.onCreateViewHolder(recycler, HOLDER_PARAGRAPH)
        adapter.onBindViewHolder(holder, 0)
        check(holder is Paragraph)

        // TESTING

        val testMM = holder.content.movementMethod

        assertEquals(
            expected = ArrowKeyMovementMethod::class.java,
            actual = testMM::class.java
        )
    }

    @Test
    fun `set not empty text, empty marks`() {

        val paragraph = BlockView.Text.Paragraph(
            text = "text",
            marks = listOf(),
            id = MockDataFactory.randomUuid(),
            isFocused = true
        )

        val adapter = buildAdapter(views = listOf(paragraph))
        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }
        val holder = adapter.onCreateViewHolder(recycler, HOLDER_PARAGRAPH)
        adapter.onBindViewHolder(holder, 0)
        check(holder is Paragraph)

        // TESTING

        val testMM = holder.content.movementMethod

        assertEquals(
            expected = ArrowKeyMovementMethod::class.java,
            actual = testMM::class.java
        )
    }

    @Test
    fun `set not empty text, marks without links or mentions`() {

        val paragraph = BlockView.Text.Paragraph(
            text = "text",
            marks = listOf(
                Markup.Mark.Bold(
                    from = 0,
                    to = 4
                )
            ),
            id = MockDataFactory.randomUuid(),
            isFocused = true
        )

        val adapter = buildAdapter(views = listOf(paragraph))
        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }
        val holder = adapter.onCreateViewHolder(recycler, HOLDER_PARAGRAPH)
        adapter.onBindViewHolder(holder, 0)
        check(holder is Paragraph)

        // TESTING

        val testMM = holder.content.movementMethod

        assertEquals(
            expected = ArrowKeyMovementMethod::class.java,
            actual = testMM::class.java
        )
    }

    @Test
    fun `set not empty text, marks with link`() {

        val paragraph = BlockView.Text.Paragraph(
            text = "text with link",
            marks = listOf(
                Markup.Mark.Link(
                    from = 10,
                    to = 14,
                    param = "link"
                )
            ),
            id = MockDataFactory.randomUuid(),
            isFocused = true
        )

        val adapter = buildAdapter(views = listOf(paragraph))
        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }
        val holder = adapter.onCreateViewHolder(recycler, HOLDER_PARAGRAPH)
        adapter.onBindViewHolder(holder, 0)
        check(holder is Paragraph)

        // TESTING

        val testMM = holder.content.movementMethod

        assertEquals(
            expected = CustomBetterLinkMovementMethod::class.java,
            actual = testMM::class.java
        )
    }

    @Test
    fun `set not empty text, marks with mention`() {

        val paragraph = BlockView.Text.Paragraph(
            text = "text with mention",
            marks = listOf(
                Markup.Mark.Mention.Base(
                    from = 10,
                    to = 14,
                    param = "mention"
                )
            ),
            id = MockDataFactory.randomUuid(),
            isFocused = true
        )

        val adapter = buildAdapter(views = listOf(paragraph))
        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }
        val holder = adapter.onCreateViewHolder(recycler, HOLDER_PARAGRAPH)
        adapter.onBindViewHolder(holder, 0)
        check(holder is Paragraph)

        // TESTING

        val testMM = holder.content.movementMethod

        assertEquals(
            expected = CustomBetterLinkMovementMethod::class.java,
            actual = testMM::class.java
        )
    }
}