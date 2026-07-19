package com.mutissx.dicechallenge.presentation.search.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.mutissx.dicechallenge.R
import com.mutissx.dicechallenge.core.domain.DataException
import com.mutissx.dicechallenge.domain.model.Artist
import com.mutissx.dicechallenge.domain.usecase.SearchArtistsUseCase
import com.mutissx.dicechallenge.core.ui.UiText
import com.mutissx.dicechallenge.core.ui.extensions.asUiText
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SearchViewModel(
    private val searchArtistsUseCase: SearchArtistsUseCase
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val results: Flow<PagingData<Artist>> = _query
        .map { query -> query.trim() }
        .debounce { query ->
            if (isQueryTooShort(query)) 0L else SEARCH_DEBOUNCE_MS
        }
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (isQueryTooShort(query)) {
                flowOf(PagingData.empty())
            } else {
                searchArtistsUseCase(query)
            }
        }
        .cachedIn(viewModelScope)

    fun onQueryChange(value: String) {
        _query.value = value
    }

    fun onClearQuery() {
        _query.value = EMPTY_VALUE
    }

    fun errorMessage(throwable: Throwable): UiText =
        (throwable as? DataException)?.error?.asUiText()
            ?: UiText.StringResource(R.string.search_error_generic)

    companion object {
        const val EMPTY_VALUE = ""
        const val MIN_QUERY_LENGTH = 2
        private const val SEARCH_DEBOUNCE_MS = 400L

        fun isQueryTooShort(query: String): Boolean = query.trim().length < MIN_QUERY_LENGTH
    }
}
