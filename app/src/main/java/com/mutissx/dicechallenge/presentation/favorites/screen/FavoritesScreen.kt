package com.mutissx.dicechallenge.presentation.favorites.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mutissx.dicechallenge.R
import com.mutissx.dicechallenge.presentation.components.ArtistRow
import com.mutissx.dicechallenge.presentation.components.EmptyView
import com.mutissx.dicechallenge.presentation.components.ErrorView
import com.mutissx.dicechallenge.presentation.components.LoadingView
import com.mutissx.dicechallenge.presentation.components.ScreenTitle
import com.mutissx.dicechallenge.presentation.components.TestTags
import com.mutissx.dicechallenge.presentation.favorites.viewmodel.FavoritesViewModel

@Composable
fun FavoritesScreen(
    innerPadding: PaddingValues,
    onArtistClick: (String) -> Unit,
    viewModel: FavoritesViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        ScreenTitle(title = stringResource(R.string.nav_favorites_title))
        when (val currentState = state) {
            FavoritesUiState.Loading -> LoadingView(
                modifier = Modifier.testTag(TestTags.FAVORITES_LOADING_INDICATOR)
            )
            FavoritesUiState.Error -> ErrorView(
                message = stringResource(R.string.favorites_error_generic),
                modifier = Modifier.testTag(TestTags.FAVORITES_ERROR_VIEW)
            )
            is FavoritesUiState.Content -> if (currentState.favorites.isEmpty()) {
                EmptyView(
                    message = stringResource(R.string.favorites_empty),
                    modifier = Modifier.testTag(TestTags.FAVORITES_EMPTY_VIEW)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag(TestTags.FAVORITES_LIST),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(currentState.favorites, key = { it.mbid }) { artist ->
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
}
