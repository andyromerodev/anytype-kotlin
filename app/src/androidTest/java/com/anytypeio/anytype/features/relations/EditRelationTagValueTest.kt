package com.anytypeio.anytype.features.relations

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.domain.`object`.ObjectTypesProvider
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.dataview.interactor.AddDataViewRelationOption
import com.anytypeio.anytype.domain.dataview.interactor.AddTagToDataViewRecord
import com.anytypeio.anytype.domain.dataview.interactor.RemoveStatusFromDataViewRecord
import com.anytypeio.anytype.domain.dataview.interactor.RemoveTagFromDataViewRecord
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewRecord
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.relations.AddFileToRecord
import com.anytypeio.anytype.presentation.editor.editor.ThemeColor
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig
import com.anytypeio.anytype.presentation.relations.providers.DataViewObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.DataViewObjectValueProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectDetailProvider
import com.anytypeio.anytype.presentation.sets.ObjectSet
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.sets.RelationValueDVViewModel
import com.anytypeio.anytype.presentation.util.CopyFileToCacheDirectory
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.anytypeio.anytype.test_utils.utils.checkHasText
import com.anytypeio.anytype.test_utils.utils.checkHasTextColor
import com.anytypeio.anytype.test_utils.utils.checkIsDisplayed
import com.anytypeio.anytype.test_utils.utils.checkIsNotDisplayed
import com.anytypeio.anytype.test_utils.utils.checkIsRecyclerSize
import com.anytypeio.anytype.test_utils.utils.matchView
import com.anytypeio.anytype.test_utils.utils.onItemView
import com.anytypeio.anytype.test_utils.utils.performClick
import com.anytypeio.anytype.test_utils.utils.rVMatcher
import com.anytypeio.anytype.ui.relations.RelationValueBaseFragment
import com.anytypeio.anytype.utils.CoroutinesTestRule
import com.bartoszlipinski.disableanimationsrule.DisableAnimationsRule
import kotlinx.coroutines.flow.MutableStateFlow
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
class EditRelationTagValueTest {

    @Mock
    lateinit var repo: BlockRepository

    @Mock
    lateinit var gateway: Gateway

    @Mock
    lateinit var dispatcher: Dispatcher<Payload>

    @Mock
    lateinit var copyFileToCacheDirectory: CopyFileToCacheDirectory

    private lateinit var addRelationOption: AddDataViewRelationOption
    private lateinit var removeTagFromDataViewRecord: RemoveTagFromDataViewRecord
    private lateinit var removeStatusFromDataViewRecord: RemoveStatusFromDataViewRecord
    private lateinit var updateDataViewRecord: UpdateDataViewRecord
    private lateinit var addTagToDataViewRecord: AddTagToDataViewRecord
    private lateinit var updateDetail: UpdateDetail
    private lateinit var urlBuilder: UrlBuilder
    private lateinit var addFileToRecord: AddFileToRecord

    @get:Rule
    val animationsRule = DisableAnimationsRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    private val ctx = MockDataFactory.randomUuid()
    private val state = MutableStateFlow(ObjectSet.init())
    private val session = ObjectSetSession()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        addRelationOption = AddDataViewRelationOption(repo)
        addTagToDataViewRecord = AddTagToDataViewRecord(repo)
        removeTagFromDataViewRecord = RemoveTagFromDataViewRecord(repo)
        removeStatusFromDataViewRecord = RemoveStatusFromDataViewRecord(repo)
        updateDataViewRecord = UpdateDataViewRecord(repo)
        updateDetail = UpdateDetail(repo)
        urlBuilder = UrlBuilder(gateway)
        addFileToRecord = AddFileToRecord(repo)
        TestRelationValueDVFragment.testVmFactory = RelationValueDVViewModel.Factory(
            relations = DataViewObjectRelationProvider(state),
            values = DataViewObjectValueProvider(state, session),
            details = object : ObjectDetailProvider {
                override fun provide(): Map<Id, Block.Fields> = state.value.details
            },
            types = object : ObjectTypesProvider {
                override fun set(objectTypes: List<ObjectType>) {}
                override fun get(): List<ObjectType> = state.value.objectTypes
            },
            removeTagFromRecord = removeTagFromDataViewRecord,
            removeStatusFromDataViewRecord = removeStatusFromDataViewRecord,
            urlBuilder = urlBuilder,
            updateDataViewRecord = updateDataViewRecord,
            addFileToRecord = addFileToRecord,
            copyFileToCache = copyFileToCacheDirectory
        )
    }

    @Test
    fun shouldRenderTwoTagsInReadThenInEditMode() {

        // SETUP

        val option1Color = ThemeColor.values().random()
        val option2Color = ThemeColor.values().random()

        val option1 = Relation.Option(
            id = MockDataFactory.randomUuid(),
            text = "Architect",
            color = option1Color.title
        )

        val option2 = Relation.Option(
            id = MockDataFactory.randomUuid(),
            text = "Manager",
            color = option2Color.title
        )

        val option3 = Relation.Option(
            id = MockDataFactory.randomUuid(),
            text = "Developer",
            color = ""
        )

        val relationKey = MockDataFactory.randomUuid()
        val target = MockDataFactory.randomUuid()

        val record: Map<String, Any?> = mapOf(
            ObjectSetConfig.ID_KEY to target,
            relationKey to listOf(option2.id, option3.id)
        )

        val viewer = Block.Content.DataView.Viewer(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            filters = emptyList(),
            sorts = emptyList(),
            viewerRelations = emptyList(),
            type = Block.Content.DataView.Viewer.Type.GRID
        )

        state.value = ObjectSet(
            blocks = listOf(
                Block(
                    id = MockDataFactory.randomUuid(),
                    children = emptyList(),
                    fields = Block.Fields.empty(),
                    content = Block.Content.DataView(
                        relations = listOf(
                            Relation(
                                key = relationKey,
                                defaultValue = null,
                                isHidden = false,
                                isReadOnly = false,
                                isMulti = true,
                                name = "Roles",
                                source = Relation.Source.values().random(),
                                format = Relation.Format.TAG,
                                selections = listOf(option1, option2, option3)
                            )
                        ),
                        viewers = listOf(viewer),
                        sources = listOf(
                            MockDataFactory.randomUuid()
                        )
                    )
                )
            ),
            viewerDb = mapOf(
                viewer.id to ObjectSet.ViewerData(
                    records = listOf(record),
                    total = 1
                )
            )
        )

        // TESTING

        launchFragment(
            bundleOf(
                RelationValueBaseFragment.CTX_KEY to ctx,
                RelationValueBaseFragment.RELATION_KEY to relationKey,
                RelationValueBaseFragment.TARGET_KEY to target
            )
        )

        val editOrDoneBtn = R.id.btnEditOrDone.matchView()

        editOrDoneBtn.checkHasText(R.string.edit)

        with(R.id.recycler.rVMatcher()) {
            onItemView(0, R.id.tvTagName).checkHasText(option2.text)
            onItemView(0, R.id.tvTagName).checkHasTextColor(option2Color.text)
            onItemView(0, R.id.btnRemoveTag).checkIsNotDisplayed()
            onItemView(0, R.id.btnDragAndDropTag).checkIsNotDisplayed()
            onItemView(1, R.id.tvTagName).checkHasText(option3.text)
            onItemView(1, R.id.btnRemoveTag).checkIsNotDisplayed()
            onItemView(1, R.id.btnDragAndDropTag).checkIsNotDisplayed()
            checkIsRecyclerSize(2)
        }

        editOrDoneBtn.performClick()

        editOrDoneBtn.checkHasText(R.string.done)

        with(R.id.recycler.rVMatcher()) {
            onItemView(0, R.id.tvTagName).checkHasText(option2.text)
            onItemView(0, R.id.tvTagName).checkHasTextColor(option2Color.text)
            onItemView(0, R.id.btnRemoveTag).checkIsDisplayed()
            onItemView(0, R.id.btnDragAndDropTag).checkIsDisplayed()
            onItemView(1, R.id.tvTagName).checkHasText(option3.text)
            onItemView(1, R.id.btnRemoveTag).checkIsDisplayed()
            onItemView(1, R.id.btnDragAndDropTag).checkIsDisplayed()
            checkIsRecyclerSize(2)
        }
    }

    @Test
    fun shouldRequestRemovingLastTagWhenRemoveButtonPressed() {

        // SETUP

        val option1Color = ThemeColor.values().random()
        val option2Color = ThemeColor.values().random()

        val option1 = Relation.Option(
            id = MockDataFactory.randomUuid(),
            text = "Architect",
            color = option1Color.title
        )

        val option2 = Relation.Option(
            id = MockDataFactory.randomUuid(),
            text = "Manager",
            color = option2Color.title
        )

        val option3 = Relation.Option(
            id = MockDataFactory.randomUuid(),
            text = "Developer",
            color = ""
        )

        val relationKey = MockDataFactory.randomUuid()
        val target = MockDataFactory.randomUuid()

        val record: Map<String, Any?> = mapOf(
            ObjectSetConfig.ID_KEY to target,
            relationKey to listOf(option2.id, option3.id)
        )

        val viewer = Block.Content.DataView.Viewer(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            filters = emptyList(),
            sorts = emptyList(),
            viewerRelations = emptyList(),
            type = Block.Content.DataView.Viewer.Type.GRID
        )

        val dv = Block(
            id = MockDataFactory.randomUuid(),
            children = emptyList(),
            fields = Block.Fields.empty(),
            content = Block.Content.DataView(
                relations = listOf(
                    Relation(
                        key = relationKey,
                        defaultValue = null,
                        isHidden = false,
                        isReadOnly = false,
                        isMulti = true,
                        name = "Roles",
                        source = Relation.Source.values().random(),
                        format = Relation.Format.TAG,
                        selections = listOf(option1, option2, option3)
                    )
                ),
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

        // TESTING

        launchFragment(
            bundleOf(
                RelationValueBaseFragment.CTX_KEY to ctx,
                RelationValueBaseFragment.DATAVIEW_KEY to dv.id,
                RelationValueBaseFragment.VIEWER_KEY to viewer.id,
                RelationValueBaseFragment.RELATION_KEY to relationKey,
                RelationValueBaseFragment.TARGET_KEY to target
            )
        )

        R.id.btnEditOrDone.performClick()

        val rvMatcher = R.id.recycler.rVMatcher()

        rvMatcher.onItemView(1, R.id.btnRemoveTag).performClick()

        verifyBlocking(repo, times(1)) {
            updateDataViewRecord(
                context = ctx,
                target = dv.id,
                record = target,
                values = mapOf(
                    ObjectSetConfig.ID_KEY to target,
                    relationKey to listOf(option2.id)
                )
            )
        }
    }

    private fun launchFragment(args: Bundle): FragmentScenario<TestRelationValueDVFragment> {
        return launchFragmentInContainer<TestRelationValueDVFragment>(
            fragmentArgs = args,
            themeResId = R.style.AppTheme
        )
    }
}