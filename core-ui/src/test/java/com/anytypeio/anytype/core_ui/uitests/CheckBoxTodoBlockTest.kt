package com.anytypeio.anytype.core_ui.uitests

import android.content.Context
import android.os.Build
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.anytypeio.anytype.test_utils.TestFragment
import com.anytypeio.anytype.test_utils.utils.onItemView
import com.anytypeio.anytype.test_utils.utils.performClick
import com.anytypeio.anytype.test_utils.utils.rVMatcher
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import com.anytypeio.anytype.test_utils.R as R_test


@RunWith(RobolectricTestRunner::class)
@Config(
    manifest = Config.NONE,
    sdk = [Build.VERSION_CODES.P],
    instrumentedPackages = [
        // required to access final members on androidx.loader.content.ModernAsyncTask
        "androidx.loader.content"
    ]
)
class CheckBoxTodoBlockTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    lateinit var scenario: FragmentScenario<TestFragment>

    @Before
    fun setUp() {
        context.setTheme(R.style.Theme_MaterialComponents)
        scenario = launchFragmentInContainer()
    }

    @Test
    fun `should not click - when locked`() {
        scenario.onFragment {
            val recycler = givenRecycler(it)
            val onTitleCheckboxClicked = mock<(BlockView.Title.Todo) -> Unit>()
            val adapter = givenAdapter(
                listOf(givenTitleTodo(BlockView.Mode.EDIT)),
                onTitleCheckboxClicked = onTitleCheckboxClicked
            )
            recycler.adapter = adapter

            R_test.id.recycler.rVMatcher().apply {
                onItemView(0, R.id.todoTitleCheckbox).performClick()
            }

            verify(onTitleCheckboxClicked).invoke(any())

            adapter.updateWithDiffUtil(listOf(givenTitleTodo(BlockView.Mode.READ)))

            R_test.id.recycler.rVMatcher().apply {
                onItemView(0, R.id.todoTitleCheckbox).performClick()
            }
            verifyNoMoreInteractions(onTitleCheckboxClicked)
        }
    }

    private fun givenTitleTodo(mode: BlockView.Mode) = BlockView.Title.Todo(
        id = MockDataFactory.randomUuid(),
        text = MockDataFactory.randomString(),
        mode = mode
    )

    private fun givenRecycler(it: Fragment): RecyclerView =
        it.view!!.findViewById<RecyclerView>(R_test.id.recycler).apply {
            layoutManager = LinearLayoutManager(context)
        }
}