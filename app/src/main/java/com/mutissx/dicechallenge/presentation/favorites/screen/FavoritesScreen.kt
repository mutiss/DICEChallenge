package com.mutissx.dicechallenge.presentation.favorites.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mutissx.dicechallenge.R
import com.mutissx.dicechallenge.presentation.components.ArtistRow
import com.mutissx.dicechallenge.presentation.components.EmptyView
import com.mutissx.dicechallenge.presentation.components.ErrorView
import com.mutissx.dicechallenge.presentation.components.LoadingView
import com.mutissx.dicechallenge.presentation.components.TestTags
import com.mutissx.dicechallenge.presentation.favorites.viewmodel.FavoritesViewModel

@Composable
fun FavoritesScreen(
    innerPadding: PaddingValues,
    onArtistClick: (String) -> Unit,
    viewModel: FavoritesViewModel
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        Text(
            text = stringResource(R.string.nav_favorites_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
        )
        when {
            state.isLoading -> LoadingView(
                modifier = Modifier.testTag(TestTags.FAVORITES_LOADING_INDICATOR)
            )
            state.isError -> ErrorView(
                message = stringResource(R.string.favorites_error_generic),
                modifier = Modifier.testTag(TestTags.FAVORITES_ERROR_VIEW)
            )
            state.favorites.isEmpty() -> EmptyView(
                message = stringResource(R.string.favorites_empty),
                modifier = Modifier.testTag(TestTags.FAVORITES_EMPTY_VIEW)
            )
            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag(TestTags.FAVORITES_LIST),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.favorites, key = { it.mbid }) { artist ->
                    ArtistRow(
                        artist = artist,
                        onClick = { onArtistClick(artist.mbid) },
                        modifier = Modifier.testTag(TestTags.FAVORITES_ARTIST_ROW)
                    )
                }
            }
        }
    }
}
