package com.anytypeio.anytype.presentation.sets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.domain.dataview.interactor.*
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.extension.sendAnalyticsRemoveViewEvent
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class EditDataViewViewerViewModel(
    private val renameDataViewViewer: RenameDataViewViewer,
    private val deleteDataViewViewer: DeleteDataViewViewer,
    private val duplicateDataViewViewer: DuplicateDataViewViewer,
    private val updateDataViewViewer: UpdateDataViewViewer,
    private val setActiveViewer: SetActiveViewer,
    private val dispatcher: Dispatcher<Payload>,
    private val objectSetState: StateFlow<ObjectSet>,
    private val objectSetSession: ObjectSetSession,
    private val analytics: Analytics
) : BaseViewModel() {

    val viewState = MutableStateFlow<ViewState>(ViewState.Init)
    val isDismissed = MutableSharedFlow<Boolean>(replay = 0)
    val isLoading = MutableStateFlow(false)
    val popupCommands = MutableSharedFlow<PopupMenuCommand>(replay = 0)

    var initialName: String = ""
    var initialType: DVViewerType = DVViewerType.GRID

    private var viewerType: DVViewerType = DVViewerType.GRID
    private var viewerName: String = ""

    fun onStart(viewerId: Id) {
        val viewer = objectSetState.value.viewers.firstOrNull { it.id == viewerId }
        if (viewer != null) {
            initialName = viewer.name
            initialType = viewer.type
            viewState.value = ViewState.Name(viewer.name)
            updateViewState(viewer.type)
        } else {
            Timber.e("Can't find viewer by id : $viewerId")
        }
    }

    fun onViewerNameChanged(name: String) {
        viewerName = name
    }

    fun onDuplicateClicked(ctx: Id, viewer: Id) {
        viewModelScope.launch {
            duplicateDataViewViewer(
                DuplicateDataViewViewer.Params(
                    context = ctx,
                    target = objectSetState.value.dataview.id,
                    viewer = objectSetState.value.viewers.first { it.id == viewer }
                )
            ).process(
                failure = { e ->
                    Timber.e(e, "Error while duplicating viewer: $viewer")
                    _toasts.emit("Error while deleting viewer: ${e.localizedMessage}")
                },
                success = {
                    dispatcher.send(it).also { isDismissed.emit(true) }
                }
            )
        }
    }

    fun onDeleteClicked(ctx: Id, viewer: Id) {
        val state = objectSetState.value
        if (state.viewers.size > 1) {
            val targetIdx = state.viewers.indexOfFirst { it.id == viewer }
            val isActive = if (objectSetSession.currentViewerId != null) {
                objectSetSession.currentViewerId == viewer
            } else {
                targetIdx == 0
            }
            var nextViewerId: Id? = null
            if (isActive) {
                nextViewerId = if (targetIdx != state.viewers.lastIndex)
                    state.viewers[targetIdx.inc()].id
                else
                    state.viewers[targetIdx.dec()].id
            }
            proceedWithDeletion(
                ctx = ctx,
                dv = state.dataview.id,
                viewer = viewer,
                nextViewerId = nextViewerId,
                state = state
            )
        } else {
            viewModelScope.launch {
                _toasts.emit("Data view should have at least one view")
            }
        }
    }

    private fun proceedWithDeletion(
        ctx: Id,
        dv: Id,
        viewer: Id,
        nextViewerId: Id?,
        state: ObjectSet
    ) {
        viewModelScope.launch {
            deleteDataViewViewer(
                DeleteDataViewViewer.Params(
                    ctx = ctx,
                    viewer = viewer,
                    dataview = dv
                )
            ).process(
                failure = { e ->
                    Timber.e(e, "Error while deleting viewer: $viewer")
                    _toasts.emit("Error while deleting viewer: ${e.localizedMessage}")
                },
                success = { firstPayload ->
                    dispatcher.send(firstPayload)
                    sendAnalyticsRemoveViewEvent(
                        analytics = analytics
                    )
                    if (nextViewerId != null) {
                        proceedWithSettingActiveView(ctx, state, nextViewerId)
                    } else {
                        isDismissed.emit(true)
                    }
                }
            )
        }
    }

    private fun proceedWithSettingActiveView(
        ctx: Id,
        state: ObjectSet,
        nextViewerId: Id
    ) {
        viewModelScope.launch {
            setActiveViewer(
                SetActiveViewer.Params(
                    context = ctx,
                    block = state.dataview.id,
                    view = nextViewerId,
                    limit = ObjectSetConfig.DEFAULT_LIMIT,
                    offset = 0
                )
            ).process(
                failure = { e ->
                    Timber.e(e, "Error while setting active viewer after deletion")
                },
                success = { secondPayload ->
                    dispatcher.send(secondPayload).also { isDismissed.emit(true) }
                }
            )
        }
    }

    fun onMenuClicked() {
        viewModelScope.launch {
            popupCommands.emit(
                PopupMenuCommand(isDeletionAllowed = objectSetState.value.viewers.size > 1)
            )
        }
    }

    fun onDoneClicked(ctx: Id, viewerId: Id) {
        if (initialName != viewerName || initialType != viewerType) {
            updateDVViewerType(ctx, viewerId, viewerType, viewerName)
        } else {
            viewModelScope.launch { isDismissed.emit(true) }
        }
    }

    fun onGridClicked() {
        updateViewState(DVViewerType.GRID)
    }

    fun onListClicked() {
        updateViewState(DVViewerType.LIST)
    }

    fun onGalleryClicked() {
        updateViewState(DVViewerType.GALLERY)
    }

    private fun updateDVViewerType(ctx: Id, viewerId: Id, type: DVViewerType, name: String) {
        val state = objectSetState.value
        val viewer = state.viewers.first { it.id == viewerId }
        viewModelScope.launch {
            isLoading.value = true
            updateDataViewViewer(
                UpdateDataViewViewer.Params(
                    context = ctx,
                    target = state.dataview.id,
                    viewer = viewer.copy(type = type, name = name)
                )
            ).process(
                success = { payload ->
                    dispatcher.send(payload)
                    isLoading.value = false
                    isDismissed.emit(true)
                },
                failure = {
                    isLoading.value = false
                    Timber.e(it, "Error while updating Viewer type")
                    isDismissed.emit(true)
                }
            )
        }
    }

    private fun updateViewState(type: DVViewerType) {
        viewerType = type
        viewState.value = when (type) {
            Block.Content.DataView.Viewer.Type.GRID -> ViewState.Grid
            Block.Content.DataView.Viewer.Type.LIST -> ViewState.List
            Block.Content.DataView.Viewer.Type.GALLERY -> ViewState.Gallery
            Block.Content.DataView.Viewer.Type.BOARD -> ViewState.Kanban
        }
    }

    class Factory(
        private val renameDataViewViewer: RenameDataViewViewer,
        private val deleteDataViewViewer: DeleteDataViewViewer,
        private val duplicateDataViewViewer: DuplicateDataViewViewer,
        private val updateDataViewViewer: UpdateDataViewViewer,
        private val setActiveViewer: SetActiveViewer,
        private val dispatcher: Dispatcher<Payload>,
        private val objectSetState: StateFlow<ObjectSet>,
        private val objectSetSession: ObjectSetSession,
        private val analytics: Analytics
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return EditDataViewViewerViewModel(
                renameDataViewViewer = renameDataViewViewer,
                deleteDataViewViewer = deleteDataViewViewer,
                duplicateDataViewViewer = duplicateDataViewViewer,
                updateDataViewViewer = updateDataViewViewer,
                setActiveViewer = setActiveViewer,
                dispatcher = dispatcher,
                objectSetState = objectSetState,
                objectSetSession = objectSetSession,
                analytics = analytics
            ) as T
        }
    }

    private data class ViewerNameUpdate(
        val ctx: Id,
        val dataview: Id,
        val viewer: DVViewer,
        val name: String
    )

    data class PopupMenuCommand(val isDeletionAllowed: Boolean = false)

    sealed class ViewState {
        object Init : ViewState()
        data class Name(val name: String) : ViewState()
        object Completed : ViewState()
        object Grid : ViewState()
        object Gallery : ViewState()
        object List : ViewState()
        object Kanban : ViewState()
        data class Error(val msg: String) : ViewState()
    }
}