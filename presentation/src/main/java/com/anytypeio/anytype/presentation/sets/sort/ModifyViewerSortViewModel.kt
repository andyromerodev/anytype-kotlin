package com.anytypeio.anytype.presentation.sets.sort

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.core_utils.ext.cancel
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.extension.sendAnalyticsChangeSortValueEvent
import com.anytypeio.anytype.presentation.sets.ObjectSet
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.sets.viewerById
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class ModifyViewerSortViewModel(
    private val objectSetState: StateFlow<ObjectSet>,
    private val session: ObjectSetSession,
    private val dispatcher: Dispatcher<Payload>,
    private val updateDataViewViewer: UpdateDataViewViewer,
    private val analytics: Analytics
) : BaseViewModel() {

    val isDismissed = MutableSharedFlow<Boolean>(replay = 0)

    val viewState = MutableStateFlow<ViewState?>(null)
    private val jobs = mutableListOf<Job>()

    fun onStart(relationId: Id) {
        jobs += viewModelScope.launch {
            objectSetState.filter { it.isInitialized }.collect { state ->
                val dv = state.dataview.content as DV
                val viewer = state.viewerById(session.currentViewerId)
                val sort = viewer.sorts.first { it.relationKey == relationId }
                val relation = dv.relations.first { it.key == relationId }
                viewState.value = ViewState(
                    format = relation.format,
                    type = sort.type,
                    name = relation.name
                )
            }
        }
    }

    fun onStop() {
        jobs.cancel()
    }

    fun onSortDescSelected(ctx: Id, relation: Id) {
        proceedWithUpdatingSortType(ctx, relation, DVSortType.DESC)
    }

    fun onSortAscSelected(ctx: Id, relation: Id) {
        proceedWithUpdatingSortType(ctx, relation, DVSortType.ASC)
    }

    private fun proceedWithUpdatingSortType(
        ctx: Id,
        relation: Id,
        type: Block.Content.DataView.Sort.Type
    ) {
        val state = objectSetState.value
        val viewer = state.viewerById(session.currentViewerId)
        viewModelScope.launch {
            updateDataViewViewer(
                UpdateDataViewViewer.Params(
                    context = ctx,
                    target = state.dataview.id,
                    viewer = viewer.copy(
                        sorts = viewer.sorts.map { sort ->
                            if (sort.relationKey == relation)
                                sort.copy(
                                    type = type
                                )
                            else
                                sort
                        }
                    )
                )
            ).process(
                success = {
                    dispatcher.send(it).also {
                        sendAnalyticsChangeSortValueEvent(analytics)
                        isDismissed.emit(true)
                    }
                },
                failure = { Timber.e(it, "Error while updating sort type") }
            )
        }
    }

    class ViewState(
        val format: Relation.Format,
        val type: DVSortType,
        val name: String
    )

    class Factory(
        private val state: StateFlow<ObjectSet>,
        private val dispatcher: Dispatcher<Payload>,
        private val session: ObjectSetSession,
        private val updateDataViewViewer: UpdateDataViewViewer,
        private val analytics: Analytics
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ModifyViewerSortViewModel(
                objectSetState = state,
                dispatcher = dispatcher,
                session = session,
                updateDataViewViewer = updateDataViewViewer,
                analytics = analytics
            ) as T
        }
    }
}