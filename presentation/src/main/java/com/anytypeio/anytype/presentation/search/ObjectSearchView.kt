package com.anytypeio.anytype.presentation.search

import com.anytypeio.anytype.presentation.navigation.DefaultObjectView


sealed class ObjectSearchView {
    object Init : ObjectSearchView()
    object Loading : ObjectSearchView()
    object EmptyPages : ObjectSearchView()
    data class Success(val objects: List<DefaultObjectView>) : ObjectSearchView()
    data class NoResults(val searchText: String) : ObjectSearchView()
    data class Error(val error: String) : ObjectSearchView()
}