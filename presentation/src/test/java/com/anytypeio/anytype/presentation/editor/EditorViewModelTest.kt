package com.anytypeio.anytype.presentation.editor

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.core_models.SmartBlockType
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.domain.`object`.ObjectTypesProvider
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.base.Result
import com.anytypeio.anytype.domain.block.UpdateDivider
import com.anytypeio.anytype.domain.block.interactor.CreateBlock
import com.anytypeio.anytype.domain.block.interactor.DuplicateBlock
import com.anytypeio.anytype.domain.block.interactor.MergeBlocks
import com.anytypeio.anytype.domain.block.interactor.Move
import com.anytypeio.anytype.domain.block.interactor.RemoveLinkMark
import com.anytypeio.anytype.domain.block.interactor.ReplaceBlock
import com.anytypeio.anytype.domain.block.interactor.SetObjectType
import com.anytypeio.anytype.domain.block.interactor.SplitBlock
import com.anytypeio.anytype.domain.block.interactor.TurnIntoDocument
import com.anytypeio.anytype.domain.block.interactor.TurnIntoStyle
import com.anytypeio.anytype.domain.block.interactor.UnlinkBlocks
import com.anytypeio.anytype.domain.block.interactor.UpdateAlignment
import com.anytypeio.anytype.domain.block.interactor.UpdateBackgroundColor
import com.anytypeio.anytype.domain.block.interactor.UpdateBlocksMark
import com.anytypeio.anytype.domain.block.interactor.UpdateCheckbox
import com.anytypeio.anytype.domain.block.interactor.UpdateFields
import com.anytypeio.anytype.domain.block.interactor.UpdateLinkMarks
import com.anytypeio.anytype.domain.block.interactor.UpdateText
import com.anytypeio.anytype.domain.block.interactor.UpdateTextColor
import com.anytypeio.anytype.domain.block.interactor.UpdateTextStyle
import com.anytypeio.anytype.domain.block.interactor.UploadBlock
import com.anytypeio.anytype.domain.block.interactor.sets.CreateObjectSet
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.clipboard.Copy
import com.anytypeio.anytype.domain.clipboard.Paste
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.cover.SetDocCoverImage
import com.anytypeio.anytype.domain.dataview.interactor.GetCompatibleObjectTypes
import com.anytypeio.anytype.domain.dataview.interactor.SearchObjects
import com.anytypeio.anytype.domain.dataview.interactor.SetRelationKey
import com.anytypeio.anytype.domain.download.DownloadFile
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.icon.SetDocumentImageIcon
import com.anytypeio.anytype.domain.launch.GetDefaultEditorType
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.SetObjectIsArchived
import com.anytypeio.anytype.domain.page.CloseBlock
import com.anytypeio.anytype.domain.page.CreateDocument
import com.anytypeio.anytype.domain.page.CreateNewDocument
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.domain.page.CreatePage
import com.anytypeio.anytype.domain.page.OpenPage
import com.anytypeio.anytype.domain.page.Redo
import com.anytypeio.anytype.domain.page.Undo
import com.anytypeio.anytype.domain.page.UpdateTitle
import com.anytypeio.anytype.domain.page.bookmark.CreateBookmark
import com.anytypeio.anytype.domain.page.bookmark.SetupBookmark
import com.anytypeio.anytype.domain.sets.FindObjectSetForType
import com.anytypeio.anytype.domain.status.InterceptThreadStatus
import com.anytypeio.anytype.domain.unsplash.DownloadUnsplashImage
import com.anytypeio.anytype.domain.unsplash.UnsplashRepository
import com.anytypeio.anytype.presentation.MockBlockFactory
import com.anytypeio.anytype.presentation.common.Action
import com.anytypeio.anytype.presentation.common.Delegator
import com.anytypeio.anytype.presentation.editor.cover.CoverImageHashProvider
import com.anytypeio.anytype.presentation.editor.editor.BlockDimensions
import com.anytypeio.anytype.presentation.editor.editor.Command
import com.anytypeio.anytype.presentation.editor.editor.Interactor
import com.anytypeio.anytype.presentation.editor.editor.InternalDetailModificationManager
import com.anytypeio.anytype.presentation.editor.editor.Markup
import com.anytypeio.anytype.presentation.editor.editor.Orchestrator
import com.anytypeio.anytype.presentation.editor.editor.ViewState
import com.anytypeio.anytype.presentation.editor.editor.actions.ActionItemType
import com.anytypeio.anytype.presentation.editor.editor.control.ControlPanelState
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.model.UiBlock
import com.anytypeio.anytype.presentation.editor.editor.pattern.DefaultPatternMatcher
import com.anytypeio.anytype.presentation.editor.editor.styling.StyleToolbarState
import com.anytypeio.anytype.presentation.editor.editor.styling.StylingEvent
import com.anytypeio.anytype.presentation.editor.render.DefaultBlockViewRenderer
import com.anytypeio.anytype.presentation.editor.selection.SelectionStateHolder
import com.anytypeio.anytype.presentation.editor.template.EditorTemplateDelegate
import com.anytypeio.anytype.presentation.editor.toggle.ToggleStateHolder
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.util.CopyFileToCacheDirectory
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.presentation.util.TXT
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.jraska.livedata.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.verifyZeroInteractions
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
@ExperimentalCoroutinesApi
open class EditorViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Mock
    lateinit var openPage: OpenPage

    @Mock
    lateinit var closePage: CloseBlock

    @Mock
    lateinit var interceptEvents: InterceptEvents

    @Mock
    lateinit var interceptThreadStatus: InterceptThreadStatus

    @Mock
    lateinit var createBlock: CreateBlock

    @Mock
    lateinit var updateText: UpdateText

    @Mock
    lateinit var updateCheckbox: UpdateCheckbox

    @Mock
    lateinit var duplicateBlock: DuplicateBlock

    @Mock
    lateinit var unlinkBlocks: UnlinkBlocks

    @Mock
    lateinit var updateDivider: UpdateDivider

    @Mock
    lateinit var updateTextStyle: UpdateTextStyle

    @Mock
    lateinit var updateTextColor: UpdateTextColor

    @Mock
    lateinit var updateLinkMark: UpdateLinkMarks

    @Mock
    lateinit var setRelationKey: SetRelationKey

    @Mock
    lateinit var updateBlocksMark: UpdateBlocksMark

    @Mock
    lateinit var removeLinkMark: RemoveLinkMark

    @Mock
    lateinit var mergeBlocks: MergeBlocks

    @Mock
    lateinit var splitBlock: SplitBlock

    @Mock
    lateinit var createPage: CreatePage

    @Mock
    lateinit var createObject: CreateObject

    @Mock
    lateinit var updateAlignment: UpdateAlignment

    @Mock
    lateinit var updateBackgroundColor: UpdateBackgroundColor

    @Mock
    lateinit var downloadFile: DownloadFile

    @Mock
    lateinit var uploadBlock: UploadBlock

    @Mock
    lateinit var paste: Paste

    @Mock
    lateinit var copy: Copy

    @Mock
    lateinit var undo: Undo

    @Mock
    lateinit var redo: Redo

    @Mock
    lateinit var setupBookmark: SetupBookmark

    @Mock
    lateinit var createBookmark: CreateBookmark

    @Mock
    lateinit var createDocument: CreateDocument

    @Mock
    lateinit var createNewDocument: CreateNewDocument

    @Mock
    lateinit var setObjectIsArchived: SetObjectIsArchived

    @Mock
    lateinit var replaceBlock: ReplaceBlock

    @Mock
    lateinit var updateTitle: UpdateTitle

    @Mock
    lateinit var move: Move

    @Mock
    lateinit var turnIntoDocument: TurnIntoDocument

    @Mock
    lateinit var turnIntoStyle: TurnIntoStyle

    @Mock
    lateinit var updateFields: UpdateFields

    @Mock
    lateinit var gateway: Gateway

    @Mock
    lateinit var analytics: Analytics

    @Mock
    lateinit var coverImageHashProvider: CoverImageHashProvider

    @Mock
    lateinit var getCompatibleObjectTypes: GetCompatibleObjectTypes

    @Mock
    lateinit var repo: BlockRepository

    @Mock
    lateinit var unsplashRepo: UnsplashRepository

    @Mock
    lateinit var setObjectType: SetObjectType

    @Mock
    lateinit var searchObjects: SearchObjects

    @Mock
    lateinit var objectTypesProvider: ObjectTypesProvider

    @Mock
    lateinit var getDefaultEditorType: GetDefaultEditorType

    @Mock
    lateinit var findObjectSetForType: FindObjectSetForType

    @Mock
    lateinit var createObjectSet: CreateObjectSet

    @Mock
    lateinit var copyFileToCacheDirectory: CopyFileToCacheDirectory

    @Mock
    lateinit var editorTemplateDelegate: EditorTemplateDelegate

    private lateinit var updateDetail: UpdateDetail

    lateinit var vm: EditorViewModel

    private lateinit var builder: UrlBuilder
    private lateinit var downloadUnsplashImage: DownloadUnsplashImage
    private lateinit var setDocCoverImage: SetDocCoverImage
    private lateinit var setDocImageIcon: SetDocumentImageIcon

    val root = MockDataFactory.randomUuid()

    val delegator = Delegator.Default<Action>()

    val title = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.Text(
            text = MockDataFactory.randomString(),
            style = Block.Content.Text.Style.TITLE,
            marks = emptyList()
        ),
        children = emptyList(),
        fields = Block.Fields.empty()
    )

    val header = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.Layout(
            type = Block.Content.Layout.Type.HEADER
        ),
        fields = Block.Fields.empty(),
        children = listOf(title.id)
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        builder = UrlBuilder(gateway)
    }

    @Test
    fun `should not start observing events when view model is initialized`() {
        buildViewModel()
        verifyZeroInteractions(interceptEvents)
    }

    @Test
    fun `should start opening page when requested`() {
        val param = OpenPage.Params(id = root)

        stubInterceptEvents()
        buildViewModel()
        stubOpenPage(context = root)

        vm.onStart(root)

        runBlockingTest { verify(openPage, times(1)).invoke(param) }
    }

    @Test
    fun `should dispatch a page to UI when this view model receives an appropriate command`() {

        val child = MockDataFactory.randomUuid()

        val paragraph = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Smart(),
                children = listOf(header.id, child)
            ),
            header,
            title,
            paragraph
        )

        stubOpenPage(
            context = root,
            events = listOf(
                Event.Command.ShowObject(
                    root = root,
                    blocks = page,
                    context = root
                )
            )
        )

        stubInterceptEvents()

        buildViewModel(builder)

        vm.onStart(root)

        val expected = ViewState.Success(
            blocks = listOf(
                BlockView.Title.Basic(
                    isFocused = false,
                    id = title.id,
                    text = title.content<TXT>().text
                ),
                BlockView.Text.Paragraph(
                    id = paragraph.id,
                    text = paragraph.content<Block.Content.Text>().text,
                    backgroundColor = paragraph.backgroundColor
                )
            )
        )

        vm.state.test().assertValue(expected)
    }

    @Test
    fun `should close page when the system back button is pressed`() {

        val root = MockDataFactory.randomUuid()

        stubOpenPage(root)
        stubInterceptEvents()

        buildViewModel()

        vm.onStart(root)

        verifyZeroInteractions(closePage)

        vm.onSystemBackPressed(editorHasChildrenScreens = false)

        runBlockingTest {
            verify(closePage, times(1)).invoke(any())
        }
    }

    @Test
    fun `should emit an approprtiate navigation command when the page is closed`() {

        val response = Either.Right(Unit)

        stubInterceptEvents()
        stubClosePage(response)
        buildViewModel()

        val testObserver = vm.navigation.test()

        verifyZeroInteractions(closePage)

        vm.onSystemBackPressed(editorHasChildrenScreens = false)

        testObserver
            .assertHasValue()
            .assertValue { value -> value.peekContent() == AppNavigation.Command.Exit }
    }

    @Test
    fun `should not emit any navigation command if there is an error while closing the page`() {

        val root = MockDataFactory.randomUuid()

        val error = Exception("Error while closing this page")

        val response = Either.Left(error)

        stubOpenPage(root)
        stubClosePage(response)
        stubInterceptEvents()
        buildViewModel()

        vm.onStart(root)

        val testObserver = vm.navigation.test()

        verifyZeroInteractions(closePage)

        vm.onSystemBackPressed(editorHasChildrenScreens = false)

        testObserver.assertNoValue()
    }

    @Test
    fun `should update block when its text changes`() {

        val blockId = MockDataFactory.randomUuid()
        val pageId = root
        val text = MockDataFactory.randomString()

        stubInterceptEvents()
        buildViewModel()
        stubOpenPage(context = pageId)
        stubUpdateText()

        vm.onStart(pageId)
        vm.onTextChanged(id = blockId, text = text, marks = emptyList())

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        runBlockingTest {
            verify(updateText, times(1)).invoke(
                argThat { this.context == pageId && this.target == blockId && this.text == text }
            )
        }
    }

    @Test
    fun `should debounce values when dispatching text changes`() {

        val blockId = MockDataFactory.randomUuid()
        val pageId = MockDataFactory.randomUuid()
        val text = MockDataFactory.randomString()

        stubObserveEvents()
        stubUpdateText()
        stubOpenPage(context = pageId)
        buildViewModel()

        vm.onStart(pageId)

        vm.onTextChanged(id = blockId, text = text, marks = emptyList())
        vm.onTextChanged(id = blockId, text = text, marks = emptyList())
        vm.onTextChanged(id = blockId, text = text, marks = emptyList())

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        vm.onTextChanged(id = blockId, text = text, marks = emptyList())

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        runBlockingTest {
            verify(updateText, times(2)).invoke(
                argThat { this.context == pageId && this.target == blockId && this.text == text }
            )
        }
    }

    @Test
    fun `should add a new block to the already existing one when this view model receives an appropriate command`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()

        val paragraph = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Smart(),
                children = listOf(header.id, child)
            ),
            header,
            title,
            paragraph
        )

        val added = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        stubObserveEvents(
            flow {
                delay(100)
                emit(
                    listOf(
                        Event.Command.ShowObject(
                            root = root,
                            blocks = page,
                            context = root
                        )
                    )
                )
                delay(100)
                emit(
                    listOf(
                        Event.Command.UpdateStructure(
                            context = root,
                            id = root,
                            children = listOf(header.id, child, added.id)
                        )
                    )
                )
                emit(
                    listOf(
                        Event.Command.AddBlock(
                            blocks = listOf(added),
                            context = root
                        )
                    )
                )
            }
        )

        stubOpenPage()
        buildViewModel(builder)
        vm.onStart(root)

        coroutineTestRule.advanceTime(200)

        val expected =
            ViewState.Success(
                listOf(
                    BlockView.Title.Basic(
                        isFocused = false,
                        id = title.id,
                        text = title.content<TXT>().text
                    ),
                    BlockView.Text.Paragraph(
                        id = paragraph.id,
                        text = paragraph.content.asText().text,
                        backgroundColor = paragraph.backgroundColor
                    ),
                    BlockView.Text.Paragraph(
                        id = added.id,
                        text = added.content.asText().text,
                        backgroundColor = added.backgroundColor
                    )
                )
            )

        vm.state.test().assertValue(expected)
    }

    @Test
    fun `should start creating a new block if user clicked create-text-block-button`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomString()

        val smart = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, child)
        )

        val paragraph = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        stubOpenPage(
            context = root,
            events = listOf(
                Event.Command.ShowObject(
                    context = root,
                    blocks = listOf(smart, header, title, paragraph),
                    root = root
                )
            )
        )

        stubCreateBlock(root)

        buildViewModel()

        vm.onStart(root)

        vm.onBlockFocusChanged(id = paragraph.id, hasFocus = true)

        vm.onAddTextBlockClicked(style = Block.Content.Text.Style.P)

        runBlockingTest {
            verify(createBlock, times(1)).invoke(any())
        }
    }

    @Test
    fun `should update block text without dispatching it to UI when we receive an appropriate event`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()

        val paragraph = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Smart(),
                children = listOf(header.id, child)
            ),
            header,
            title,
            paragraph
        )

        val text = MockDataFactory.randomString()

        interceptEvents.stub {
            onBlocking { build(any()) } doReturn flow {
                delay(100)
                emit(
                    listOf(
                        Event.Command.ShowObject(
                            root = root,
                            blocks = page,
                            context = root
                        )
                    )
                )
                delay(100)
                emit(
                    listOf(
                        Event.Command.UpdateBlockText(
                            text = text,
                            id = child,
                            context = root
                        )
                    )
                )
            }
        }

        stubOpenPage()
        buildViewModel(builder)

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        val beforeUpdate = ViewState.Success(
            listOf(
                BlockView.Title.Basic(
                    isFocused = false,
                    id = title.id,
                    text = title.content<TXT>().text
                ),
                BlockView.Text.Paragraph(
                    id = paragraph.id,
                    text = paragraph.content.asText().text,
                    backgroundColor = paragraph.backgroundColor
                )
            )
        )

        vm.state.test().assertValue(beforeUpdate)

        coroutineTestRule.advanceTime(200)

        val afterUpdate = beforeUpdate.copy()

        vm.state.test().assertValue(afterUpdate)
    }

    @Test
    fun `should emit loading state when starting opening a page`() {

        val root = MockDataFactory.randomUuid()

        stubOpenPage()
        stubObserveEvents()
        buildViewModel()

        val testObserver = vm.state.test()

        vm.onStart(root)

        testObserver.assertValue(ViewState.Loading)
    }

    @Test
    fun `should apply two different markup actions`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()

        val paragraph = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P,
                color = "red"
            ),
            children = emptyList(),
            backgroundColor = "yellow"
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, child)
        )

        val blocks = listOf(
            header,
            title,
            page,
            paragraph
        )

        interceptEvents.stub {
            onBlocking { build(any()) } doReturn flow {
                delay(100)
                emit(
                    listOf(
                        Event.Command.ShowObject(
                            root = root,
                            blocks = blocks,
                            context = root
                        )
                    )
                )
            }
        }

        stubOpenPage()

        stubUpdateText()

        buildViewModel(builder)

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        val firstTimeRange = 0..3
        val firstTimeMarkup = StylingEvent.Markup.Bold

        vm.onBlockFocusChanged(
            hasFocus = true,
            id = paragraph.id
        )

        vm.onSelectionChanged(
            id = paragraph.id,
            selection = firstTimeRange
        )

        vm.onStyleToolbarMarkupAction(type = Markup.Type.BOLD)

        val firstTimeExpected = ViewState.Success(
            listOf(
                BlockView.Title.Basic(
                    isFocused = false,
                    id = title.id,
                    text = title.content<TXT>().text
                ),
                BlockView.Text.Paragraph(
                    isFocused = true,
                    id = paragraph.id,
                    text = paragraph.content.asText().text,
                    color = paragraph.content<Block.Content.Text>().color,
                    backgroundColor = paragraph.backgroundColor,
                    marks = listOf(
                        Markup.Mark.Bold(
                            from = firstTimeRange.first(),
                            to = firstTimeRange.last()
                        )
                    )
                )
            )
        )

        vm.state.test().apply {
            assertHasValue()
            assertValue(firstTimeExpected)
        }

        val secondTimeRange = 0..5
        val secondTimeMarkup = StylingEvent.Markup.Italic

        vm.onSelectionChanged(
            id = paragraph.id,
            selection = secondTimeRange
        )

        vm.onStyleToolbarMarkupAction(type = Markup.Type.ITALIC)

        val secondTimeExpected = ViewState.Success(
            listOf(
                BlockView.Title.Basic(
                    isFocused = false,
                    id = title.id,
                    text = title.content<TXT>().text
                ),
                BlockView.Text.Paragraph(
                    isFocused = true,
                    id = paragraph.id,
                    text = paragraph.content.asText().text,
                    color = paragraph.content<Block.Content.Text>().color,
                    backgroundColor = paragraph.backgroundColor,
                    marks = listOf(
                        Markup.Mark.Bold(
                            from = firstTimeRange.first(),
                            to = firstTimeRange.last()
                        ),
                        Markup.Mark.Italic(
                            from = secondTimeRange.first(),
                            to = secondTimeRange.last()
                        )
                    )
                )
            )
        )

        vm.state.test().apply {
            assertHasValue()
            assertValue(secondTimeExpected)
        }
    }

    @Test
    fun `should apply two markup actions of the same markup type`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()

        val paragraph = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P,
                color = "red"
            ),
            backgroundColor = "yellow",
            children = emptyList()
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, child)
        )

        val blocks = listOf(
            header,
            title,
            page,
            paragraph
        )

        val events = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = blocks,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(events)
        stubOpenPage()
        buildViewModel(builder)

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        val firstTimeRange = 0..3
        val firstTimeMarkup = StylingEvent.Markup.Bold

        vm.onBlockFocusChanged(
            hasFocus = true,
            id = paragraph.id
        )

        vm.onSelectionChanged(
            id = paragraph.id,
            selection = firstTimeRange
        )

        vm.onStyleToolbarMarkupAction(type = Markup.Type.BOLD)

        val firstTimeExpected = ViewState.Success(
            listOf(
                BlockView.Title.Basic(
                    isFocused = false,
                    id = title.id,
                    text = title.content<TXT>().text
                ),
                BlockView.Text.Paragraph(
                    isFocused = true,
                    id = paragraph.id,
                    text = paragraph.content.asText().text,
                    color = paragraph.content<Block.Content.Text>().color,
                    backgroundColor = paragraph.backgroundColor,
                    marks = listOf(
                        Markup.Mark.Bold(
                            from = firstTimeRange.first(),
                            to = firstTimeRange.last()
                        )
                    )
                )
            )
        )

        assertEquals(firstTimeExpected, vm.state.value)

        vm.onSelectionChanged(
            id = paragraph.id,
            selection = 3..3
        )

        vm.onSelectionChanged(
            id = paragraph.id,
            selection = 0..0
        )

        val secondTimeRange = 0..5
        val secondTimeMarkup = StylingEvent.Markup.Bold

        vm.onSelectionChanged(
            id = paragraph.id,
            selection = secondTimeRange
        )

        vm.onStyleToolbarMarkupAction(type = Markup.Type.BOLD)

        val secondTimeExpected = ViewState.Success(
            listOf(
                BlockView.Title.Basic(
                    isFocused = false,
                    id = title.id,
                    text = title.content<TXT>().text
                ),
                BlockView.Text.Paragraph(
                    isFocused = true,
                    id = paragraph.id,
                    text = paragraph.content.asText().text,
                    color = paragraph.content<Block.Content.Text>().color,
                    backgroundColor = paragraph.backgroundColor,
                    marks = listOf(
                        Markup.Mark.Bold(
                            from = secondTimeRange.first(),
                            to = secondTimeRange.last()
                        )
                    )
                )
            )
        )

        vm.state.test().apply {
            assertHasValue()
            assertValue(secondTimeExpected)
        }
    }

    @Test
    fun `should dispatch texts changes and markup even if only markup is changed`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()

        val paragraph = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P,
                color = "red"
            ),
            children = emptyList(),
            backgroundColor = "yellow"
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, child)
        )

        val blocks = listOf(
            page,
            header,
            title,
            paragraph
        )

        val events = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = blocks,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(events)
        stubUpdateText()
        stubOpenPage()
        buildViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        val range = 0..3
        val markup = StylingEvent.Markup.Bold

        vm.onBlockFocusChanged(
            hasFocus = true,
            id = paragraph.id
        )

        vm.onSelectionChanged(
            id = paragraph.id,
            selection = range
        )

        vm.onStyleToolbarMarkupAction(type = Markup.Type.BOLD)

        val marks = listOf(
            Block.Content.Text.Mark(
                type = Block.Content.Text.Mark.Type.BOLD,
                range = range
            )
        )

        runBlockingTest {
            verify(updateText, times(1)).invoke(
                params = eq(
                    UpdateText.Params(
                        target = paragraph.id,
                        marks = marks,
                        context = page.id,
                        text = paragraph.content.asText().text
                    )
                )
            )
        }
    }

    @Test
    fun `test changes from UI do not trigger re-rendering`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()

        val paragraph = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, child)
        )

        val blocks = listOf(
            page,
            header,
            title,
            paragraph
        )

        val events = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = blocks,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(events)
        stubOpenPage()
        buildViewModel(builder)
        stubUpdateText()

        val testObserver = vm.state.test()

        vm.onStart(root)

        testObserver.assertValue(ViewState.Loading)

        coroutineTestRule.advanceTime(100)

        val state = ViewState.Success(
            listOf(
                BlockView.Title.Basic(
                    isFocused = false,
                    id = title.id,
                    text = title.content<TXT>().text
                ),
                BlockView.Text.Paragraph(
                    id = paragraph.id,
                    text = paragraph.content.asText().text,
                    backgroundColor = paragraph.backgroundColor
                )
            )
        )

        testObserver.assertValue(state).assertHistorySize(2)

        val userInput = MockDataFactory.randomString()

        val range = 0..3

        val marks = listOf(
            Block.Content.Text.Mark(
                type = Block.Content.Text.Mark.Type.BOLD,
                range = range
            )
        )

        vm.onTextChanged(
            id = paragraph.id,
            marks = marks,
            text = userInput
        )

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        testObserver.assertValue(state).assertHistorySize(2)
    }

    @Test
    fun `should update text inside view state when user changed text`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()
        val initialText = ""

        val initialContent = Block.Content.Text(
            text = initialText,
            marks = emptyList(),
            style = Block.Content.Text.Style.P
        )

        val paragraph = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = initialContent,
            children = emptyList()
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, child)
        )

        val blocks = listOf(
            page,
            header,
            title,
            paragraph
        )

        val events = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = blocks,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(events)
        stubOpenPage()

        buildViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        val userInput = MockDataFactory.randomString()

        vm.onTextChanged(id = paragraph.id, text = userInput, marks = emptyList())

        val contentAfterChange = Block.Content.Text(
            text = userInput,
            marks = emptyList(),
            style = Block.Content.Text.Style.P
        )

        val paragraphAfterChange = paragraph.copy(
            content = contentAfterChange
        )

        val expected = listOf(
            page,
            header,
            title,
            paragraphAfterChange
        )

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        assertEquals(
            expected = expected,
            actual = vm.blocks
        )
    }

    @Test
    fun `should dispatch text changes including markup to the middleware`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()
        val initialText = ""

        val initialContent = Block.Content.Text(
            text = initialText,
            marks = emptyList(),
            style = Block.Content.Text.Style.P
        )

        val paragraph = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = initialContent,
            children = emptyList()
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(child)
        )

        val blocks = listOf(
            page,
            paragraph
        )

        interceptEvents.stub {
            onBlocking { build() } doReturn flow {
                delay(100)
                emit(
                    listOf(
                        Event.Command.ShowObject(
                            root = root,
                            blocks = blocks,
                            context = root
                        )
                    )
                )
            }
        }

        stubOpenPage()

        stubUpdateText()

        buildViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        val userInput = MockDataFactory.randomString()
        val marks = listOf(
            Block.Content.Text.Mark(
                range = 0..5,
                type = Block.Content.Text.Mark.Type.BOLD
            )
        )

        vm.onTextChanged(id = paragraph.id, text = userInput, marks = marks)

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        runBlockingTest {
            verify(updateText, times(1)).invoke(
                params = eq(
                    UpdateText.Params(
                        target = paragraph.id,
                        text = userInput,
                        marks = marks,
                        context = page.id
                    )
                )
            )
        }
    }

    @Test
    fun `should receive initial control panel state when view model is initialized`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()
        val page = MockBlockFactory.makeOnePageWithOneTextBlock(root = root, child = child)

        val flow: Flow<List<Event.Command>> = flow {
            delay(1000)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(1001)

        val expected = ControlPanelState.init()

        vm.controlPanelViewState.test().assertValue(expected)
    }

    @Test
    fun `should dispatch open-add-block-panel command on add-block-toolbar-clicked event`() {

        // SETUP

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()
        val page = MockBlockFactory.makeOnePageWithOneTextBlock(root = root, child = child)

        val flow: Flow<List<Event.Command>> = flow {
            delay(1000)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(1001)

        // TESTING

        vm.onBlockFocusChanged(
            id = child,
            hasFocus = true
        )

        val commands = vm.commands.test()

        vm.onAddBlockToolbarClicked()

        val result = commands.value()

        assertEquals(
            expected = Command.OpenAddBlockPanel(ctx = root),
            actual = result.peekContent()
        )
    }

    @Test
    fun `should add a header-one block on add-header-one event`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()

        val paragraph = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, child)
        )

        val style = Block.Content.Text.Style.H1

        val new = Block(
            id = MockDataFactory.randomString(),
            content = Block.Content.Text(
                text = "",
                marks = emptyList(),
                style = style
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        val flow: Flow<List<Event>> = flow {
            delay(500)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = listOf(page, header, title, paragraph),
                        context = root
                    )
                )
            )
            delay(500)
            emit(
                listOf(
                    Event.Command.UpdateStructure(
                        context = root,
                        id = root,
                        children = listOf(header.id, child, new.id)
                    )
                )
            )
            emit(
                listOf(
                    Event.Command.AddBlock(
                        blocks = listOf(new),
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()

        buildViewModel(builder)

        vm.onStart(root)

        coroutineTestRule.advanceTime(500)

        val testObserver = vm.state.test()

        testObserver.assertValue(
            ViewState.Success(
                blocks = listOf(
                    BlockView.Title.Basic(
                        isFocused = false,
                        id = title.id,
                        text = title.content<TXT>().text
                    ),
                    BlockView.Text.Paragraph(
                        id = paragraph.id,
                        text = paragraph.content<Block.Content.Text>().text,
                        backgroundColor = paragraph.backgroundColor
                    )
                )
            )
        )

        coroutineTestRule.advanceTime(500)

        testObserver.assertValue(
            ViewState.Success(
                blocks = listOf(
                    BlockView.Title.Basic(
                        isFocused = false,
                        id = title.id,
                        text = title.content<TXT>().text
                    ),
                    BlockView.Text.Paragraph(
                        id = paragraph.id,
                        text = paragraph.content<Block.Content.Text>().text,
                        backgroundColor = paragraph.backgroundColor
                    ),
                    BlockView.Text.Header.One(
                        id = new.id,
                        text = new.content<Block.Content.Text>().text,
                        backgroundColor = new.backgroundColor,
                        indent = 0
                    )
                )
            )
        )
    }

    @Test
    fun `should start duplicating focused block when requested`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()
        val page = MockBlockFactory.makeOnePageWithOneTextBlock(root = root, child = child)

        val events: Flow<List<Event.Command>> = flow {
            delay(1000)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubOpenPage()
        stubObserveEvents(events)
        buildViewModel()
        stubDuplicateBlock(
            newBlockId = MockDataFactory.randomString(),
            root = root
        )


        vm.onStart(root)

        coroutineTestRule.advanceTime(1001)

        vm.onBlockFocusChanged(id = child, hasFocus = true)

        vm.onBlockToolbarBlockActionsClicked()

        vm.onBlockFocusChanged(id = child, hasFocus = false)

        vm.onMultiSelectAction(ActionItemType.Duplicate)

        runBlockingTest {
            verify(duplicateBlock, times(1)).invoke(
                params = eq(
                    DuplicateBlock.Params(
                        target = child,
                        context = root,
                        blocks = listOf(child)
                    )
                )
            )
        }
    }

    @Test
    fun `should start deleting focused block when requested`() {

        // SETUP

        val child = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )
        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, child.id)
        )

        val doc = listOf(page, header, title, child)

        val events: Flow<List<Event.Command>> = flow {
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = doc,
                        context = root
                    )
                )
            )
        }

        stubOpenPage()
        stubUnlinkBlocks(root = root)
        stubObserveEvents(events)
        buildViewModel()

        // TESTING

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        vm.onSelectionChanged(id = child.id, selection = IntRange(0, 0))
        vm.onBlockFocusChanged(id = child.id, hasFocus = true)
        vm.onBlockToolbarBlockActionsClicked()
        vm.onBlockFocusChanged(id = child.id, hasFocus = false)
        vm.onMultiSelectModeDeleteClicked()
        vm.onExitMultiSelectModeClicked()

        coroutineTestRule.advanceTime(300)

        runBlockingTest {
            verify(unlinkBlocks, times(1)).invoke(
                params = eq(
                    UnlinkBlocks.Params(
                        context = root,
                        targets = listOf(child.id)
                    )
                )
            )
        }
    }

    @Test
    fun `should delete the first block when the delete-block event received for the first block, then rerender the page`() {

        val pageOpenedDelay = 100L
        val blockDeletedEventDelay = 100L

        val root = MockDataFactory.randomUuid()

        val firstChild = Block(
            id = "FIRST CHILD",
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = "FIRST CHILD TEXT",
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        val secondChild = Block(
            id = "SECOND CHILD",
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = "SECOND CHILD TEXT",
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, firstChild.id, secondChild.id)
        )

        val doc = listOf(page, header, title, firstChild, secondChild)

        val events: Flow<List<Event.Command>> = flow {
            delay(pageOpenedDelay)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = doc,
                        context = root
                    )
                )
            )
            delay(blockDeletedEventDelay)
            emit(
                listOf(
                    Event.Command.DeleteBlock(
                        targets = listOf(firstChild.id),
                        context = root
                    )
                )
            )
        }

        stubOpenPage()
        stubObserveEvents(events)
        buildViewModel(builder)
        stubUnlinkBlocks(root)

        vm.onStart(root)

        coroutineTestRule.advanceTime(pageOpenedDelay)

        val testObserver = vm.state.test()

        testObserver.assertValue(
            ViewState.Success(
                blocks = listOf(
                    BlockView.Title.Basic(
                        isFocused = false,
                        id = title.id,
                        text = title.content<TXT>().text
                    ),
                    BlockView.Text.Paragraph(
                        id = firstChild.id,
                        text = firstChild.content<Block.Content.Text>().text,
                        backgroundColor = firstChild.backgroundColor
                    ),
                    BlockView.Text.Paragraph(
                        id = secondChild.id,
                        text = secondChild.content<Block.Content.Text>().text,
                        backgroundColor = secondChild.backgroundColor
                    )
                )
            )
        )

        vm.onBlockFocusChanged(id = firstChild.id, hasFocus = true)
        vm.onBlockToolbarBlockActionsClicked()
        vm.onBlockFocusChanged(id = firstChild.id, hasFocus = false)
        vm.onMultiSelectModeDeleteClicked()
        vm.onExitMultiSelectModeClicked()

        assertEquals(expected = 5, actual = vm.blocks.size)

        coroutineTestRule.advanceTime(blockDeletedEventDelay)

        assertEquals(expected = 4, actual = vm.blocks.size)

        val expected = ViewState.Success(
            blocks = listOf(
                BlockView.Title.Basic(
                    isFocused = false,
                    id = title.id,
                    text = title.content<TXT>().text,
                    cursor = null
                ),
                BlockView.Text.Paragraph(
                    id = secondChild.id,
                    text = secondChild.content<Block.Content.Text>().text,
                    backgroundColor = secondChild.backgroundColor
                )
            )
        )

        assertEquals(expected, testObserver.value())

        coroutineTestRule.advanceTime(300L)
    }

    @Test
    fun `should start deleting the target block on empty-block-backspace-click event`() {

        val child = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )


        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, child.id)
        )

        val doc = listOf(page, header, title, child)

        val events: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = doc,
                        context = root
                    )
                )
            )
        }

        stubOpenPage()
        stubObserveEvents(events)
        buildViewModel()
        stubUnlinkBlocks(root)

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        vm.onBlockFocusChanged(child.id, true)
        vm.onEmptyBlockBackspaceClicked(child.id)

        verifyBlocking(unlinkBlocks, times(1)) {
            invoke(
                params = eq(
                    UnlinkBlocks.Params(
                        context = root,
                        targets = listOf(child.id)
                    )
                )
            )
        }
    }

    private fun stubUnlinkBlocks(root: String) {
        unlinkBlocks.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(
                Payload(
                    context = root,
                    events = emptyList()
                )
            )
        }
    }

    @Test
    fun `should not proceed with deleting the title block on empty-block-backspace-click event`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()
        val page = MockBlockFactory.makeOnePageWithOneTextBlock(
            root = root,
            child = child,
            style = Block.Content.Text.Style.TITLE
        )

        val events: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubOpenPage()
        stubObserveEvents(events)
        buildViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        vm.onEmptyBlockBackspaceClicked(child)

        verify(unlinkBlocks, never()).invoke(
            scope = any(),
            params = any(),
            onResult = any()
        )
    }

    @Test
    fun `should proceed with creating a new block on end-line-enter-press event`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Smart(SmartBlockType.PAGE),
                children = listOf(header.id, child)
            ),
            header,
            title,
            Block(
                id = child,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Text(
                    text = MockDataFactory.randomString(),
                    marks = emptyList(),
                    style = Block.Content.Text.Style.P
                ),
                children = emptyList()
            )
        )

        val events: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubOpenPage()
        stubObserveEvents(events)
        stubCreateBlock(root)
        buildViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        vm.onEndLineEnterClicked(
            id = child,
            marks = emptyList(),
            text = page.last().content<Block.Content.Text>().text
        )

        runBlockingTest {
            verify(createBlock, times(1)).invoke(
                params = eq(
                    CreateBlock.Params(
                        context = root,
                        target = child,
                        position = Position.BOTTOM,
                        prototype = Block.Prototype.Text(style = Block.Content.Text.Style.P)
                    )
                )
            )
        }
    }

    @Test
    fun `should start updating text style of the focused block on turn-into-option-clicked event`() {

        val root = MockDataFactory.randomUuid()
        val firstChild = MockDataFactory.randomUuid()
        val secondChild = MockDataFactory.randomUuid()
        val page = MockBlockFactory.makeOnePageWithTwoTextBlocks(
            root = root,
            firstChild = firstChild,
            firstChildStyle = Block.Content.Text.Style.TITLE,
            secondChild = secondChild,
            secondChildStyle = Block.Content.Text.Style.P
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        stubTurnIntoStyle()

        buildViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        vm.onBlockFocusChanged(
            id = secondChild,
            hasFocus = true
        )

        val newStyle = Block.Content.Text.Style.H1

        vm.onTurnIntoBlockClicked(secondChild, UiBlock.HEADER_ONE)

        runBlockingTest {
            verify(turnIntoStyle, times(1)).invoke(
                params = eq(
                    TurnIntoStyle.Params(
                        context = root,
                        targets = listOf(secondChild),
                        style = newStyle
                    )
                )
            )
        }
    }

    @Test
    fun `should start updating the target block's color on color-toolbar-option-selected event`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()

        val page = MockBlockFactory.makeOnePageWithOneTextBlock(
            root = root,
            child = child,
            style = Block.Content.Text.Style.TITLE
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        stubUpdateTextColor(root)

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        vm.onBlockFocusChanged(
            id = child,
            hasFocus = true
        )

        val color = MockDataFactory.randomString()

        vm.onToolbarTextColorAction(color = color, targets = listOf(child))

        runBlockingTest {
            verify(updateTextColor, times(1)).invoke(
                params = eq(
                    UpdateTextColor.Params(
                        context = root,
                        targets = listOf(child),
                        color = color
                    )
                )
            )
        }
    }

    @Test
    fun `should start creating a new paragraph on endline-enter-pressed event inside a quote block`() {

        val style = Block.Content.Text.Style.QUOTE
        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Smart(SmartBlockType.PAGE),
                children = listOf(header.id, child)
            ),
            header,
            title,
            Block(
                id = child,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Text(
                    text = MockDataFactory.randomString(),
                    marks = emptyList(),
                    style = style
                ),
                children = emptyList()
            )
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        stubCreateBlock(root)
        buildViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        vm.onBlockFocusChanged(
            id = child,
            hasFocus = true
        )

        vm.onEndLineEnterClicked(
            id = child,
            text = page.last().content<Block.Content.Text>().text,
            marks = emptyList()
        )

        runBlockingTest {
            verify(createBlock, times(1)).invoke(
                params = eq(
                    CreateBlock.Params(
                        context = root,
                        target = child,
                        prototype = Block.Prototype.Text(
                            style = Block.Content.Text.Style.P
                        ),
                        position = Position.BOTTOM
                    )
                )
            )
        }
    }

    @Test
    fun `should turn a list item with empty text into a paragraph on endline-enter-pressed event`() {

        val root = MockDataFactory.randomUuid()
        val firstChild = MockDataFactory.randomUuid()
        val secondChild = MockDataFactory.randomUuid()

        val page = MockBlockFactory.makeOnePageWithTwoTextBlocks(
            root = root,
            firstChild = firstChild,
            secondChild = secondChild,
            firstChildStyle = Block.Content.Text.Style.TITLE,
            secondChildStyle = Block.Content.Text.Style.BULLET,
            secondChildText = ""
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        stubUpdateTextStyle()
        buildViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        vm.onBlockFocusChanged(
            id = secondChild,
            hasFocus = true
        )

        vm.onEndLineEnterClicked(
            id = secondChild,
            text = "",
            marks = emptyList()
        )

        runBlockingTest {

            verify(createBlock, never()).invoke(
                scope = any(),
                params = any(),
                onResult = any()
            )

            verify(updateTextStyle, times(1)).invoke(
                params = eq(
                    UpdateTextStyle.Params(
                        targets = listOf(secondChild),
                        style = Block.Content.Text.Style.P,
                        context = root
                    )
                )
            )
        }
    }

    @Test
    fun `should send update text style intent when is list and empty`() {
        // SETUP

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Smart(SmartBlockType.PAGE),
                children = listOf(header.id, child)
            ),
            header,
            title,
            Block(
                id = child,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Text(
                    text = MockDataFactory.randomString(),
                    marks = emptyList(),
                    style = Block.Content.Text.Style.P
                ),
                children = emptyList()
            )
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()
        stubUpdateText()
        stubSplitBlocks(root)

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        // TESTING

        vm.onBlockFocusChanged(
            id = child,
            hasFocus = true
        )

        val index = MockDataFactory.randomInt()

        val text = MockDataFactory.randomString()

        vm.onTextChanged(
            id = child,
            text = text,
            marks = emptyList()
        )

        vm.onEnterKeyClicked(
            target = child,
            text = text,
            marks = emptyList(),
            range = 0..0
        )

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        runBlockingTest {
            verify(updateText, times(1)).invoke(
                params = eq(
                    UpdateText.Params(
                        context = root,
                        text = text,
                        target = child,
                        marks = emptyList()
                    )
                )
            )
        }

        runBlockingTest {
            verify(splitBlock, times(1)).invoke(
                params = eq(
                    SplitBlock.Params(
                        context = root,
                        block = page[3],
                        range = 0..0,
                        isToggled = null
                    )
                )
            )
        }
    }

    @Test
    fun `should preserve text style while splitting`() {

        // SETUP

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()

        val style = Block.Content.Text.Style.BULLET

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Smart(),
                children = listOf(header.id, child)
            ),
            header,
            title,
            Block(
                id = child,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Text(
                    text = MockDataFactory.randomString(),
                    marks = emptyList(),
                    style = style
                ),
                children = emptyList()
            )
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()
        stubUpdateText()
        stubSplitBlocks(root)

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        // TESTING

        vm.onBlockFocusChanged(
            id = child,
            hasFocus = true
        )

        val index = MockDataFactory.randomInt()

        val text = MockDataFactory.randomString()

        vm.onTextChanged(
            id = child,
            text = text,
            marks = emptyList()
        )

        vm.onEnterKeyClicked(
            target = child,
            text = text,
            marks = emptyList(),
            range = 1..1
        )

        runBlockingTest {
            verify(splitBlock, times(1)).invoke(
                params = eq(
                    SplitBlock.Params(
                        context = root,
                        block = page[3],
                        range = 1..1,
                        isToggled = null
                    )
                )
            )
        }

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    @Test
    fun `should start downloading file`() {

        val root = MockDataFactory.randomUuid()
        val file = MockBlockFactory.makeFileBlock()
        val title = MockBlockFactory.makeTitleBlock()

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Smart(),
                children = listOf(title.id, file.id)
            ),
            title,
            file
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel(builder)

        stubDownloadFile()

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        // TESTING

        vm.startDownloadingFile(id = file.id)

        runBlockingTest {
            verify(downloadFile, times(1)).invoke(
                params = eq(
                    DownloadFile.Params(
                        name = file.content<Block.Content.File>().name.orEmpty(),
                        url = builder.file(
                            hash = file.content<Block.Content.File>().hash
                        )
                    )
                )
            )
        }
    }

    @Test
    fun `should create a new text block after currently focused block`() {

        val root = MockDataFactory.randomUuid()
        val paragraph = MockBlockFactory.makeParagraphBlock()
        val title = MockBlockFactory.makeTitleBlock()

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(
                    map = mapOf("icon" to "")
                ),
                content = Block.Content.Smart(),
                children = listOf(title.id, paragraph.id)
            ),
            title,
            paragraph
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        stubCreateBlock(root)
        buildViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        // TESTING

        vm.onBlockFocusChanged(
            id = title.id,
            hasFocus = true
        )

        vm.onAddBlockToolbarClicked()

        vm.onAddTextBlockClicked(
            style = Block.Content.Text.Style.P
        )

        runBlockingTest {
            verify(createBlock, times(1)).invoke(
                params = eq(
                    CreateBlock.Params(
                        context = root,
                        target = title.id,
                        position = Position.BOTTOM,
                        prototype = Block.Prototype.Text(Block.Content.Text.Style.P)
                    )
                )
            )
        }
    }

    @Test
    fun `should create a new page block after currently focused block`() {
        val root = MockDataFactory.randomUuid()
        val paragraph = MockBlockFactory.makeParagraphBlock()
        val title = MockBlockFactory.makeTitleBlock()
        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(
                    map = mapOf("icon" to "")
                ),
                content = Block.Content.Smart(),
                children = listOf(title.id, paragraph.id)
            ),
            title,
            paragraph
        )
        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }
        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()
        vm.onStart(root)
        coroutineTestRule.advanceTime(100)
        // TESTING
        vm.onBlockFocusChanged(
            id = title.id,
            hasFocus = true
        )
        vm.onAddBlockToolbarClicked()
        vm.onAddNewPageClicked()
        runBlockingTest {
            verify(createDocument, times(1)).invoke(
                params = eq(
                    CreateDocument.Params(
                        context = root,
                        target = title.id,
                        position = Position.BOTTOM
                    )
                )
            )
        }
    }

    @Test
    fun `should create a new object block after currently focused block`() {
        val root = MockDataFactory.randomUuid()
        val paragraph = MockBlockFactory.makeParagraphBlock()
        val title = MockBlockFactory.makeTitleBlock()
        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(
                    map = mapOf("icon" to "")
                ),
                content = Block.Content.Smart(),
                children = listOf(title.id, paragraph.id)
            ),
            title,
            paragraph
        )
        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }
        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()
        vm.onStart(root)
        coroutineTestRule.advanceTime(100)
        // TESTING
        vm.onBlockFocusChanged(
            id = title.id,
            hasFocus = true
        )
        vm.onAddBlockToolbarClicked()
        vm.onAddNewObjectClicked(type = "_idea", layout = ObjectType.Layout.TODO)
        runBlockingTest {
            verify(createObject, times(1)).invoke(
                params = eq(
                    CreateObject.Params(
                        context = root,
                        target = title.id,
                        position = Position.BOTTOM,
                        type = "_idea",
                        layout = ObjectType.Layout.TODO
                    )
                )
            )
        }
    }

    @Test
    fun `should create a new bookmark block after currently focused block`() {

        val root = MockDataFactory.randomUuid()
        val paragraph = MockBlockFactory.makeParagraphBlock()
        val title = MockBlockFactory.makeTitleBlock()

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(
                    map = mapOf("icon" to "")
                ),
                content = Block.Content.Smart(),
                children = listOf(title.id, paragraph.id)
            ),
            title,
            paragraph
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()
        stubCreateBlock(root)

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        // TESTING

        vm.onBlockFocusChanged(
            id = title.id,
            hasFocus = true
        )

        vm.onAddBlockToolbarClicked()

        vm.onAddBookmarkBlockClicked()

        runBlockingTest {
            verify(createBlock, times(1)).invoke(
                params = eq(
                    CreateBlock.Params(
                        context = root,
                        target = title.id,
                        position = Position.BOTTOM,
                        prototype = Block.Prototype.Bookmark
                    )
                )
            )
        }
    }

    @Test
    fun `should create a new line divider block after currently focused block`() {

        val root = MockDataFactory.randomUuid()
        val paragraph = MockBlockFactory.makeParagraphBlock()
        val title = MockBlockFactory.makeTitleBlock()

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(
                    map = mapOf("icon" to "")
                ),
                content = Block.Content.Smart(),
                children = listOf(title.id, paragraph.id)
            ),
            title,
            paragraph
        )

        stubObserveEvents()
        stubOpenPage(
            events = listOf(
                Event.Command.ShowObject(
                    root = root,
                    blocks = page,
                    context = root
                )
            )
        )
        stubCreateBlock(root = root)
        buildViewModel()

        vm.onStart(root)

        // TESTING

        vm.onBlockFocusChanged(
            id = title.id,
            hasFocus = true
        )

        vm.onAddBlockToolbarClicked()

        vm.onAddDividerBlockClicked(style = Block.Content.Divider.Style.LINE)

        runBlockingTest {
            verify(createBlock, times(1)).invoke(
                params = eq(
                    CreateBlock.Params(
                        context = root,
                        target = title.id,
                        position = Position.BOTTOM,
                        prototype = Block.Prototype.DividerLine
                    )
                )
            )
        }
    }

    @Test
    fun `should create a new dots divider block after currently focused block`() {

        val root = MockDataFactory.randomUuid()
        val paragraph = MockBlockFactory.makeParagraphBlock()
        val title = MockBlockFactory.makeTitleBlock()

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(
                    map = mapOf("icon" to "")
                ),
                content = Block.Content.Smart(),
                children = listOf(title.id, paragraph.id)
            ),
            title,
            paragraph
        )

        stubObserveEvents()
        stubOpenPage(
            events = listOf(
                Event.Command.ShowObject(
                    root = root,
                    blocks = page,
                    context = root
                )
            )
        )
        stubCreateBlock(root = root)
        buildViewModel()

        vm.onStart(root)

        // TESTING

        vm.onBlockFocusChanged(
            id = title.id,
            hasFocus = true
        )

        vm.onAddBlockToolbarClicked()

        vm.onAddDividerBlockClicked(style = Block.Content.Divider.Style.DOTS)

        runBlockingTest {
            verify(createBlock, times(1)).invoke(
                params = eq(
                    CreateBlock.Params(
                        context = root,
                        target = title.id,
                        position = Position.BOTTOM,
                        prototype = Block.Prototype.DividerDots
                    )
                )
            )
        }
    }

    @Test
    fun `should proceed with undo`() {

        val root = MockDataFactory.randomUuid()
        val paragraph = MockBlockFactory.makeParagraphBlock()
        val title = MockBlockFactory.makeTitleBlock()

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(
                    map = mapOf("icon" to "")
                ),
                content = Block.Content.Smart(),
                children = listOf(title.id, paragraph.id)
            ),
            title,
            paragraph
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage(context = root)
        buildViewModel()

        undo.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(
                Undo.Result.Success(
                    Payload(
                        context = root,
                        events = emptyList()
                    )
                )
            )
        }

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        // TESTING

        vm.onActionUndoClicked()

        runBlockingTest {
            verify(undo, times(1)).invoke(
                params = eq(
                    Undo.Params(context = root)
                )
            )
        }
    }

    @Test
    fun `should proceed with redo`() {

        val root = MockDataFactory.randomUuid()
        val paragraph = MockBlockFactory.makeParagraphBlock()
        val title = MockBlockFactory.makeTitleBlock()

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(
                    map = mapOf("icon" to "")
                ),
                content = Block.Content.Smart(),
                children = listOf(title.id, paragraph.id)
            ),
            title,
            paragraph
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        redo.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(
                Redo.Result.Success(
                    Payload(
                        context = root,
                        events = emptyList()
                    )
                )
            )
        }

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        // TESTING

        vm.onActionRedoClicked()

        runBlockingTest {
            verify(redo, times(1)).invoke(
                params = eq(
                    Redo.Params(context = root)
                )
            )
        }
    }

    @Test
    fun `should start archiving document on on-archive-this-page-clicked event`() {

        // SETUP

        val title = MockBlockFactory.makeTitleBlock()

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(
                    map = mapOf("icon" to "")
                ),
                content = Block.Content.Smart(),
                children = listOf(title.id)
            ),
            title
        )

        stubInterceptEvents()

        stubOpenPage(
            events = listOf(
                Event.Command.ShowObject(
                    root = root,
                    blocks = page,
                    context = root
                )
            )
        )

        stubArchiveDocument()

        buildViewModel()

        vm.onStart(root)

        // TESTING

        vm.onArchiveThisObjectClicked()

        runBlockingTest {
            verify(setObjectIsArchived, times(1)).invoke(
                params = eq(
                    SetObjectIsArchived.Params(
                        context = root,
                        isArchived = true
                    )
                )
            )
        }
    }

    @Test
    fun `should start closing page after successful archive operation`() {

        // SETUP

        val title = MockBlockFactory.makeTitleBlock()

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(
                    map = mapOf("icon" to "")
                ),
                content = Block.Content.Smart(),
                children = listOf(title.id)
            ),
            title
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        stubClosePage()
        buildViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        stubArchiveDocument()
        stubClosePage()

        // TESTING

        vm.onArchiveThisObjectClicked()

        runBlockingTest {
            verify(setObjectIsArchived, times(1)).invoke(
                params = eq(
                    SetObjectIsArchived.Params(
                        context = root,
                        isArchived = true
                    )
                )
            )

            verify(closePage, times(1)).invoke(
                params = eq(
                    CloseBlock.Params(
                        id = root
                    )
                )
            )
        }
    }

    private fun stubArchiveDocument(
        params: SetObjectIsArchived.Params =
            SetObjectIsArchived.Params(
                context = root,
                isArchived = true
            )
    ) {
        setObjectIsArchived.stub {
            onBlocking { invoke(params = params) } doReturn Either.Right(
                Payload(
                    context = root,
                    events = emptyList()
                )
            )
        }
    }

    @Test
    fun `should convert paragraph to numbered list without any delay when regex matches`() {

        // SETUP

        val root = MockDataFactory.randomUuid()
        val paragraph = MockBlockFactory.makeParagraphBlock()
        val title = MockBlockFactory.makeTitleBlock()

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields.empty(),
                content = Block.Content.Smart(),
                children = listOf(title.id, paragraph.id)
            ),
            title,
            paragraph
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        stubReplaceBlock(root = root)
        buildViewModel()
        stubReplaceBlock(root)

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        // TESTING

        val update = "1. "

        vm.onTextBlockTextChanged(
            BlockView.Text.Paragraph(
                id = paragraph.id,
                marks = emptyList(),
                text = update
            )
        )

        runBlockingTest {
            verify(replaceBlock, times(1)).invoke(
                params = eq(
                    ReplaceBlock.Params(
                        context = root,
                        target = paragraph.id,
                        prototype = Block.Prototype.Text(
                            style = Block.Content.Text.Style.NUMBERED
                        )
                    )
                )
            )
        }
    }

    @Test
    fun `should ignore create-numbered-list-item pattern and update text with delay`() {

        // SETUP

        val root = MockDataFactory.randomUuid()
        val numbered = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.NUMBERED
            ),
            children = emptyList()
        )
        val title = MockBlockFactory.makeTitleBlock()

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields.empty(),
                content = Block.Content.Smart(),
                children = listOf(title.id, numbered.id)
            ),
            title,
            numbered
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        // TESTING

        val update = "1. "

        vm.onTextChanged(
            id = numbered.id,
            marks = numbered.content<Block.Content.Text>().marks,
            text = update
        )

        runBlockingTest {
            verify(updateText, never()).invoke(
                params = any()
            )

            verify(replaceBlock, never()).invoke(
                params = any()
            )
        }

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        runBlockingTest {
            verify(replaceBlock, never()).invoke(
                params = any()
            )

            verify(updateText, times(1)).invoke(
                params = eq(
                    UpdateText.Params(
                        context = root,
                        target = numbered.id,
                        marks = numbered.content<Block.Content.Text>().marks,
                        text = update
                    )
                )
            )
        }
    }

    @Test
    fun `should not update text while processing paragraph-to-numbered-list editor pattern`() {

        // SETUP

        val root = MockDataFactory.randomUuid()
        val paragraph = MockBlockFactory.makeParagraphBlock()
        val title = MockBlockFactory.makeTitleBlock()

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields.empty(),
                content = Block.Content.Smart(),
                children = listOf(title.id, paragraph.id)
            ),
            title,
            paragraph
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()
        stubUpdateText()
        stubReplaceBlock(root)

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        // TESTING

        val update = "1. "

        vm.onTextBlockTextChanged(
            BlockView.Text.Paragraph(
                id = paragraph.id,
                marks = emptyList(),
                text = update
            )
        )

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        verify(updateText, never()).invoke(
            scope = any(),
            params = any(),
            onResult = any()
        )
    }

    @Test
    fun `should update focus after block duplication`() {

        val root = MockDataFactory.randomUuid()
        val paragraph = MockBlockFactory.makeParagraphBlock()

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields.empty(),
                content = Block.Content.Smart(),
                children = listOf(paragraph.id)
            ),
            paragraph
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        // TESTING

        val newBlockId = MockDataFactory.randomUuid()

        stubDuplicateBlock(newBlockId, root)

        val focus = vm.focus.test()

        focus.assertValue { id -> id.isEmpty() }

        vm.onBlockFocusChanged(
            id = paragraph.id,
            hasFocus = true
        )

        focus.assertValue { id -> id == paragraph.id }

        vm.onBlockToolbarBlockActionsClicked()
        vm.onBlockFocusChanged(id = paragraph.id, hasFocus = false)
        vm.onMultiSelectAction(ActionItemType.Duplicate)

        runBlockingTest {
            verify(duplicateBlock, times(1)).invoke(
                params = eq(
                    DuplicateBlock.Params(
                        context = root,
                        target = paragraph.id,
                        blocks = listOf(paragraph.id)
                    )
                )
            )
        }

        verifyNoMoreInteractions(duplicateBlock)

        focus.assertValue { id -> id == newBlockId }

        coroutineTestRule.advanceTime(200)
    }

    private fun stubDuplicateBlock(newBlockId: String, root: String) {
        duplicateBlock.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(
                Pair(
                    listOf(newBlockId),
                    Payload(
                        context = root,
                        events = emptyList()
                    )
                )
            )
        }
    }

    @Test
    fun `should enter multi-select mode and select blocks, and exit into edit mode when all blocks are unselected`() {

        // SETUP

        val paragraphs = listOf(
            Block(
                id = MockDataFactory.randomString(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.P
                ),
                children = emptyList(),
                fields = Block.Fields.empty()
            ),
            Block(
                id = MockDataFactory.randomString(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.P
                ),
                children = emptyList(),
                fields = Block.Fields.empty()
            ),
            Block(
                id = MockDataFactory.randomString(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.P
                ),
                children = emptyList(),
                fields = Block.Fields.empty()
            )
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields.empty(),
                content = Block.Content.Smart(),
                children = listOf(header.id) + paragraphs.map { it.id }
            )
        ) + listOf(header, title) + paragraphs

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        // TESTING

        val testObserver = vm.state.test()

        val titleView = BlockView.Title.Basic(
            id = title.id,
            text = title.content<TXT>().text,
            isFocused = false
        )

        val initial = listOf(
            paragraphs[0].let { p ->
                BlockView.Text.Paragraph(
                    id = p.id,
                    marks = emptyList(),
                    text = p.content<Block.Content.Text>().text
                )
            },
            paragraphs[1].let { p ->
                BlockView.Text.Paragraph(
                    id = p.id,
                    marks = emptyList(),
                    text = p.content<Block.Content.Text>().text
                )
            },
            paragraphs[2].let { p ->
                BlockView.Text.Paragraph(
                    id = p.id,
                    marks = emptyList(),
                    text = p.content<Block.Content.Text>().text
                )
            }
        )

        testObserver.assertValue(ViewState.Success(listOf(titleView) + initial))

        vm.onClickListener(
            clicked = ListenerType.LongClick(
                target = paragraphs[0].id,
                dimensions = BlockDimensions(0, 0, 0, 0, 0, 0)
            )
        )

        coroutineTestRule.advanceTime(150)

        testObserver.assertValue(
            ViewState.Success(
                listOf(titleView.copy(mode = BlockView.Mode.READ)) + initial.mapIndexed { i, view ->
                    if (i == 0)
                        view.copy(mode = BlockView.Mode.READ, isSelected = true)
                    else
                        view.copy(mode = BlockView.Mode.READ)
                }
            )
        )

        vm.onTextInputClicked(target = paragraphs[1].id)

        testObserver.assertValue(
            ViewState.Success(
                listOf(titleView.copy(mode = BlockView.Mode.READ)) + initial.mapIndexed { i, view ->
                    if (i == 0 || i == 1)
                        view.copy(mode = BlockView.Mode.READ, isSelected = true)
                    else
                        view.copy(mode = BlockView.Mode.READ)
                }
            )
        )

        vm.onTextInputClicked(target = paragraphs[2].id)

        testObserver.assertValue(
            ViewState.Success(
                listOf(titleView.copy(mode = BlockView.Mode.READ)) + initial.mapIndexed { i, view ->
                    if (i == 0 || i == 1 || i == 2)
                        view.copy(mode = BlockView.Mode.READ, isSelected = true)
                    else
                        view.copy(mode = BlockView.Mode.READ)
                }
            )
        )

        vm.onTextInputClicked(target = paragraphs[0].id)

        testObserver.assertValue(
            ViewState.Success(
                listOf(titleView.copy(mode = BlockView.Mode.READ)) + initial.mapIndexed { i, view ->
                    if (i == 1 || i == 2)
                        view.copy(mode = BlockView.Mode.READ, isSelected = true)
                    else
                        view.copy(mode = BlockView.Mode.READ)
                }
            )
        )

        vm.onTextInputClicked(target = paragraphs[1].id)

        testObserver.assertValue(
            ViewState.Success(
                listOf(titleView.copy(mode = BlockView.Mode.READ)) + initial.mapIndexed { i, view ->
                    if (i == 2)
                        view.copy(mode = BlockView.Mode.READ, isSelected = true)
                    else
                        view.copy(mode = BlockView.Mode.READ)
                }
            )
        )

        vm.onTextInputClicked(target = paragraphs[2].id)

        // At this momemnt, we expect that all blocks are unselected, therefore we should exit to read mode.

        coroutineTestRule.advanceTime(EditorViewModel.DELAY_REFRESH_DOCUMENT_ON_EXIT_MULTI_SELECT_MODE)

        testObserver.assertValue(ViewState.Success(listOf(titleView) + initial))
    }

    @Test
    fun `should exit multi-select mode and unselect blocks`() {

        // SETUP

        val root = MockDataFactory.randomUuid()

        val paragraphs = listOf(
            Block(
                id = MockDataFactory.randomString(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.P
                ),
                children = emptyList(),
                fields = Block.Fields.empty()
            )
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields.empty(),
                content = Block.Content.Smart(),
                children = listOf(header.id) + paragraphs.map { it.id }
            )
        ) + listOf(header, title) + paragraphs

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        // TESTING

        val testObserver = vm.state.test()

        val titleView = BlockView.Title.Basic(
            id = title.id,
            text = title.content<TXT>().text,
            isFocused = false
        )

        val initial = listOf(
            paragraphs[0].let { p ->
                BlockView.Text.Paragraph(
                    id = p.id,
                    marks = emptyList(),
                    text = p.content<Block.Content.Text>().text
                )
            }
        )

        testObserver.assertValue(ViewState.Success(listOf(titleView) + initial))

        vm.onEnterMultiSelectModeClicked()

        coroutineTestRule.advanceTime(150)

        testObserver.assertValue(
            ViewState.Success(
                listOf(titleView.copy(mode = BlockView.Mode.READ)) + initial.map { view ->
                    view.copy(mode = BlockView.Mode.READ)
                }
            )
        )

        vm.onTextInputClicked(target = paragraphs[0].id)

        testObserver.assertValue(
            ViewState.Success(
                listOf(titleView.copy(mode = BlockView.Mode.READ)) + initial.mapIndexed { i, view ->
                    if (i == 0)
                        view.copy(mode = BlockView.Mode.READ, isSelected = true)
                    else
                        view.copy(mode = BlockView.Mode.READ)
                }
            )
        )

        vm.onExitMultiSelectModeClicked()

        coroutineTestRule.advanceTime(300)

        testObserver.assertValue(ViewState.Success(listOf(titleView) + initial))
    }

    private fun stubClosePage(
        response: Either<Throwable, Unit> = Either.Right(Unit)
    ) {
        closePage.stub {
            onBlocking { invoke(any()) } doReturn response
        }
    }

    private fun stubSplitBlocks(root: String) {
        splitBlock.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(
                Pair(
                    MockDataFactory.randomString(),
                    Payload(
                        context = root,
                        events = emptyList()
                    )
                )
            )
        }
    }

    fun stubOpenPage(
        context: Id = MockDataFactory.randomString(),
        events: List<Event> = emptyList()
    ) {
        openPage.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(
                Result.Success(
                    Payload(
                        context = context,
                        events = events
                    )
                )
            )
        }
    }

    fun stubObserveEvents(
        flow: Flow<List<Event>> = flowOf(),
        stubInterceptThreadStatus: Boolean = true
    ) {
        interceptEvents.stub {
            onBlocking { build(any()) } doReturn flow
        }
        if (stubInterceptThreadStatus) stubInterceptThreadStatus()
    }

    fun stubInterceptThreadStatus() {
        interceptThreadStatus.stub {
            onBlocking { build(any()) } doReturn emptyFlow()
        }
    }

    fun stubInterceptEvents(
        params: InterceptEvents.Params = InterceptEvents.Params(context = root),
        flow: Flow<List<Event>> = flowOf(),
        stubInterceptThreadStatus: Boolean = true
    ) {
        interceptEvents.stub {
            onBlocking { build(params) } doReturn flow
        }
        if (stubInterceptThreadStatus) stubInterceptThreadStatus()
    }

    private fun stubUpdateText() {
        updateText.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(Unit)
        }
    }

    private fun stubReplaceBlock(root: String) {
        replaceBlock.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(
                Pair(
                    MockDataFactory.randomString(),
                    Payload(
                        context = root,
                        events = emptyList()
                    )
                )
            )
        }
    }

    private fun stubCreateBlock(root: String) {
        createBlock.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(
                Pair(
                    MockDataFactory.randomString(), Payload(
                        context = root,
                        events = listOf()
                    )
                )
            )
        }
    }

    private fun stubUpdateTitle() {
        updateTitle.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(Unit)
        }
    }

    private fun stubDownloadFile() {
        downloadFile.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(Unit)
        }
    }

    private fun stubUpdateTextColor(root: String) {
        updateTextColor.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(
                Payload(
                    context = root,
                    events = emptyList()
                )
            )
        }
    }

    private fun stubUpdateTextStyle(
        payload: Payload = Payload(
            context = MockDataFactory.randomUuid(),
            events = emptyList()
        )
    ) {
        updateTextStyle.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(payload)
        }
    }

    private fun stubTurnIntoStyle(
        payload: Payload = Payload(
            context = MockDataFactory.randomUuid(),
            events = emptyList()
        )
    ) {
        turnIntoStyle.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(payload)
        }
    }

    private fun stubGetDefaultObjectType(type: String? = null, name: String? = null) {
        getDefaultEditorType.stub {
            onBlocking { invoke(Unit) } doReturn Either.Right(
                GetDefaultEditorType.Response(
                    type,
                    null
                )
            )
        }
    }

    fun buildViewModel(urlBuilder: UrlBuilder = builder) {

        val storage = Editor.Storage()
        val proxies = Editor.Proxer()
        val memory = Editor.Memory(
            selections = SelectionStateHolder.Default()
        )
        updateDetail = UpdateDetail(repo)
        setDocCoverImage = SetDocCoverImage(repo)
        setDocImageIcon = SetDocumentImageIcon(repo)
        downloadUnsplashImage = DownloadUnsplashImage(unsplashRepo)

        vm = EditorViewModel(
            openPage = openPage,
            closePage = closePage,
            createPage = createPage,
            createObject = createObject,
            interceptEvents = interceptEvents,
            interceptThreadStatus = interceptThreadStatus,
            updateLinkMarks = updateLinkMark,
            removeLinkMark = removeLinkMark,
            reducer = DocumentExternalEventReducer(),
            urlBuilder = urlBuilder,
            renderer = DefaultBlockViewRenderer(
                urlBuilder = urlBuilder,
                toggleStateHolder = ToggleStateHolder.Default(),
                coverImageHashProvider = coverImageHashProvider
            ),
            setObjectIsArchived = setObjectIsArchived,
            createDocument = createDocument,
            createNewDocument = createNewDocument,
            analytics = analytics,
            getDefaultEditorType = getDefaultEditorType,
            orchestrator = Orchestrator(
                createBlock = createBlock,
                replaceBlock = replaceBlock,
                updateTextColor = updateTextColor,
                duplicateBlock = duplicateBlock,
                downloadFile = downloadFile,
                undo = undo,
                redo = redo,
                updateTitle = updateTitle,
                updateText = updateText,
                updateCheckbox = updateCheckbox,
                updateTextStyle = updateTextStyle,
                updateBackgroundColor = updateBackgroundColor,
                mergeBlocks = mergeBlocks,
                uploadBlock = uploadBlock,
                splitBlock = splitBlock,
                unlinkBlocks = unlinkBlocks,
                updateDivider = updateDivider,
                memory = memory,
                stores = storage,
                proxies = proxies,
                textInteractor = Interactor.TextInteractor(
                    proxies = proxies,
                    stores = storage,
                    matcher = DefaultPatternMatcher()
                ),
                updateAlignment = updateAlignment,
                setupBookmark = setupBookmark,
                createBookmark = createBookmark,
                paste = paste,
                copy = copy,
                move = move,
                turnIntoDocument = turnIntoDocument,
                analytics = analytics,
                updateFields = updateFields,
                setRelationKey = setRelationKey,
                turnIntoStyle = turnIntoStyle,
                updateBlocksMark = updateBlocksMark,
                setObjectType = setObjectType,
            ),
            dispatcher = Dispatcher.Default(),
            detailModificationManager = InternalDetailModificationManager(storage.details),
            updateDetail = updateDetail,
            getCompatibleObjectTypes = getCompatibleObjectTypes,
            objectTypesProvider = objectTypesProvider,
            searchObjects = searchObjects,
            findObjectSetForType = findObjectSetForType,
            createObjectSet = createObjectSet,
            copyFileToCache = copyFileToCacheDirectory,
            downloadUnsplashImage = downloadUnsplashImage,
            setDocCoverImage = setDocCoverImage,
            setDocImageIcon = setDocImageIcon,
            delegator = delegator,
            templateDelegate = editorTemplateDelegate
        )
    }

    @Test
    fun `should enter multi select mode and selections should be empty`() {
        // SETUP

        val root = MockDataFactory.randomUuid()

        val paragraphs = listOf(
            Block(
                id = MockDataFactory.randomString(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.P
                ),
                children = emptyList(),
                fields = Block.Fields.empty()
            ),
            Block(
                id = MockDataFactory.randomString(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.P
                ),
                children = emptyList(),
                fields = Block.Fields.empty()
            ),
            Block(
                id = MockDataFactory.randomString(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.P
                ),
                children = emptyList(),
                fields = Block.Fields.empty()
            )
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields.empty(),
                content = Block.Content.Smart(),
                children = listOf(header.id) + paragraphs.map { it.id }
            )
        ) + listOf(header, title) + paragraphs

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        // TESTING

        val testObserver = vm.state.test()

        val titleView = BlockView.Title.Basic(
            id = title.id,
            text = title.content<TXT>().text,
            isFocused = false
        )

        val initial = listOf(
            paragraphs[0].let { p ->
                BlockView.Text.Paragraph(
                    id = p.id,
                    marks = emptyList(),
                    text = p.content<Block.Content.Text>().text
                )
            },
            paragraphs[1].let { p ->
                BlockView.Text.Paragraph(
                    id = p.id,
                    marks = emptyList(),
                    text = p.content<Block.Content.Text>().text
                )
            },
            paragraphs[2].let { p ->
                BlockView.Text.Paragraph(
                    id = p.id,
                    marks = emptyList(),
                    text = p.content<Block.Content.Text>().text
                )
            }
        )

        testObserver.assertValue(ViewState.Success(listOf(titleView) + initial))

        vm.onEnterMultiSelectModeClicked()

        coroutineTestRule.advanceTime(150)

        assertEquals(
            expected = 0,
            actual = vm.currentSelection().size
        )
    }

    @Test
    fun `should be two selected blocks in multi select mode`() {
        // SETUP

        val root = MockDataFactory.randomUuid()

        val paragraphs = listOf(
            Block(
                id = MockDataFactory.randomString(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.P
                ),
                children = emptyList(),
                fields = Block.Fields.empty()
            ),
            Block(
                id = MockDataFactory.randomString(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.P
                ),
                children = emptyList(),
                fields = Block.Fields.empty()
            ),
            Block(
                id = MockDataFactory.randomString(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.P
                ),
                children = emptyList(),
                fields = Block.Fields.empty()
            )
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields.empty(),
                content = Block.Content.Smart(),
                children = listOf(header.id) + paragraphs.map { it.id }
            )
        ) + listOf(header, title) + paragraphs

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        // TESTING

        val testObserver = vm.state.test()

        val titleView = BlockView.Title.Basic(
            id = title.id,
            text = title.content<TXT>().text,
            isFocused = false
        )

        val initial = listOf(
            paragraphs[0].let { p ->
                BlockView.Text.Paragraph(
                    id = p.id,
                    marks = emptyList(),
                    text = p.content<Block.Content.Text>().text
                )
            },
            paragraphs[1].let { p ->
                BlockView.Text.Paragraph(
                    id = p.id,
                    marks = emptyList(),
                    text = p.content<Block.Content.Text>().text
                )
            },
            paragraphs[2].let { p ->
                BlockView.Text.Paragraph(
                    id = p.id,
                    marks = emptyList(),
                    text = p.content<Block.Content.Text>().text
                )
            }
        )

        testObserver.assertValue(ViewState.Success(listOf(titleView) + initial))

        vm.onEnterMultiSelectModeClicked()

        coroutineTestRule.advanceTime(150)

        assertEquals(
            expected = 0,
            actual = vm.currentSelection().size
        )

        vm.onTextInputClicked(target = paragraphs[1].id)
        vm.onTextInputClicked(target = paragraphs[2].id)

        assertEquals(
            expected = 2,
            actual = vm.currentSelection().size
        )
    }

    @Test
    fun `should be zero selected blocks after done click`() {
        // SETUP

        val root = MockDataFactory.randomUuid()

        val paragraphs = listOf(
            Block(
                id = MockDataFactory.randomString(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.P
                ),
                children = emptyList(),
                fields = Block.Fields.empty()
            ),
            Block(
                id = MockDataFactory.randomString(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.P
                ),
                children = emptyList(),
                fields = Block.Fields.empty()
            ),
            Block(
                id = MockDataFactory.randomString(),
                content = Block.Content.Text(
                    marks = emptyList(),
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.P
                ),
                children = emptyList(),
                fields = Block.Fields.empty()
            )
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields.empty(),
                content = Block.Content.Smart(),
                children = listOf(header.id) + paragraphs.map { it.id }
            )
        ) + listOf(header, title) + paragraphs

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)

        // TESTING

        val testObserver = vm.state.test()

        val titleView = BlockView.Title.Basic(
            id = title.id,
            text = title.content<TXT>().text,
            isFocused = false
        )

        val initial = listOf(
            paragraphs[0].let { p ->
                BlockView.Text.Paragraph(
                    id = p.id,
                    marks = emptyList(),
                    text = p.content<Block.Content.Text>().text
                )
            },
            paragraphs[1].let { p ->
                BlockView.Text.Paragraph(
                    id = p.id,
                    marks = emptyList(),
                    text = p.content<Block.Content.Text>().text
                )
            },
            paragraphs[2].let { p ->
                BlockView.Text.Paragraph(
                    id = p.id,
                    marks = emptyList(),
                    text = p.content<Block.Content.Text>().text
                )
            }
        )

        testObserver.assertValue(ViewState.Success(listOf(titleView) + initial))

        vm.onEnterMultiSelectModeClicked()

        coroutineTestRule.advanceTime(150)

        assertEquals(
            expected = 0,
            actual = vm.currentSelection().size
        )

        vm.onTextInputClicked(target = paragraphs[1].id)
        vm.onTextInputClicked(target = paragraphs[2].id)

        assertEquals(
            expected = 2,
            actual = vm.currentSelection().size
        )

        vm.onExitMultiSelectModeClicked()

        coroutineTestRule.advanceTime(300)

        assertEquals(
            expected = 0,
            actual = vm.currentSelection().size
        )
    }

    @Test
    fun `should not update text style in multi select mode`() {

        val id1 = MockDataFactory.randomUuid()
        val id2 = MockDataFactory.randomUuid()
        val blocks = listOf(
            Block(
                id = id1,
                content = Block.Content.Text(
                    marks = listOf(
                        Block.Content.Text.Mark(
                            range = 0..7, type = Block.Content.Text.Mark.Type.BOLD
                        )
                    ),
                    text = "Foo Bar",
                    style = Block.Content.Text.Style.P,
                    align = Block.Align.AlignCenter
                ),
                children = emptyList(),
                fields = Block.Fields.empty()
            ),
            Block(
                id = id2,
                content = Block.Content.Text(
                    marks = emptyList(),
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.P
                ),
                children = emptyList(),
                fields = Block.Fields.empty()
            )
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields.empty(),
                content = Block.Content.Smart(),
                children = blocks.map { it.id }
            )
        ) + blocks

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
            emit(
                listOf(
                    Event.Command.ShowObject(
                        root = root,
                        blocks = page,
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        vm.onStart(root)

        coroutineTestRule.advanceTime(100)


        // TESTING

        val stateBefore = vm.controlPanelViewState.value

        assertNotNull(stateBefore)

        assertTrue(stateBefore.navigationToolbar.isVisible)
        assertFalse(stateBefore.styleTextToolbar.isVisible)

        vm.onClickListener(ListenerType.LongClick(target = blocks[0].id))
        vm.onMultiSelectStyleButtonClicked()

        val actual = vm.controlPanelViewState.test().value()
        val expected = ControlPanelState(
            navigationToolbar = ControlPanelState.Toolbar.Navigation(
                isVisible = false
            ),
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = false
            ),
            styleTextToolbar = ControlPanelState.Toolbar.Styling(
                isVisible = true,
                state = StyleToolbarState.Text(Block.Content.Text.Style.P)
            ),
            multiSelect = ControlPanelState.Toolbar.MultiSelect(
                isVisible = true,
                count = 1
            ),
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar.reset(),
            slashWidget = ControlPanelState.Toolbar.SlashWidget.reset()
        )

        assertEquals(expected, actual)

        vm.onStylingToolbarEvent(event = StylingEvent.Markup.Italic)

        verifyNoMoreInteractions(updateText)

        coroutineTestRule.advanceTime(200)
    }
}