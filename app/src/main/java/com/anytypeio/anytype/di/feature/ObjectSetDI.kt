package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.feature.cover.UnsplashSubComponent
import com.anytypeio.anytype.di.feature.relations.RelationAddToDataViewSubComponent
import com.anytypeio.anytype.di.feature.relations.RelationCreateFromScratchForDataViewSubComponent
import com.anytypeio.anytype.di.feature.sets.CreateFilterSubComponent
import com.anytypeio.anytype.di.feature.sets.ModifyFilterSubComponent
import com.anytypeio.anytype.di.feature.sets.SelectFilterRelationSubComponent
import com.anytypeio.anytype.di.feature.sets.viewer.ViewerCardSizeSelectSubcomponent
import com.anytypeio.anytype.di.feature.sets.viewer.ViewerImagePreviewSelectSubcomponent
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.interactor.UpdateText
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.cover.SetDocCoverImage
import com.anytypeio.anytype.domain.dataview.interactor.AddNewRelationToDataView
import com.anytypeio.anytype.domain.dataview.interactor.CreateDataViewRecord
import com.anytypeio.anytype.domain.dataview.interactor.SearchObjects
import com.anytypeio.anytype.domain.dataview.interactor.SetActiveViewer
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewRecord
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.domain.event.interactor.EventChannel
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.icon.SetDocumentImageIcon
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.SetObjectIsArchived
import com.anytypeio.anytype.domain.page.CloseBlock
import com.anytypeio.anytype.domain.relations.AddFileToRecord
import com.anytypeio.anytype.domain.relations.DeleteRelationFromDataView
import com.anytypeio.anytype.domain.sets.OpenObjectSet
import com.anytypeio.anytype.domain.status.InterceptThreadStatus
import com.anytypeio.anytype.domain.status.ThreadStatusChannel
import com.anytypeio.anytype.domain.templates.GetTemplates
import com.anytypeio.anytype.domain.unsplash.DownloadUnsplashImage
import com.anytypeio.anytype.domain.unsplash.UnsplashRepository
import com.anytypeio.anytype.presentation.common.Action
import com.anytypeio.anytype.presentation.common.Delegator
import com.anytypeio.anytype.presentation.relations.providers.DataViewObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.DataViewObjectValueProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectDetailProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectTypeProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.presentation.sets.ObjectSet
import com.anytypeio.anytype.presentation.sets.ObjectSetRecordCache
import com.anytypeio.anytype.presentation.sets.ObjectSetReducer
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModelFactory
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.sets.ObjectSetFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow

@Subcomponent(modules = [ObjectSetModule::class])
@PerScreen
interface ObjectSetSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun module(module: ObjectSetModule): Builder
        fun build(): ObjectSetSubComponent
    }

    fun inject(fragment: ObjectSetFragment)

    fun objectSetRecordComponent(): ObjectSetRecordSubComponent.Builder
    fun viewerCustomizeSubComponent(): ViewerCustomizeSubComponent.Builder
    fun viewerSortBySubComponent(): ViewerSortBySubComponent.Builder
    fun viewerFilterBySubComponent(): ViewerFilterSubComponent.Builder
    fun createDataViewViewerSubComponent(): CreateDataViewViewerSubComponent.Builder
    fun editDataViewViewerComponent(): EditDataViewViewerSubComponent.Builder
    fun objectRelationValueComponent(): ObjectSetObjectRelationValueSubComponent.Builder
    fun manageViewerComponent(): ManageViewerSubComponent.Builder
    fun viewerRelationsComponent(): ViewerRelationsSubComponent.Builder
    fun viewerCardSizeSelectComponent(): ViewerCardSizeSelectSubcomponent.Builder
    fun viewerImagePreviewSelectComponent(): ViewerImagePreviewSelectSubcomponent.Builder
    fun relationAddToDataViewComponent() : RelationAddToDataViewSubComponent.Builder
    fun relationCreateFromScratchForDataViewComponent() : RelationCreateFromScratchForDataViewSubComponent.Builder
    fun dataviewViewerActionComponent(): DataViewViewerActionSubComponent.Builder
    fun selectSortRelationComponent(): SelectSortRelationSubComponent.Builder
    fun selectFilterRelationComponent(): SelectFilterRelationSubComponent.Builder
    fun createFilterComponent(): CreateFilterSubComponent.Builder
    fun modifyFilterComponent(): ModifyFilterSubComponent.Builder
    fun viewerSortComponent(): ViewerSortSubComponent.Builder
    fun modifyViewerSortComponent(): ModifyViewerSortSubComponent.Builder
    fun relationTextValueComponent(): RelationTextValueSubComponent.Builder
    fun relationDateValueComponent(): RelationDataValueSubComponent.Builder

    fun objectSetMenuComponent() : ObjectSetMenuComponent.Builder
    fun objectSetIconPickerComponent() : ObjectSetIconPickerComponent.Builder
    fun objectSetCoverComponent() : SelectCoverObjectSetSubComponent.Builder
    fun objectUnsplashComponent() : UnsplashSubComponent.Builder
}

@Module
object ObjectSetModule {

    @JvmStatic
    @Provides
    @PerScreen
    fun provideSetDocumentImageIconUseCase(
        repo: BlockRepository
    ): SetDocumentImageIcon = SetDocumentImageIcon(repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideObjectSetViewModelFactory(
        openObjectSet: OpenObjectSet,
        closeBlock: CloseBlock,
        setActiveViewer: SetActiveViewer,
        addDataViewRelation: AddNewRelationToDataView,
        updateDataViewViewer: UpdateDataViewViewer,
        updateDataViewRecord: UpdateDataViewRecord,
        updateText: UpdateText,
        interceptEvents: InterceptEvents,
        interceptThreadStatus: InterceptThreadStatus,
        createDataViewRecord: CreateDataViewRecord,
        reducer: ObjectSetReducer,
        dispatcher: Dispatcher<Payload>,
        delegator: Delegator<Action>,
        objectSetRecordCache: ObjectSetRecordCache,
        urlBuilder: UrlBuilder,
        session: ObjectSetSession,
        analytics: Analytics,
        downloadUnsplashImage: DownloadUnsplashImage,
        setDocCoverImage: SetDocCoverImage,
        getTemplates: GetTemplates
    ): ObjectSetViewModelFactory = ObjectSetViewModelFactory(
        openObjectSet = openObjectSet,
        closeBlock = closeBlock,
        setActiveViewer = setActiveViewer,
        addDataViewRelation = addDataViewRelation,
        updateDataViewViewer = updateDataViewViewer,
        updateDataViewRecord = updateDataViewRecord,
        createDataViewRecord = createDataViewRecord,
        updateText = updateText,
        interceptEvents = interceptEvents,
        interceptThreadStatus = interceptThreadStatus,
        reducer = reducer,
        dispatcher = dispatcher,
        delegator = delegator,
        objectSetRecordCache = objectSetRecordCache,
        urlBuilder = urlBuilder,
        session = session,
        analytics = analytics,
        downloadUnsplashImage = downloadUnsplashImage,
        setDocCoverImage = setDocCoverImage,
        getTemplates = getTemplates
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideOpenObjectSetUseCase(
        repo: BlockRepository,
        auth: AuthRepository
    ): OpenObjectSet = OpenObjectSet(repo = repo, auth = auth)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideSetActiveViewerUseCase(
        repo: BlockRepository
    ): SetActiveViewer = SetActiveViewer(repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideAddDataViewRelationUseCase(
        repo: BlockRepository
    ): AddNewRelationToDataView = AddNewRelationToDataView(repo = repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideUpdateDataViewViewerUseCase(
        repo: BlockRepository
    ): UpdateDataViewViewer = UpdateDataViewViewer(repo = repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideCreateDataViewRecordUseCase(
        repo: BlockRepository
    ): CreateDataViewRecord = CreateDataViewRecord(repo = repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideUpdateDataViewRecordUseCase(
        repo: BlockRepository
    ): UpdateDataViewRecord = UpdateDataViewRecord(repo = repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideUpdateTextUseCase(
        repo: BlockRepository
    ): UpdateText = UpdateText(repo = repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideInterceptEventsUseCase(
        channel: EventChannel
    ): InterceptEvents = InterceptEvents(
        channel = channel,
        context = Dispatchers.IO
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideInterceptThreadStatus(
        channel: ThreadStatusChannel
    ) : InterceptThreadStatus = InterceptThreadStatus(
        channel = channel
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideCloseBlockUseCase(
        repo: BlockRepository
    ): CloseBlock = CloseBlock(repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideObjectSetReducer(): ObjectSetReducer = ObjectSetReducer()

    @JvmStatic
    @Provides
    @PerScreen
    fun provideState(
        reducer: ObjectSetReducer
    ): StateFlow<ObjectSet> = reducer.state

    @JvmStatic
    @Provides
    @PerScreen
    fun provideObjectSetSession(): ObjectSetSession = ObjectSetSession()

    @JvmStatic
    @Provides
    @PerScreen
    fun provideDispatcher(): Dispatcher<Payload> = Dispatcher.Default()

    @JvmStatic
    @Provides
    @PerScreen
    fun provideDelegator() : Delegator<Action> = Delegator.Default()

    @JvmStatic
    @Provides
    @PerScreen
    fun provideObjectSetRecordCache(): ObjectSetRecordCache = ObjectSetRecordCache()

    @JvmStatic
    @Provides
    @PerScreen
    fun provideDataViewObjectRelationProvider(
        state: StateFlow<ObjectSet>
    ) : ObjectRelationProvider = DataViewObjectRelationProvider(state)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideDataViewObjectValueProvider(
        state: StateFlow<ObjectSet>,
        session: ObjectSetSession
    ) : ObjectValueProvider = DataViewObjectValueProvider(state, session)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideObjectTypeProvider(
        state: StateFlow<ObjectSet>,
    ) : ObjectTypeProvider = object : ObjectTypeProvider {
        override fun provide(): List<ObjectType> = state.value.objectTypes
    }

    @JvmStatic
    @Provides
    @PerScreen
    fun provideObjectDetailProvider(
        state: StateFlow<ObjectSet>,
    ) : ObjectDetailProvider = object : ObjectDetailProvider {
        override fun provide(): Map<Id, Block.Fields> = state.value.details
    }

    @JvmStatic
    @Provides
    @PerScreen
    fun provideUpdateDetailUseCase(
        repository: BlockRepository
    ) : UpdateDetail = UpdateDetail(repository)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideAddFileToRecordUseCase(
        repo: BlockRepository
    ): AddFileToRecord = AddFileToRecord(repo = repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideArchiveDocumentUseCase(
        repo: BlockRepository
    ): SetObjectIsArchived = SetObjectIsArchived(
        repo = repo
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideSearchObjectsUseCase(
        repo: BlockRepository
    ): SearchObjects = SearchObjects(repo = repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideDeleteRelationFromDataViewUseCase(
        repo: BlockRepository
    ): DeleteRelationFromDataView = DeleteRelationFromDataView(repo = repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideSetDocCoverImageUseCase(
        repo: BlockRepository
    ): SetDocCoverImage = SetDocCoverImage(repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideDownload(repo: UnsplashRepository): DownloadUnsplashImage = DownloadUnsplashImage(
        repo = repo
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideGetTemplates(repo: BlockRepository): GetTemplates = GetTemplates(
        repo = repo,
        dispatchers = AppCoroutineDispatchers(
            io = Dispatchers.IO,
            computation = Dispatchers.Default,
            main = Dispatchers.Main
        )
    )
}