package com.mutissx.dicechallenge.presentation.detail.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mutissx.dicechallenge.R
import com.mutissx.dicechallenge.domain.model.Artist
import com.mutissx.dicechallenge.presentation.components.EmptyView
import com.mutissx.dicechallenge.presentation.components.ErrorView
import com.mutissx.dicechallenge.presentation.components.LoadingView
import com.mutissx.dicechallenge.presentation.components.ReleaseGroupRow
import com.mutissx.dicechallenge.presentation.components.TestTags
import com.mutissx.dicechallenge.presentation.detail.viewmodel.ArtistDetailViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailScreen(
    onBack: () -> Unit,
    viewModel: ArtistDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.favoriteMessages.collectLatest { message ->
            snackbarHostState.showSnackbar(message.asString(context))
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.detail_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag(TestTags.DETAIL_BACK_BUTTON)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_button_description)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            (uiState as? ArtistDetailUiState.Content)?.let { content ->
                FloatingActionButton(
                    onClick = viewModel::onFavoriteToggle,
                    modifier = Modifier.testTag(TestTags.DETAIL_FAVORITE_BUTTON),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(0.dp)
                ) {
                    Icon(
                        imageVector = if (content.isFavorite) Icons.Filled.Favorite
                        else Icons.Outlined.FavoriteBorder,
                        contentDescription = stringResource(
                            if (content.isFavorite) R.string.remove_from_favorites_description
                            else R.string.add_to_favorites_description
                        )
                    )
                }
            }
        }
    ) { padding ->
        when (val state = uiState) {
            is ArtistDetailUiState.Loading -> LoadingView(
                modifier = Modifier
                    .padding(padding)
                    .testTag(TestTags.DETAIL_LOADING_INDICATOR)
            )
            is ArtistDetailUiState.Error -> ErrorView(
                message = state.message.asString(),
                onRetry = viewModel::loadInfo,
                modifier = Modifier
                    .padding(padding)
                    .testTag(TestTags.DETAIL_ERROR_VIEW)
            )
            is ArtistDetailUiState.Content -> DetailContent(
                artist = state.artist,
                releaseGroups = state.releaseGroups,
                onRetryReleaseGroups = viewModel::retryReleaseGroups,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        }
    }
}

@Composable
private fun DetailContent(
    artist: Artist,
    releaseGroups: ReleaseGroupsState,
    onRetryReleaseGroups: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.testTag(TestTags.DETAIL_CONTENT_LIST)) {
        item { ArtistHeader(artist = artist) }
        albumsSection(releaseGroups = releaseGroups, onRetryReleaseGroups = onRetryReleaseGroups)
    }
}

@Composable
private fun ArtistHeader(
    artist: Artist,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = artist.name,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        artist.disambiguation?.takeIf { it.isNotBlank() }?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        artist.country?.takeIf { it.isNotBlank() }?.let { country ->
            Surface(
                modifier = Modifier.padding(top = 8.dp),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = country,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }
        Text(
            text = stringResource(R.string.detail_albums_section),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 20.dp)
        )
    }
}

private fun LazyListScope.albumsSection(
    releaseGroups: ReleaseGroupsState,
    onRetryReleaseGroups: () -> Unit
) {
    when (releaseGroups) {
        ReleaseGroupsState.Loading -> item {
            LoadingView(modifier = Modifier.testTag(TestTags.DETAIL_ALBUMS_LOADING))
        }
        is ReleaseGroupsState.Error -> item {
            ErrorView(
                message = releaseGroups.message.asString(),
                onRetry = onRetryReleaseGroups,
                modifier = Modifier.testTag(TestTags.DETAIL_ALBUMS_ERROR)
            )
        }
        is ReleaseGroupsState.Loaded -> if (releaseGroups.items.isEmpty()) {
            item {
                EmptyView(
                    message = stringResource(R.string.detail_empty_albums),
                    modifier = Modifier.testTag(TestTags.DETAIL_EMPTY_ALBUMS)
                )
            }
        } else {
            items(releaseGroups.items, key = { it.mbid }) { rg ->
                ReleaseGroupRow(
                    releaseGroup = rg,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(TestTags.DETAIL_ALBUM_ROW)
                )
            }
        }
    }
}
