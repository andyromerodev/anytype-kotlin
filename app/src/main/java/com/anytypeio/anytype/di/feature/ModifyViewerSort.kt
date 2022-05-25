package com.anytypeio.anytype.di.feature;

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.presentation.sets.ObjectSet
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.sets.sort.ModifyViewerSortViewModel
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.sets.modals.sort.ModifyViewerSortFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import kotlinx.coroutines.flow.StateFlow

@Subcomponent(modules = [ModifyViewerSortModule::class])
@PerModal
interface ModifyViewerSortSubComponent {
    @Subcomponent.Builder
    interface Builder {
        fun module(module: ModifyViewerSortModule): Builder
        fun build(): ModifyViewerSortSubComponent
    }

    fun inject(fragment: ModifyViewerSortFragment)
}

@Module
object ModifyViewerSortModule {
    @JvmStatic
    @Provides
    @PerModal
    fun provideViewModelFactory(
        state: StateFlow<ObjectSet>,
        session: ObjectSetSession,
        dispatcher: Dispatcher<Payload>,
        updateDataViewViewer: UpdateDataViewViewer,
        analytics: Analytics
    ): ModifyViewerSortViewModel.Factory = ModifyViewerSortViewModel.Factory(
        state = state,
        session = session,
        dispatcher = dispatcher,
        updateDataViewViewer = updateDataViewViewer,
        analytics = analytics
    )
}
