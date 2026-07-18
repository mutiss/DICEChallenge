package com.mutissx.dicechallenge.presentation.search.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.mutissx.dicechallenge.R
import com.mutissx.dicechallenge.presentation.components.ArtistRow
import com.mutissx.dicechallenge.presentation.components.EmptyView
import com.mutissx.dicechallenge.presentation.components.ErrorView
import com.mutissx.dicechallenge.presentation.components.LoadingView
import com.mutissx.dicechallenge.presentation.components.TestTags
import com.mutissx.dicechallenge.presentation.search.viewmodel.SearchViewModel

@Composable
fun SearchScreen(
    innerPadding: PaddingValues,
    onArtistClick: (String) -> Unit,
    viewModel: SearchViewModel
) {
    val query by viewModel.query.collectAsState()
    val items = viewModel.results.collectAsLazyPagingItems()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        Text(
            text = stringResource(R.string.nav_search_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
        )

        TextField(
            value = query,
            onValueChange = viewModel::onQueryChange,
            placeholder = {
                Text(
                    text = stringResource(R.string.search_placeholder),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = viewModel::onClearQuery,
                        modifier = Modifier.testTag(TestTags.SEARCH_CLEAR_BUTTON)
                    ) {
                        Icon(
                            Icons.Filled.Clear,
                            contentDescription = stringResource(R.string.clear_search_description),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            singleLine = true,
            shape = MaterialTheme.shapes.extraLarge,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 16.dp)
                .testTag(TestTags.SEARCH_TEXT_FIELD),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
        )

        val refresh = items.loadState.refresh
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                query.trim().length < SearchViewModel.MIN_QUERY_LENGTH -> {
                    EmptyView(
                        modifier = Modifier.testTag(TestTags.EMPTY_VIEW_START),
                        message = stringResource(R.string.search_empty_start)
                    )
                }
                refresh is LoadState.Loading -> LoadingView(
                    modifier = Modifier.testTag(TestTags.INITIAL_LOADING_INDICATOR)
                )
                refresh is LoadState.Error -> ErrorView(
                    modifier = Modifier.testTag(TestTags.ERROR_VIEW),
                    message = refresh.error.localizedMessage ?: stringResource(R.string.search_error_generic),
                    onRetry = { items.retry() }
                )
                items.itemCount == 0 -> EmptyView(
                    modifier = Modifier.testTag(TestTags.EMPTY_VIEW_NO_RESULTS),
                    message = stringResource(R.string.search_empty_no_results)
                )
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag(TestTags.SEARCH_RESULTS_LIST),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(
                            count = items.itemCount,
                            key = items.itemKey { it.mbid }
                        ) { index ->
                            val artist = items[index] ?: return@items
                            ArtistRow(
                                artist = artist,
                                onClick = { onArtistClick(artist.mbid) },
                                modifier = Modifier.testTag(TestTags.ARTIST_ROW)
                            )
                        }
                        if (items.loadState.append is LoadState.Loading) {
                            item {
                                LoadingView(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .testTag(TestTags.APPEND_LOADING_INDICATOR)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
