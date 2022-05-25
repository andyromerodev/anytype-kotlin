package com.anytypeio.anytype.features.editor.base

import android.os.Bundle
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import com.anytypeio.anytype.R
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.DocumentInfo
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.domain.`object`.ObjectTypesProvider
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
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
import com.anytypeio.anytype.domain.clipboard.Clipboard
import com.anytypeio.anytype.domain.clipboard.Copy
import com.anytypeio.anytype.domain.clipboard.Paste
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.cover.RemoveDocCover
import com.anytypeio.anytype.domain.cover.SetDocCoverImage
import com.anytypeio.anytype.domain.dataview.interactor.GetCompatibleObjectTypes
import com.anytypeio.anytype.domain.dataview.interactor.SearchObjects
import com.anytypeio.anytype.domain.dataview.interactor.SetRelationKey
import com.anytypeio.anytype.domain.download.DownloadFile
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.icon.DocumentEmojiIconProvider
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
import com.anytypeio.anytype.domain.status.ThreadStatusChannel
import com.anytypeio.anytype.domain.templates.ApplyTemplate
import com.anytypeio.anytype.domain.templates.GetTemplates
import com.anytypeio.anytype.domain.unsplash.DownloadUnsplashImage
import com.anytypeio.anytype.domain.unsplash.UnsplashRepository
import com.anytypeio.anytype.presentation.common.Delegator
import com.anytypeio.anytype.presentation.editor.DocumentExternalEventReducer
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.editor.EditorViewModelFactory
import com.anytypeio.anytype.presentation.editor.cover.CoverImageHashProvider
import com.anytypeio.anytype.presentation.editor.editor.Interactor
import com.anytypeio.anytype.presentation.editor.editor.InternalDetailModificationManager
import com.anytypeio.anytype.presentation.editor.editor.Orchestrator
import com.anytypeio.anytype.presentation.editor.editor.Proxy
import com.anytypeio.anytype.presentation.editor.editor.pattern.DefaultPatternMatcher
import com.anytypeio.anytype.presentation.editor.render.DefaultBlockViewRenderer
import com.anytypeio.anytype.presentation.editor.selection.SelectionStateHolder
import com.anytypeio.anytype.presentation.editor.template.DefaultEditorTemplateDelegate
import com.anytypeio.anytype.presentation.editor.template.EditorTemplateDelegate
import com.anytypeio.anytype.presentation.editor.toggle.ToggleStateHolder
import com.anytypeio.anytype.presentation.util.CopyFileToCacheDirectory
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub

open class EditorTestSetup {

    lateinit var createObject: CreateObject
    lateinit var setObjectIsArchived: SetObjectIsArchived
    lateinit var createDocument: CreateDocument
    lateinit var downloadFile: DownloadFile
    lateinit var undo: Undo
    lateinit var redo: Redo
    lateinit var copy: Copy
    lateinit var paste: Paste
    lateinit var updateTitle: UpdateTitle
    lateinit var updateAlignment: UpdateAlignment
    lateinit var replaceBlock: ReplaceBlock
    lateinit var setupBookmark: SetupBookmark
    lateinit var createBookmark: CreateBookmark
    lateinit var uploadBlock: UploadBlock
    lateinit var splitBlock: SplitBlock
    lateinit var createPage: CreatePage
    lateinit var updateBackgroundColor: UpdateBackgroundColor
    lateinit var move: Move
    lateinit var setRelationKey: SetRelationKey
    lateinit var updateDetail: UpdateDetail
    lateinit var getCompatibleObjectTypes: GetCompatibleObjectTypes

    @Mock
    lateinit var copyFileToCacheDirectory: CopyFileToCacheDirectory

    @Mock
    lateinit var openPage: OpenPage
    @Mock
    lateinit var closePage: CloseBlock
    @Mock
    lateinit var updateText: UpdateText
    @Mock
    lateinit var createBlock: CreateBlock
    @Mock
    lateinit var interceptEvents: InterceptEvents
    @Mock
    lateinit var updateCheckbox: UpdateCheckbox
    @Mock
    lateinit var unlinkBlocks: UnlinkBlocks

    lateinit var getSearchObjects: SearchObjects
    @Mock
    lateinit var duplicateBlock: DuplicateBlock

    @Mock
    lateinit var updateTextStyle: UpdateTextStyle

    @Mock
    lateinit var updateTextColor: UpdateTextColor

    @Mock
    lateinit var updateLinkMarks: UpdateLinkMarks

    @Mock
    lateinit var removeLinkMark: RemoveLinkMark

    @Mock
    lateinit var mergeBlocks: MergeBlocks

    lateinit var editorTemplateDelegate: EditorTemplateDelegate
    lateinit var getTemplates: GetTemplates
    lateinit var applyTemplate: ApplyTemplate

    lateinit var createNewDocument: CreateNewDocument
    lateinit var interceptThreadStatus: InterceptThreadStatus

    lateinit var setDocCoverImage: SetDocCoverImage
    lateinit var setDocImageIcon: SetDocumentImageIcon
    lateinit var removeDocCover: RemoveDocCover

    lateinit var updateFields: UpdateFields
    lateinit var turnIntoDocument: TurnIntoDocument
    lateinit var turnIntoStyle: TurnIntoStyle
    lateinit var setObjectType: SetObjectType

    lateinit var getDefaultEditorType: GetDefaultEditorType

    private lateinit var findObjectSetForType: FindObjectSetForType
    private lateinit var createObjectSet: CreateObjectSet

    lateinit var downloadUnsplashImage: DownloadUnsplashImage

    @Mock
    lateinit var updateDivider: UpdateDivider

    @Mock
    lateinit var uriMatcher: Clipboard.UriMatcher

    @Mock
    lateinit var updateBlocksMark: UpdateBlocksMark

    @Mock
    lateinit var repo: BlockRepository

    @Mock
    lateinit var unsplashRepository: UnsplashRepository

    @Mock
    lateinit var userSettingsRepository: UserSettingsRepository

    @Mock
    lateinit var coverImageHashProvider: CoverImageHashProvider

    @Mock
    lateinit var clipboard: Clipboard

    @Mock
    lateinit var gateway: Gateway

    @Mock
    lateinit var analytics: Analytics

    @Mock
    lateinit var threadStatusChannel: ThreadStatusChannel

    @Mock
    lateinit var documentEmojiIconProvider: DocumentEmojiIconProvider

    @Mock
    lateinit var objectTypesProvider: ObjectTypesProvider

    val root: String = "rootId123"

    private val urlBuilder by lazy {
        UrlBuilder(
            gateway = gateway
        )
    }

    private val intents = Proxy.Intents()

    private val stores = Editor.Storage()

    private val proxies = Editor.Proxer(
        intents = intents
    )

    open fun setup() {
        MockitoAnnotations.openMocks(this)

        val dispatchers = AppCoroutineDispatchers(
            io = StandardTestDispatcher(),
            main = StandardTestDispatcher(),
            computation = StandardTestDispatcher()
        )

        splitBlock = SplitBlock(repo)
        createPage = CreatePage(repo)
        setObjectIsArchived = SetObjectIsArchived(repo)
        createDocument = CreateDocument(repo, documentEmojiIconProvider)
        undo = Undo(repo)
        redo = Redo(repo)
        replaceBlock = ReplaceBlock(repo)
        setupBookmark = SetupBookmark(repo)
        updateAlignment = UpdateAlignment(repo)
        updateTitle = UpdateTitle(repo)
        uploadBlock = UploadBlock(repo)
        createObject = CreateObject(repo, documentEmojiIconProvider)
        setRelationKey = SetRelationKey(repo)
        turnIntoDocument = TurnIntoDocument(repo)
        updateFields = UpdateFields(repo)
        setObjectType = SetObjectType(repo)
        createNewDocument = CreateNewDocument(repo, documentEmojiIconProvider)
        getSearchObjects = SearchObjects(repo)
        interceptThreadStatus = InterceptThreadStatus(channel = threadStatusChannel)
        downloadUnsplashImage = DownloadUnsplashImage(unsplashRepository)
        downloadFile = DownloadFile(
            downloader = mock(),
            context = Dispatchers.Main
        )
        copy = Copy(
            repo = repo,
            clipboard = clipboard
        )

        paste = Paste(
            repo = repo,
            clipboard = clipboard,
            matcher = uriMatcher
        )
        move = Move(repo)

        updateBackgroundColor = UpdateBackgroundColor(repo)

        setDocCoverImage = SetDocCoverImage(repo)
        setDocImageIcon = SetDocumentImageIcon(repo)
        removeDocCover = RemoveDocCover(repo)
        turnIntoStyle = TurnIntoStyle(repo)
        updateDetail = UpdateDetail(repo)
        getCompatibleObjectTypes = GetCompatibleObjectTypes(repo)
        getDefaultEditorType = GetDefaultEditorType(userSettingsRepository)
        createObjectSet = CreateObjectSet(repo)
        findObjectSetForType = FindObjectSetForType(repo)
        createBookmark = CreateBookmark(repo)
        applyTemplate = ApplyTemplate(
            repo = repo,
            dispatchers = dispatchers
        )
        getTemplates = GetTemplates(
            repo = repo,
            dispatchers = dispatchers
        )

        editorTemplateDelegate = DefaultEditorTemplateDelegate(
            getTemplates = getTemplates,
            applyTemplate = applyTemplate
        )

        TestEditorFragment.testViewModelFactory = EditorViewModelFactory(
            openPage = openPage,
            closeObject = closePage,
            interceptEvents = interceptEvents,
            updateLinkMarks = updateLinkMarks,
            removeLinkMark = removeLinkMark,
            createPage = createPage,
            createObject = createObject,
            documentEventReducer = DocumentExternalEventReducer(),
            setObjectIsArchived = setObjectIsArchived,
            createDocument = createDocument,
            urlBuilder = urlBuilder,
            renderer = DefaultBlockViewRenderer(
                urlBuilder = urlBuilder,
                toggleStateHolder = ToggleStateHolder.Default(),
                coverImageHashProvider = coverImageHashProvider
            ),
            orchestrator = Orchestrator(
                createBlock = createBlock,
                splitBlock = splitBlock,
                unlinkBlocks = unlinkBlocks,
                updateCheckbox = updateCheckbox,
                updateTextStyle = updateTextStyle,
                updateText = updateText,
                updateBackgroundColor = updateBackgroundColor,
                undo = undo,
                redo = redo,
                copy = copy,
                paste = paste,
                duplicateBlock = duplicateBlock,
                updateAlignment = updateAlignment,
                downloadFile = downloadFile,
                mergeBlocks = mergeBlocks,
                updateTitle = updateTitle,
                updateTextColor = updateTextColor,
                replaceBlock = replaceBlock,
                setupBookmark = setupBookmark,
                setRelationKey = setRelationKey,
                memory = Editor.Memory(
                    selections = SelectionStateHolder.Default()
                ),
                stores = stores,
                proxies = proxies,
                textInteractor = Interactor.TextInteractor(
                    proxies = proxies,
                    stores = stores,
                    matcher = DefaultPatternMatcher()
                ),
                uploadBlock = uploadBlock,
                move = move,
                analytics = analytics,
                updateDivider = updateDivider,
                updateFields = updateFields,
                turnIntoDocument = turnIntoDocument,
                turnIntoStyle = turnIntoStyle,
                updateBlocksMark = updateBlocksMark,
                setObjectType = setObjectType,
                createBookmark = createBookmark
            ),
            createNewDocument = createNewDocument,
            interceptThreadStatus = interceptThreadStatus,
            analytics = analytics,
            dispatcher = Dispatcher.Default(),
            detailModificationManager = InternalDetailModificationManager(stores.details),
            updateDetail = updateDetail,
            getCompatibleObjectTypes = getCompatibleObjectTypes,
            objectTypesProvider = objectTypesProvider,
            searchObjects = getSearchObjects,
            getDefaultEditorType = getDefaultEditorType,
            createObjectSet = createObjectSet,
            findObjectSetForType = findObjectSetForType,
            copyFileToCacheDirectory = copyFileToCacheDirectory,
            downloadUnsplashImage = downloadUnsplashImage,
            delegator = Delegator.Default(),
            setDocCoverImage = setDocCoverImage,
            setDocImageIcon = setDocImageIcon,
            editorTemplateDelegate = editorTemplateDelegate
        )
    }

    /**
     * STUBBING
     */

    fun stubInterceptEvents() {
        interceptEvents.stub {
            onBlocking { build(any()) } doReturn emptyFlow()
        }
    }

    fun stubInterceptThreadStatus(
        params: InterceptThreadStatus.Params = InterceptThreadStatus.Params(ctx = root)
    ) {
        interceptThreadStatus.stub {
            onBlocking { build(params) } doReturn emptyFlow()
        }
    }

    fun stubOpenDocument(
        document: List<Block>,
        details: Block.Details = Block.Details(),
        relations: List<Relation> = emptyList()
    ) {
        openPage.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(
                Result.Success(
                    Payload(
                        context = root,
                        events = listOf(
                            Event.Command.ShowObject(
                                context = root,
                                root = root,
                                details = details,
                                blocks = document,
                                relations = relations
                            )
                        )
                    )
                )
            )
        }
    }

    fun stubCreateBlock(
        params: CreateBlock.Params,
        events: List<Event.Command>
    ) {
        createBlock.stub {
            onBlocking { invoke(params) } doReturn Either.Right(
                Pair(
                    MockDataFactory.randomUuid(),
                    Payload(context = root, events = events)
                )
            )
        }
    }

    fun stubSplitBlocks(
        command: Command.Split,
        new: Id,
        events: List<Event.Command>
    ) {
        repo.stub {
            onBlocking {
                split(command = command)
            } doReturn Pair(new, Payload(context = root, events = events))
        }
    }

    fun stubUpdateText() {
        updateText.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(Unit)
        }
    }

    fun stubGetObjectTypes(objectTypes: List<ObjectType>) {
        repo.stub {
            onBlocking { getObjectTypes() } doReturn objectTypes
        }
    }

    fun stubGetListPages(pages: List<DocumentInfo>) {
        repo.stub {
            onBlocking { getListPages() } doReturn pages
        }
    }

    fun stubUpdateTextStyle(
        events: List<Event> = emptyList()
    ) {
        updateTextStyle.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(
                Payload(
                    context = root,
                    events = events
                )
            )
        }
    }

    fun stubAnalytics() {
        analytics.stub {
            onBlocking { registerEvent(any()) } doReturn Unit
        }
    }

    fun launch(args: Bundle): FragmentScenario<TestEditorFragment> {
        return launchFragmentInContainer(
            fragmentArgs = args,
            themeResId = R.style.AppTheme
        )
    }
}