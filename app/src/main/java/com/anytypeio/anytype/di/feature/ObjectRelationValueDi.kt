package com.anytypeio.anytype.di.feature

import android.content.Context
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.domain.`object`.ObjectTypesProvider
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.dataview.interactor.AddDataViewRelationOption
import com.anytypeio.anytype.domain.dataview.interactor.AddTagToDataViewRecord
import com.anytypeio.anytype.domain.dataview.interactor.RemoveStatusFromDataViewRecord
import com.anytypeio.anytype.domain.dataview.interactor.RemoveTagFromDataViewRecord
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewRecord
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.relations.AddFileToObject
import com.anytypeio.anytype.domain.relations.AddFileToRecord
import com.anytypeio.anytype.presentation.relations.providers.ObjectDetailProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.presentation.sets.RelationValueDVViewModel
import com.anytypeio.anytype.presentation.sets.RelationValueViewModel
import com.anytypeio.anytype.presentation.util.CopyFileToCacheDirectory
import com.anytypeio.anytype.presentation.util.DefaultCopyFileToCacheDirectory
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.relations.RelationValueDVFragment
import com.anytypeio.anytype.ui.relations.RelationValueFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [ObjectRelationValueModule::class, ObjectSetObjectRelationValueModule::class])
@PerModal
interface ObjectSetObjectRelationValueSubComponent {
    @Subcomponent.Builder
    interface Builder {
        fun module(module: ObjectRelationValueModule): Builder
        fun build(): ObjectSetObjectRelationValueSubComponent
    }

    fun inject(fragment: RelationValueDVFragment)

    fun addObjectRelationValueComponent(): AddObjectRelationValueSubComponent.Builder
    fun addObjectRelationObjectValueComponent(): AddObjectRelationSubComponent.Builder
    fun addRelationFileValueAddComponent() : AddFileRelationSubComponent.Builder
}

@Subcomponent(modules = [ObjectRelationValueModule::class, ObjectObjectRelationValueModule::class])
@PerModal
interface ObjectObjectRelationValueSubComponent {
    @Subcomponent.Builder
    interface Builder {
        fun module(module: ObjectRelationValueModule): Builder
        fun build(): ObjectObjectRelationValueSubComponent
    }

    fun inject(fragment: RelationValueFragment)

    fun addObjectRelationValueComponent(): AddObjectRelationValueSubComponent.Builder
    fun addObjectRelationObjectValueComponent(): AddObjectRelationSubComponent.Builder
    fun addRelationFileValueAddComponent() : AddFileRelationSubComponent.Builder
}

@Module
object ObjectRelationValueModule {

    @JvmStatic
    @Provides
    @PerModal
    fun provideAddRelationOptionUseCase(
        repo: BlockRepository
    ): AddDataViewRelationOption = AddDataViewRelationOption(repo = repo)

    @JvmStatic
    @Provides
    @PerModal
    fun provideAddTagToDataViewRecordUseCase(
        repo: BlockRepository
    ): AddTagToDataViewRecord = AddTagToDataViewRecord(repo = repo)

    @JvmStatic
    @Provides
    @PerModal
    fun provideRemoveTagFromDataViewRecordUseCase(
        repo: BlockRepository
    ): RemoveTagFromDataViewRecord = RemoveTagFromDataViewRecord(repo = repo)

    @JvmStatic
    @Provides
    @PerModal
    fun provideRemoveStatusFromDataViewRecordUseCase(
        repo: BlockRepository
    ): RemoveStatusFromDataViewRecord = RemoveStatusFromDataViewRecord(repo = repo)
}

@Module
object ObjectSetObjectRelationValueModule {

    @JvmStatic
    @Provides
    @PerModal
    fun provideCopyFileToCache(
        context: Context
    ): CopyFileToCacheDirectory = DefaultCopyFileToCacheDirectory(context)

    @JvmStatic
    @Provides
    @PerModal
    fun provideViewModelFactoryForDataView(
        relations: ObjectRelationProvider,
        values: ObjectValueProvider,
        details: ObjectDetailProvider,
        types: ObjectTypesProvider,
        removeTagFromDataViewRecord: RemoveTagFromDataViewRecord,
        removeStatusFromDataViewRecord: RemoveStatusFromDataViewRecord,
        urlBuilder: UrlBuilder,
        updateDataViewRecord: UpdateDataViewRecord,
        addFileToRecord: AddFileToRecord,
        copyFileToCacheDirectory: CopyFileToCacheDirectory
    ): RelationValueDVViewModel.Factory = RelationValueDVViewModel.Factory(
        relations = relations,
        values = values,
        details = details,
        types = types,
        removeTagFromRecord = removeTagFromDataViewRecord,
        removeStatusFromDataViewRecord = removeStatusFromDataViewRecord,
        urlBuilder = urlBuilder,
        updateDataViewRecord = updateDataViewRecord,
        addFileToRecord = addFileToRecord,
        copyFileToCache = copyFileToCacheDirectory
    )
}

@Module
object ObjectObjectRelationValueModule {

    @JvmStatic
    @Provides
    @PerModal
    fun provideViewModelFactoryForObject(
        relations: ObjectRelationProvider,
        values: ObjectValueProvider,
        details: ObjectDetailProvider,
        types: ObjectTypesProvider,
        urlBuilder: UrlBuilder,
        dispatcher: Dispatcher<Payload>,
        updateDetail: UpdateDetail,
        addFileToObject: AddFileToObject,
        copyFileToCacheDirectory: CopyFileToCacheDirectory,
        analytics: Analytics
    ): RelationValueViewModel.Factory = RelationValueViewModel.Factory(
        relations = relations,
        values = values,
        details = details,
        types = types,
        urlBuilder = urlBuilder,
        dispatcher = dispatcher,
        updateDetail = updateDetail,
        addFileToObject = addFileToObject,
        copyFileToCache = copyFileToCacheDirectory,
        analytics = analytics
    )
}