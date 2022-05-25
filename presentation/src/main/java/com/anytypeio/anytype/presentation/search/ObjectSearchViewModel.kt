package com.anytypeio.anytype.presentation.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.core_utils.ext.cancel
import com.anytypeio.anytype.core_utils.ui.ViewStateViewModel
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.dataview.interactor.SearchObjects
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.extension.sendAnalyticsSearchQueryEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsSearchResultEvent
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.navigation.SupportNavigation
import com.anytypeio.anytype.presentation.objects.toView
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import timber.log.Timber

open class ObjectSearchViewModel(
    private val urlBuilder: UrlBuilder,
    private val searchObjects: SearchObjects,
    private val getObjectTypes: GetObjectTypes,
    private val analytics: Analytics
) : ViewStateViewModel<ObjectSearchView>(),
    SupportNavigation<EventWrapper<AppNavigation.Command>> {

    private val jobs = mutableListOf<Job>()

    private val userInput = MutableStateFlow(EMPTY_QUERY)
    private val searchQuery = userInput
        .take(1)
        .onCompletion {
            emitAll(userInput.drop(1).debounce(DEBOUNCE_DURATION).distinctUntilChanged())
        }

    protected val types = MutableSharedFlow<List<ObjectType>>(replay = 0)
    protected val objects = MutableSharedFlow<List<ObjectWrapper.Basic>>(replay = 0)

    override val navigation = MutableLiveData<EventWrapper<AppNavigation.Command>>()

    private var eventRoute = ""

    init {
        viewModelScope.launch {
            combine(objects, types) { listOfObjects, listOfTypes ->
                listOfObjects.toView(
                    urlBuilder = urlBuilder,
                    objectTypes = listOfTypes
                )
            }.collectLatest { views ->
                if (views.isNotEmpty())
                    stateData.postValue(ObjectSearchView.Success(views))
                else
                    stateData.postValue(ObjectSearchView.NoResults(userInput.value))
            }
        }
    }

    fun onStart(route: String) {
        eventRoute = route
        getObjectTypes()
        startProcessingSearchQuery()
    }

    fun onStop() {
        jobs.cancel()
    }

    private fun getObjectTypes() {
        jobs += viewModelScope.launch {
            val params = GetObjectTypes.Params(filterArchivedObjects = true)
            getObjectTypes.invoke(params).process(
                failure = { Timber.e(it, "Error while getting object types") },
                success = { types.emit(it) }
            )
        }
    }

    private fun startProcessingSearchQuery() {
        jobs += viewModelScope.launch {
            searchQuery.collectLatest { query ->
                sendSearchQueryEvent(query)
                val params = getSearchObjectsParams().copy(fulltext = query)
                searchObjects(params = params).process(
                    success = { objects -> setObjects(objects) },
                    failure = { Timber.e(it, "Error while searching for objects") }
                )
            }
        }
    }

    private fun sendSearchQueryEvent(query: String) {
        viewModelScope.sendAnalyticsSearchQueryEvent(
            analytics = analytics,
            route = eventRoute,
            length = query.length
        )
    }

    open suspend fun setObjects(data: List<ObjectWrapper.Basic>) {
        objects.emit(data)
    }

    fun onSearchTextChanged(searchText: String) {
        userInput.value = searchText
    }

    open fun onObjectClicked(target: Id, layout: ObjectType.Layout?) {
        sendSearchResultEvent(target)
        when (layout) {
            ObjectType.Layout.PROFILE,
            ObjectType.Layout.BASIC,
            ObjectType.Layout.TODO,
            ObjectType.Layout.NOTE,
            ObjectType.Layout.FILE,
            ObjectType.Layout.IMAGE -> {
                navigate(EventWrapper(AppNavigation.Command.LaunchDocument(id = target)))
            }
            ObjectType.Layout.SET -> {
                navigate(EventWrapper(AppNavigation.Command.LaunchObjectSet(target = target)))
            }
            else -> {
                Timber.e("Unexpected layout: $layout")
            }
        }
    }

    protected fun sendSearchResultEvent(id: String) {
        val value = state.value
        if (value is ObjectSearchView.Success) {
            val index = value.objects.indexOfFirst { it.id == id }
            if (index != -1) {
                viewModelScope.sendAnalyticsSearchResultEvent(
                    analytics = analytics,
                    pos = index + 1,
                    length = userInput.value.length
                )
            }
        }
    }

    open fun getSearchObjectsParams() = SearchObjects.Params(
        limit = SEARCH_LIMIT,
        filters = ObjectSearchConstants.filterSearchObjects,
        sorts = ObjectSearchConstants.sortsSearchObjects,
        fulltext = EMPTY_QUERY,
        keys = ObjectSearchConstants.defaultKeys
    )

    open fun onDialogCancelled() {
        navigateToDesktop()
    }

    private fun navigateToDesktop() {
        navigation.postValue(EventWrapper(AppNavigation.Command.ExitToDesktop))
    }

    companion object {
        const val EMPTY_QUERY = ""
        const val DEBOUNCE_DURATION = 300L
        const val SEARCH_LIMIT = 200
    }
}