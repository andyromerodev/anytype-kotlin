package com.anytypeio.anytype.features.sets.filter

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isSelected
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.anytypeio.anytype.R
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.domain.`object`.ObjectTypesProvider
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.dataview.interactor.SearchObjects
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig
import com.anytypeio.anytype.presentation.sets.ObjectSet
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.sets.filter.FilterViewModel
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.anytypeio.anytype.test_utils.utils.TestUtils
import com.anytypeio.anytype.ui.sets.modals.filter.ModifyFilterFromSelectedValueFragment
import com.anytypeio.anytype.utils.CoroutinesTestRule
import com.bartoszlipinski.disableanimationsrule.DisableAnimationsRule
import kotlinx.coroutines.flow.MutableStateFlow
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking

@RunWith(AndroidJUnit4::class)
@LargeTest
class ModifyStatusFilterTest {

    @get:Rule
    val animationsRule = DisableAnimationsRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Mock
    lateinit var repo: BlockRepository
    @Mock
    lateinit var gateway: Gateway
    @Mock
    lateinit var objectTypesProvider: ObjectTypesProvider

    @Mock
    lateinit var analytics: Analytics

    lateinit var updateDataViewViewer: UpdateDataViewViewer
    lateinit var searchObjects: SearchObjects
    lateinit var urlBuilder: UrlBuilder

    private val root = MockDataFactory.randomUuid()
    private val session = ObjectSetSession()
    private val state = MutableStateFlow(ObjectSet.init())
    private val dispatcher = Dispatcher.Default<Payload>()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        updateDataViewViewer = UpdateDataViewViewer(repo)
        searchObjects = SearchObjects(repo)
        urlBuilder = UrlBuilder(gateway)
        TestModifyFilterFromSelectedValueFragment.testVmFactory = FilterViewModel.Factory(
            objectSetState = state,
            session = session,
            updateDataViewViewer = updateDataViewViewer,
            dispatcher = dispatcher,
            searchObjects = searchObjects,
            urlBuilder = urlBuilder,
            objectTypesProvider = objectTypesProvider,
            analytics = analytics
        )
    }

    @Test
    fun shouldSelectSecondStatusAndApplyChangesOnClick() {

        val relationKey = MockDataFactory.randomUuid()
        val target = MockDataFactory.randomUuid()

        // Defining three different statuses:

        val option1 = Relation.Option(
            id = MockDataFactory.randomUuid(),
            text = "In progress",
            color = MockDataFactory.randomString()
        )

        val option2 = Relation.Option(
            id = MockDataFactory.randomUuid(),
            text = "In Testing",
            color = MockDataFactory.randomString()
        )

        val option3 = Relation.Option(
            id = MockDataFactory.randomUuid(),
            text = "Done",
            color = MockDataFactory.randomString()
        )

        val record: Map<String, Any?> = mapOf(
            ObjectSetConfig.ID_KEY to target,
            relationKey to emptyList<String>()
        )

        // Defining viewer containing one filter

        val filter = DVFilter(
            relationKey = relationKey,
            value = listOf(option1.id),
            condition = DVFilterCondition.EQUAL
        )

        val viewer = Block.Content.DataView.Viewer(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            filters = listOf(filter),
            sorts = emptyList(),
            viewerRelations = listOf(
                Block.Content.DataView.Viewer.ViewerRelation(
                    key = relationKey,
                    isVisible = true
                )
            ),
            type = Block.Content.DataView.Viewer.Type.values().random()
        )

        val relation = Relation(
            key = relationKey,
            defaultValue = null,
            isHidden = false,
            isReadOnly = false,
            isMulti = true,
            name = MockDataFactory.randomString(),
            source = Relation.Source.values().random(),
            format = Relation.Format.STATUS,
            selections = listOf(option1, option2, option3)
        )

        val dv = Block(
            id = MockDataFactory.randomUuid(),
            children = emptyList(),
            fields = Block.Fields.empty(),
            content = Block.Content.DataView(
                relations = listOf(relation),
                viewers = listOf(viewer),
                sources = listOf(
                    MockDataFactory.randomUuid()
                )
            )
        )

        state.value = ObjectSet(
            blocks = listOf(dv),
            viewerDb = mapOf(
                viewer.id to ObjectSet.ViewerData(
                    records = listOf(record),
                    total = 1
                )
            )
        )

        // Launching fragment

        launchFragment(
            bundleOf(
                ModifyFilterFromSelectedValueFragment.CTX_KEY to root,
                ModifyFilterFromSelectedValueFragment.IDX_KEY to 0,
                ModifyFilterFromSelectedValueFragment.RELATION_KEY to relationKey,
            )
        )

        // TESTING

        val rvMatcher = TestUtils.withRecyclerView(R.id.rvViewerFilterRecycler)

        // Checking names

        onView(rvMatcher.atPositionOnView(0, R.id.tvStatusName)).apply {
            check(matches(withText(option1.text)))
        }

        onView(rvMatcher.atPositionOnView(1, R.id.tvStatusName)).apply {
            check(matches(withText(option2.text)))
        }

        onView(rvMatcher.atPositionOnView(2, R.id.tvStatusName)).apply {
            check(matches(withText(option3.text)))
        }

        // Veryfing that only the first status is selected

        onView(rvMatcher.atPositionOnView(0, R.id.ivSelectStatusIcon)).apply {
            check(matches(isSelected()))
        }

        onView(rvMatcher.atPositionOnView(1, R.id.ivSelectStatusIcon)).apply {
            check(matches(not(isSelected())))
        }

        onView(rvMatcher.atPositionOnView(2, R.id.ivSelectStatusIcon)).apply {
            check(matches(not(isSelected())))
        }

        // Verifying that the selection counter is equal to 1

        onView(withId(R.id.tvOptionCount)).apply {
            check(matches(withText("1")))
        }

        // Performing click, in order to sellect second status

        onView(rvMatcher.atPositionOnView(1, R.id.ivSelectStatusIcon)).apply {
            perform(click())
        }

        // Veryfing that only the second status is selected

        onView(rvMatcher.atPositionOnView(0, R.id.ivSelectStatusIcon)).apply {
            check(matches(not(isSelected())))
        }

        onView(rvMatcher.atPositionOnView(1, R.id.ivSelectStatusIcon)).apply {
            check(matches((isSelected())))
        }

        onView(rvMatcher.atPositionOnView(2, R.id.ivSelectStatusIcon)).apply {
            check(matches(not(isSelected())))
        }

        // Verifying that the selection counter is still equal to 1

        onView(withId(R.id.tvOptionCount)).apply {
            check(matches(withText("1")))
        }

        // Performing a click to apply filter changes.

        onView(withId(R.id.btnBottomAction)).apply {
            perform(click())
        }

        // Verifying that viewer's filters are updated.

        verifyBlocking(repo, times(1)) {
            updateDataViewViewer(
                context = root,
                target = dv.id,
                viewer = viewer.copy(
                    filters = listOf(filter.copy(value = listOf(option2.id)))
                )
            )
        }
    }

    private fun launchFragment(args: Bundle): FragmentScenario<TestModifyFilterFromSelectedValueFragment> {
        return launchFragmentInContainer(
            fragmentArgs = args,
            themeResId = R.style.AppTheme
        )
    }
}