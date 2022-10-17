package com.anytypeio.anytype.presentation.linking

import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.presentation.navigation.DefaultObjectView
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.SupportedLayouts
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.search.ObjectSearchViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class LinkToObjectViewModel(
    urlBuilder: UrlBuilder,
    searchObjects: SearchObjects,
    getObjectTypes: GetObjectTypes,
    analytics: Analytics
) : ObjectSearchViewModel(
    urlBuilder = urlBuilder,
    getObjectTypes = getObjectTypes,
    searchObjects = searchObjects,
    analytics = analytics
) {

    val commands = MutableSharedFlow<Command>(replay = 0)

    override fun getSearchObjectsParams(ignore: Id?) = SearchObjects.Params(
        limit = SEARCH_LIMIT,
        filters = ObjectSearchConstants.getFilterLinkTo(ignore),
        sorts = ObjectSearchConstants.sortLinkTo,
        fulltext = EMPTY_QUERY,
        keys = ObjectSearchConstants.defaultKeys
    )

    override fun onObjectClicked(view: DefaultObjectView) {
        sendSearchResultEvent(view.id)
        viewModelScope.launch {
            commands.emit(
                Command.Link(
                    link = view.id,
                    text = view.name,
                    icon = view.icon,
                    isBookmark = view.layout == ObjectType.Layout.BOOKMARK,
                    isSet = view.layout == ObjectType.Layout.SET
                )
            )
        }
    }

    override fun onDialogCancelled() {
        viewModelScope.launch {
            commands.emit(Command.Exit)
        }
    }

    override suspend fun setObjects(data: List<ObjectWrapper.Basic>) {
        objects.emit(
            data.filter {
                SupportedLayouts.layouts.contains(it.layout)
            }
        )
    }

    sealed class Command {
        object Exit : Command()
        data class Link(
            val link: Id,
            val isBookmark: Boolean,
            val text: String,
            val icon: ObjectIcon,
            val isSet: Boolean
        ) : Command()
    }
}